package utils;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Pattern;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import commands.ControlStructures;
import commands.Input;
import commands.Print;
import commands.Variable;
import commands.Loop;

import javax.tools.JavaCompiler.CompilationTask;

import display.Console;
import display.Editor;
import display.State;

/*
 * class that handles the execution of a class file
 * checks for errors in user input code
 * stores user declared variables for data-type reference
 * uses compiler to execute the code
 * able to save and export file as Java
 */
public class FileExecutionTool {
	
	// map that stores the user customized commands and declared variables
	public static HashMap<String, String> userCommands = new HashMap<String, String>();
	public static ArrayList<Variable> userDeclaredVariables = new ArrayList<Variable>();
	
	// boolean variable for terminating the program
	public static boolean executeSuccessful;
	
	// String variable that stores the translated code in Java
	public static String translatedCode = "";
	
	// print writer that writes output to the console
	public static PrintWriter printer;
	
	// variables that checks for indentation
	public static int previousTabNumber = 0, currentTabNumber = 0;
	
	// constructor initializes the user commands, and empties the translated code
	public FileExecutionTool() {
		
		updateCommands();
		resetCode();
		
		// declares the print writer for saving the Java file for code execution
		try {
			
			printer = new PrintWriter("src/JarRunFile.java");
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	// method that resets the program when user runs the program
	public static void resetCode() {
		
		// resets the output file, indent information, and console
		previousTabNumber = 0;
		currentTabNumber = 0;
		translatedCode = String.format("public class JarRunFile%d { " 
				+ "\npublic static void main(String[] args) {", State.numExecutions);
		
		Console.consoleTextArea.setText("");
		
	}
	
	// method that updates the customizable commands
	public static void updateCommands() {
		
		// empties the map
		userCommands.clear();
		
		// try to see if the user command file exist
		try {
			
			Scanner command = new Scanner(new File("commands/userCommands"));
			
			// read every command into the command list
			while(command.hasNext()) {
				
				userCommands.put(command.next(), command.next());
				
			}
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	// method that executes a input file
	public static void executeFile(File file) {
		
		//Switch console output to the console text area
		PrintStream printStream = new PrintStream(new CustomOutputStream(Console.consoleTextArea));
		System.setOut(printStream);
		System.setErr(printStream);
		        
		// reset run information
		resetCode();
		userDeclaredVariables.clear();
		
		// try and catch to read the input file using a scanner
		try {
			
			int lineNumber = 0;
			Scanner input = new Scanner(file);
			
			executeSuccessful = true;
			
			// declaring a stack to keep track of the loop and control structure order
			Stack<String> loopContainer = new Stack<String>();
			
			// executes while a new line can be read from the input file
			while(input.hasNext()) {
				
				// if the execution fails, exit the file execution
				if(!executeSuccessful) {
					return;
				}
				
				// update the user indent and line number
				previousTabNumber = currentTabNumber;
				String line = input.nextLine();
				lineNumber++;
				
				// checks if the line contains a command operator, else it is not able to be executed
				if(!(line.contains(":") || line.contains("-") || line.contains("+") || line.contains("*") || line.contains("/") || line.contains("=") || line.trim().equals(""))) {
					
					terminate("No Command Executors Found: Line ", lineNumber);
					return;
					
				}
				
				// checks if the line is not empty
				if(line.trim().length() > 0) {
					
					// Calculate the tab difference to auto close control structures and loops
					int numTabs = 0;
					for(int i = 0; i < line.length(); i++) {
						if(line.charAt(i) == '\t') {
							numTabs++;
						}
					}
					
					currentTabNumber = numTabs;
					
					// add the necessary closing to the translated code, if the tab difference is different from the previous line
					while(currentTabNumber < previousTabNumber) {
						translatedCode += "\n}\n";
						previousTabNumber--;
				
						// terminate the program if there is an unnecessary indent
						if(loopContainer.isEmpty()) {
							
							terminate("Unessasary Tab Entered: Line ", lineNumber);
							return;
							
						}
						
						// if a loop was previously declared, remove the loop from the container
						if(!loopContainer.isEmpty() && loopContainer.peek().equals("loop")) {
							
							loopContainer.pop();
							
						}
						
						// checks to see if 
						if((line.indexOf(":") != -1 || line.equals("")) && !(line.substring(0, line.indexOf(":")).trim().equals(userCommands.get("else_if")) || 
								line.substring(0, line.indexOf(":")).trim().equals(userCommands.get("else"))) && 
								!loopContainer.isEmpty() && loopContainer.peek().equals("if")) {

							loopContainer.pop();
							
						}
						
					}
				
				}
				
				for(int i = 0; i < line.length(); i++) {
					
					if(line.charAt(i) == ':') {
						
						String key = line.substring(0, i).trim();
						String action = line.substring(line.indexOf(key) + key.length() + 1).trim();
						
						boolean actionPerformed = false;
						
						for(HashMap.Entry<String, String> command : userCommands.entrySet()) {
							
							if(key.equals(command.getValue()) && command.getKey().equals("print")) {
								
								line = action;
								Print.print(line, lineNumber);
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("printl")) {
								
								line = action;
								Print.printLine(line, lineNumber);
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("input")) {
								
								Input.readVariable(action, lineNumber);
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && (command.getKey().equals("if"))) {
								
								loopContainer.push("if");
								
								ControlStructures.initialize(action, command.getKey(), lineNumber);
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && (command.getKey().equals("else_if") || 
									command.getKey().equals("else"))) {
								
								if(!loopContainer.isEmpty() && loopContainer.peek().equals("if")) {
									
									ControlStructures.initialize(action, command.getKey(), lineNumber);
									actionPerformed = true;
									
								}
								
								else {
									
									terminate("Invalid Control Structure(check structure and placement): Line ", lineNumber);
									return;
									
								}
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("loop")) {
								
								Loop.initialize(action, lineNumber);
								loopContainer.add("loop");
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("break")) {
								
								if(loopContainer.contains("loop")) {
									
									translatedCode += "\nbreak;";
									
								} else {
									
									terminate("No Loop to Break Out: Line ", lineNumber);
									return;
									
								}
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("continue")) {
								
								if(loopContainer.contains("loop")) {
									
									translatedCode += "\ncontinue;";
									
								} else {
									
									terminate("No Loop to Break Out: Line ", lineNumber);
									return;
									
								}
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("random")) {
								
								if(action.contains(":")) {
									
									boolean isFound = false;
									
									for(Variable var: userDeclaredVariables) {
										
										String actioVariable = action.substring(0, action.indexOf(":")).trim();
										if(var.getName().equals(actioVariable)) {
											
											var.getRandom(action.substring(action.indexOf(":") + 1, action.length()).trim(), lineNumber);
											isFound = true;
											actionPerformed = true;
											break;
											
										}
										
									}
									
									if(!isFound) {
										
										terminate("Variable have to be Initialized Before Randomizing(keyword: variable: randomizer): Line ", lineNumber);
										return;
										
									}
									
								} else {
									
									terminate("improper Random Format(keyword: variable: randomizer): Line ", lineNumber);
									return;
									
								}
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("parse(int/double)")) {
								
								if(action.contains(":")) {
									
									boolean isFound = false;
									
									for(Variable var: userDeclaredVariables) {
										
										String actioVariable = action.substring(0, action.indexOf(":")).trim();
										if(var.getName().equals(actioVariable)) {
											
											var.parse(action.substring(action.indexOf(":") + 1, action.length()).trim(), lineNumber);
											isFound = true;
											actionPerformed = true;
											break;
											
										}
										
									}
									
									if(!isFound) {
										
										terminate("Variable Have to be Initialized First Before Parsing(keyword: int/double variable: String value): Line ", lineNumber);
										return;
										
									}
									
								} else {
									
									terminate("Improper Parse_Int Format(keyword: int/doublevariable: String value): Line ", lineNumber);
									return;
									
								}
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("to_string")) {
								
								if(action.contains(":")) {
									
									boolean isFound = false;
									
									for(Variable var: userDeclaredVariables) {
										
										String actioVariable = action.substring(0, action.indexOf(":")).trim();
										if(var.getName().equals(actioVariable)) {
											
											var.toStringValue(action.substring(action.indexOf(":") + 1, action.length()).trim(), lineNumber);
											isFound = true;
											actionPerformed = true;
											break;
											
										}
										
									}
									
									if(!isFound) {
										
										terminate("Variable Have to be Initialized First Before To_String(keyword: String variable: value): Line ", lineNumber);
										return;
										
									}
									
								} else {
									
									terminate("Improper To_String Format(keyword: String variable: value): Line ", lineNumber);
									return;
									
								}

							}
							
							if(actionPerformed) {
								break;
							}
							
						}
						
						if(actionPerformed) {
							
							break;
							
						} else {
							
							terminate("Unknown Keyword: Line ", lineNumber);
							
						}
						
					} else if(line.charAt(i) == '=') {
						
						String variable = line.substring(0, i).trim();
						String value = line.substring(i+1, line.length()).trim();
						
						boolean found = false;
						
						for(Variable var: userDeclaredVariables) {
							
							if(var.getName().equals(variable)) {
								
								found = true;
								
								var.setValue(value, lineNumber);
								
								break;
								
							}
							
						}
						
						if(!found) {
							
							if(!loopContainer.isEmpty()) {
								
								terminate("Variables Cannot be Declared Inside a Loop or Control Structure: Line ", lineNumber);
								
							} else {
								
								userDeclaredVariables.add(new Variable(variable, value, true, lineNumber));
								
							}
							
						}
						
					} else if(line.charAt(i) == '+' || line.charAt(i) == '-' || line.charAt(i) == '*'
							|| line.charAt(i) == '/') {
						
						//char operator = line.charAt(i);
						String variable = line.substring(0, i).trim();
						String operator = line.substring(i, i + 1).trim();
						String calculation = line.substring(i + 1, line.length()).trim();
						boolean found = false;
						
						for(Variable var: userDeclaredVariables) {
							
							if(var.getName().equals(variable)) {
								
								found = true;
								var.calculate(calculation, operator, lineNumber);
								break;
								
							}
							
						}
						
						if(!found) {
							
							terminate("Invalid Calculation: Line ", lineNumber);
							return;
							
						}
						
						break;
						
					} 
					
					if(!executeSuccessful) {
						return;
					}
					
				}
				
				if(!executeSuccessful) {
					return;
				}
				
			}
			
			translatedCode += "\n}\n";
			
			while(!loopContainer.isEmpty()) {
				translatedCode += "\n}";
				loopContainer.pop();
			}
			
			translatedCode += "\n}";
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
		System.setProperty("java.home", State.JDKFilepath);
		
        //Write java code to a file
        File jarFile = new File(String.format("src/JarRunFile%d.java", State.numExecutions));
        
        try {
        	
            PrintWriter pr = new PrintWriter(jarFile);
            
            pr.print(translatedCode);
            
            pr.close();
            
        } catch (IOException e) {
            System.out.println("Class file was not created");
            e.printStackTrace();
        }
        
        //Specify the bin path for the compiler
        String regex = String.format("\\s*\\bsrc\\\\JarRunFile%d.java\\b\\s*", State.numExecutions);
        String binPath = jarFile.getAbsolutePath().replaceAll(regex, "bin");
        
		//SOURCE: https://stackoverflow.com/questions/2028193/specify-output-path-for-dynamic-compilation/7532171
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager sjfm = javaCompiler.getStandardFileManager(null, null, null); 

		String[] options = new String[] { "-d", binPath };
		File[] javaFiles = new File[] { new File(String.format("src/JarRunFile%d.java", State.numExecutions)) };

		CompilationTask compilationTask = javaCompiler.getTask(null, null, null,
		        Arrays.asList(options),
		        null,
		        sjfm.getJavaFileObjects(javaFiles)
		);
		compilationTask.call();
		
		//Call the main method
		try {
			
			String[] params = null;
			Class<?> cls = Class.forName(String.format("JarRunFile%d", State.numExecutions));

			Method method;
			try {
				//System.out.println(String.format("Executing JarRunFile%d.main", State.numExecutions));
				method = cls.getMethod("main", String[].class);
				method.invoke(null, (Object) params);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch( ClassNotFoundException e ) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Delete the new JarRunFile
		if(jarFile.exists()) {
			jarFile.delete();
		}
		
		State.numExecutions++; //Increment the number of executions
		
	}
	
	//This method allows the user to export cyclone code as java code
	public static void exportFile(File file) {
		
		//Switch console output to the console text area
		PrintStream printStream = new PrintStream(new CustomOutputStream(Console.consoleTextArea));
		System.setOut(printStream);
		System.setErr(printStream);
				
		resetCode();
		
		userDeclaredVariables.clear();
		
		try {
			
			int lineNumber = 0;
			boolean inputUsed = false;
			Scanner input = new Scanner(file);
			
			executeSuccessful = true;
			Stack<String> loopContainer = new Stack<String>();
			
			while(input.hasNext()) {
				
				if(!executeSuccessful) {
					return;
				}
				
				previousTabNumber = currentTabNumber;
				String line = input.nextLine();
				lineNumber++;
				
				if(line.trim().length() > 0) {
					
					int numTabs = 0;
					for(int i = 0; i < line.length(); i++) {
						if(line.charAt(i) == '\t') {
							numTabs++;
						}
					}
					
					currentTabNumber = numTabs;
					
					while(currentTabNumber < previousTabNumber) {
						translatedCode += "\n}\n";
						previousTabNumber--;
				
						if(loopContainer.isEmpty()) {
							
							terminate("Unessasary Tab Entered: Line ", lineNumber);
							return;
							
						}
						
						if(!loopContainer.isEmpty() && loopContainer.peek().equals("loop")) {
							
							loopContainer.pop();
							
						}
						
						if((line.indexOf(":") != -1 || line.equals("")) && !(line.substring(0, line.indexOf(":")).trim().equals(userCommands.get("else_if")) || 

								line.substring(0, line.indexOf(":")).trim().equals(userCommands.get("else"))) && 
								!loopContainer.isEmpty() && loopContainer.peek().equals("if")) {

							loopContainer.pop();
							
						}
						
					}
				
				}
				
				for(int i = 0; i < line.length(); i++) {
					
					if(line.charAt(i) == ':') {
						
						String key = line.substring(0, i).trim();
						String action = line.substring(line.indexOf(key) + key.length() + 1);
						
						boolean actionPerformed = false;
						
						for(HashMap.Entry<String, String> command : userCommands.entrySet()) {
							
							if(key.equals(command.getValue()) && command.getKey().equals("print")) {
								
								line = action;
								Print.print(line, lineNumber);
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("printl")) {
								
								line = action;
								Print.printLine(line, lineNumber);
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("input")) {
								
								//Declare a scanner when input is first taken
								if(!inputUsed) {
									inputUsed = true;
									translatedCode += "\nScanner get = new Scanner(System.in);\n";
								}
								
								//Add a different scanner method depending on the variable data type
								for(Variable variable: FileExecutionTool.userDeclaredVariables) {

									//Don't add a scanner method if variable is not declared
									if(variable.getName().equals(action.trim()) && variable.getDatatype() != null) {

										if(variable.getDatatype().equals("boolean")) {
											translatedCode += String.format("\n%s = get.nextBoolean();\n", variable.getName());
										} else if(variable.getDatatype().equals("int")) {
											translatedCode += String.format("\n%s = get.nextInt();\n", variable.getName());
										} else if(variable.getDatatype().equals("double")) {
											translatedCode += String.format("\n%s = get.nextDouble();\n", variable.getName());
										} else if(variable.getDatatype().equals("String")) {
											translatedCode += String.format("\n%s = get.next();\n", variable.getName());
										}

										break;

									}

								}
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && (command.getKey().equals("if"))) {
								
								loopContainer.push("if");
								
								ControlStructures.initialize(action, command.getKey(), lineNumber);
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && (command.getKey().equals("else_if") || 
									command.getKey().equals("else"))) {
								//System.out.println("here + "  + 1);
								if(!loopContainer.isEmpty() && loopContainer.peek().equals("if")) {
									//System.out.println("here + "  + 2);
									ControlStructures.initialize(action, command.getKey(), lineNumber);
									actionPerformed = true;
									
								}
								
								else {
									
									terminate("Invalid Control Structure(check structure and placement): Line ", lineNumber);
									return;
									
								}
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("loop")) {
								
								Loop.initializeToFile(action, lineNumber);
								loopContainer.add("loop");
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("break")) {
								
								if(loopContainer.contains("loop")) {
									
									translatedCode += "\nbreak;";
									
								} else {
									
									terminate("No Loop to Break Out: Line ", lineNumber);
									return;
									
								}
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("continue")) {
								
								if(loopContainer.contains("loop")) {
									
									translatedCode += "\ncontinue;";
									
								} else {
									
									terminate("No Loop to Break Out: Line ", lineNumber);
									return;
									
								}
								
								actionPerformed = true;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("random")) {
								
								if(action.contains(":")) {
									
									for(Variable var: userDeclaredVariables) {
										
										String actioVariable = action.substring(0, action.indexOf(":")).trim();
										if(var.getName().equals(actioVariable)) {
											
											var.getRandom(action.substring(action.indexOf(":") + 1, action.length()).trim(), lineNumber);
											break;
											
										} else {
											
											terminate("Variable Have to be Initialized First Before Random: Line ", lineNumber);
											return;
											
										}
										
									}
									
									actionPerformed = true;
									
								} else {
									
									terminate("improper Random Format(keyword: variable: randomizer): Line ", lineNumber);
									return;
									
								}
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("parse(int/double)")) {
								
								if(action.contains(":")) {
									
									boolean isFound = false;
									
									for(Variable var: userDeclaredVariables) {
										
										String actioVariable = action.substring(0, action.indexOf(":")).trim();
										if(var.getName().equals(actioVariable)) {
											
											var.parse(action.substring(action.indexOf(":") + 1, action.length()).trim(), lineNumber);
											isFound = true;
											actionPerformed = true;
											break;
											
										}
										
									}
									
									if(!isFound) {
										
										terminate("Variable Have to be Initialized First Before Parsing(keyword: int/double variable: String value): Line ", lineNumber);
										return;
										
									}
									
								} else {
									
									terminate("Improper Parse_Int Format(keyword: int/doublevariable: String value): Line ", lineNumber);
									return;
									
								}
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("to_string")) {
								
								if(action.contains(":")) {
									
									boolean isFound = false;
									
									for(Variable var: userDeclaredVariables) {
										
										String actioVariable = action.substring(0, action.indexOf(":")).trim();
										if(var.getName().equals(actioVariable)) {
											
											var.toStringValue(action.substring(action.indexOf(":") + 1, action.length()).trim(), lineNumber);
											isFound = true;
											actionPerformed = true;
											break;
											
										}
										
									}
									
									if(!isFound) {
										
										terminate("Variable Have to be Initialized First Before To_String(keyword: String variable: value): Line ", lineNumber);
										return;
										
									}
									
								} else {
									
									terminate("Improper To_String Format(keyword: String variable: value): Line ", lineNumber);
									return;
									
								}

							}
							
							if(actionPerformed) {
								break;
							}

							if(actionPerformed) {
								break;
							}
							
						}
						
						if(actionPerformed) {
							
							break;
							
						} else {
							
							terminate("Unknown Keyword: Line ", lineNumber);
							
						}
						
					} else if(line.charAt(i) == '=') {
						
						String variable = line.substring(0, i).trim();
						String value = line.substring(i+1, line.length()).trim();
						
						boolean found = false;
						
						for(Variable var: userDeclaredVariables) {
							
							if(var.getName().equals(variable)) {
								
								found = true;
								var.setValue(value, lineNumber);
								break;
								
							}
							
						}
						
						if(!found) {
							
							userDeclaredVariables.add(new Variable(variable, value, true, lineNumber));
							
						}
						
					} else if(line.charAt(i) == '+' || line.charAt(i) == '-' || line.charAt(i) == '*'
							|| line.charAt(i) == '/') {
						
						//char operator = line.charAt(i);
						String variable = line.substring(0, i).trim();
						String operator = line.substring(i, i + 1).trim();
						String calculation = line.substring(i + 1, line.length()).trim();
						boolean found = false;
						
						for(Variable var: userDeclaredVariables) {
							
							if(var.getName().equals(variable)) {
								
								found = true;
								var.calculate(calculation, operator, lineNumber);
								break;
								
							}
							
						}
						
						if(!found) {
							
							terminate("Invalid Calculation: Line ", lineNumber);
							return;
							
						}
						
						break;
						
					} 
					
					if(!executeSuccessful) {
						return;
					}
					
				}
				
				if(!executeSuccessful) {
					return;
				}
				
			}
			
			translatedCode += "\n}\n";
			
			while(!loopContainer.isEmpty()) {
				translatedCode += "\n}";
				loopContainer.pop();
			}
			
			translatedCode += "\n}";
			
			//Import a scanner if it's needed
			if(inputUsed) {
				
				String scannerImport = "import java.util.Scanner;\n\n" + translatedCode;
				translatedCode = scannerImport;
				
			}
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
		//Open a file dialog and use it to decide where to save the file to
	    FileDialog fileDialog = new FileDialog((Frame) null, "Select Where to Save the File");
	    fileDialog.setMode(FileDialog.SAVE);
	    fileDialog.setVisible(true);
	    String fileLocation = fileDialog.getDirectory() + fileDialog.getFile() + ".java";
	    File jarFile = new File(fileLocation);
	    
	    //Replace the placeholder class name with the one entered by the user
	    String jarRunFile = String.format("JarRunFile%d", State.numExecutions);
	    translatedCode = translatedCode.replaceFirst(Pattern.quote(jarRunFile), fileDialog.getFile());

        //Print the java code to the specified file
        try {
        	
            PrintWriter pr = new PrintWriter(jarFile);
            pr.print(translatedCode);
            pr.close();
            
        } catch (IOException e) {
            System.out.println("Class file was not created");
        }
		
	}
	
	//This method inserts returns the number of tabs required for the next line of code
	//when given the all of the code currently typed into the editor text area
	public static int insertTabs(String codeblock) {
		
		int requiredTabs = 0;
		Scanner input = new Scanner(codeblock);
			
		try {

			String line = "";

			while(input.hasNextLine()) {
				line = input.nextLine();
			}

			int numTabs = 0;
			for(int i = 0; i < line.length(); i++) {
				if(line.charAt(i) == '\t') {
					numTabs++;
				} else {
					break;
				}
			}

			if(!line.trim().equals("") && line.indexOf(":") != -1 &&
					(line.substring(0, line.indexOf(":")).trim().equals(userCommands.get("if")) || 
							line.substring(0, line.indexOf(":")).trim().equals(userCommands.get("else_if")) ||
							line.substring(0, line.indexOf(":")).trim().equals(userCommands.get("else")) ||
							line.substring(0, line.indexOf(":")).trim().equals(userCommands.get("loop")))) {
				numTabs++;

			}
			
			requiredTabs = numTabs;
			
			input.close();
			
		} catch(NoSuchElementException error) {

		}
		
		return requiredTabs;
		
	}
	
	
	public static void terminate(String message, int lineNumber) {
		
		if(executeSuccessful) {
			
			System.out.println(message + "" +lineNumber);
			Editor.highlightLine(lineNumber);
			
		}
		
		executeSuccessful = false;
		
	}
	
}
