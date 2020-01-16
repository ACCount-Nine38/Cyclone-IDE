package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import commands.ControlStructures;
import commands.For;
import commands.Function;
import commands.Input;
import commands.Print;
import commands.Variable;
import commands.While;

import javax.tools.JavaCompiler.CompilationTask;

import display.Console;
import display.State;

public class FileExecutionTool {
	
	public static HashMap<String, String> userCommands = new HashMap<String, String>();
	public static HashMap<String, String> userDatatypes = new HashMap<String, String>();
	
	public static ArrayList<Variable> userDeclaredVariables = new ArrayList<Variable>();
	public static ArrayList<Method> userDeclaredReturnMethods = new ArrayList<Method>();
	
	public static boolean executeSuccessful;
	
	public static String translatedCode = "";
	
	public static PrintWriter printer;
	
	public static int previousTabNumber = 0, currentTabNumber = 0;
	
	public FileExecutionTool() {
		
		updateCommands();
		updateDatatypes();
		resetCode();
		
		try {
			printer = new PrintWriter("src/JarRunFile.java");
			//printer = new PrintWriter(
			//		new BufferedWriter(new FileWriter("src/JarRunFile.java", true)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void resetCode() {
		
		previousTabNumber = 0;
		currentTabNumber = 0;
		translatedCode = "public class JarRunFile { ";
		
	}
	
	public static void updateCommands() {
		
		userCommands.clear();
		
		try {
			
			Scanner command = new Scanner(new File("commands/userCommands"));
			
			while(command.hasNext()) {
				
				userCommands.put(command.next(), command.next());
				
			}
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static void updateDatatypes() {
		
		userDatatypes.clear();
		
		try {
			
			Scanner command = new Scanner(new File("commands/datatypes"));
			
			while(command.hasNext()) {
				
				userCommands.put(command.next(), command.next());
				
			}
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static void executeFile(File file) {

		Console.consoleTextArea.setText("");

		resetCode();
		Console.consoleTextArea.setText("");
		
		userDeclaredVariables.clear();
		userDeclaredReturnMethods.clear();
		
		try {
			
			Scanner input = new Scanner(file);
			
			executeSuccessful = true;
			
			while(input.hasNext()) {
				
				if(!executeSuccessful) {
					return;
				}
				
				previousTabNumber = currentTabNumber;
				String line = input.nextLine();
				
				if(line.trim().length() > 0) {
					
					int numTabs = 0;
					for(int i = 0; i < line.length(); i++) {
						if(line.charAt(i) == '\t') {
							numTabs++;
						}
					}
					
					currentTabNumber = numTabs;
					
					if(currentTabNumber < previousTabNumber) {
						translatedCode += "\n}\n";
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
								Print.print(line);
								
								actionPerformed = true;
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("printl")) {
								
								line = action;
								Print.printLine(line);
								
								actionPerformed = true;
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("input")) {
								
								String inputVariable = Input.validateText(action);
								Input.readVariable(inputVariable);
								
								actionPerformed = true;
								break;
								
							} else if(key.equals(command.getValue()) && (command.getKey().equals("if") || 
									command.getKey().equals("else_if") || 
									command.getKey().equals("else"))) {
								
								ControlStructures.initialize(action, command.getKey());
								actionPerformed = true;
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("for")) {
								
								For.initialize(action);
								actionPerformed = true;
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("while")) {
								
								While.initialize(action);
								actionPerformed = true;
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("break")) {
								
								translatedCode += "\nbreak;";
								actionPerformed = true;
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("main")) {
								
								Function.declareMain();
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("int")) {
								
								Function.declareMain();
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("doub")) {
								
								Function.declareMain();
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("bool")) {
							
								Function.declareMain();
								break;
								
							} else if(key.equals(command.getValue()) && command.getKey().equals("str")) {
								
								Function.declareMain();
								break;
								
							}
							
						}
						
						if(actionPerformed) {
							break;
						}
						
					} else if(line.charAt(i) == '=') {
						
						String variable = line.substring(0, i).trim();
						String value = line.substring(i+1, line.length()).trim();
						
						boolean found = false;
						
						for(Variable var: userDeclaredVariables) {
							
							if(var.getName().equals(variable)) {
								
								found = true;
								
								var.setValue(value);
								
								break;
								
							}
							
						}
						
						if(!found) {
							
							userDeclaredVariables.add(new Variable(variable, value, true));
							
						}
						
					} else if(line.charAt(i) == '+' || line.charAt(i) == '-' || line.charAt(i) == '*'
							|| line.charAt(i) == '/' || line.charAt(i) == '%') {
						
						//char operator = line.charAt(i);
						String variable = line.substring(0, i).trim();
						String calculation = line.substring(i, line.length());
						//String value = line.substring(i+1, line.length()).trim();
						
						Variable operatingVariable = null;
						
						for(Variable var: userDeclaredVariables) {
							
							if(var.getName().equals(variable)) {
								
								operatingVariable = var;
								
								break;
								
							}
							
						}
						/*
						if(operatingVariable == null) {
							
							executeSuccessful = false;
							terminate("Variable Not Found Exception");
							
							return;
							
						}
						
						boolean valueFound = false;
						
						for(Variable var: userDeclaredVariables) {
							
							if(var.getName().equals(value)) {
								
								valueFound = true;
								
								operatingVariable.calculate(operator, var.getValue());
								
								break;
								
							}
							
						}
						*/
						operatingVariable.calculate(calculation);
						break;
						
					} 
					/*
					else if(line.charAt(i) == '{') {
						
						String methodName = line.substring(0, i).trim();
						
						if(methodName.equals("main")) {
							
							Function.declareMain();
							break;
							
						} 
						
					} else if(line.charAt(i) == '}') {
						
						translatedCode += "\n}";
						
					}
					*/
				}
				
			}
			
			translatedCode += "\n}\n";
			
			/*
			if(executeSuccessful) {
				
				StyledDocument doc = Console.consoleTextArea.getStyledDocument();
				Style greenStyle = Console.consoleTextArea.addStyle("Green", null);
				Style blackStyle = Console.consoleTextArea.addStyle("Black", null);
			    StyleConstants.setForeground(greenStyle, Color.GREEN);

			    // Inherits from "Red"; makes text red and underlined
			    StyleConstants.setUnderline(greenStyle, true);
			    
			    try {
			    	
			    	if(!Console.consoleTextArea.getText().isEmpty()) {
			    		
			    		doc.insertString(doc.getLength(), "\n", greenStyle);
			    		 
			    	} 
			    	
			    	doc.insertString(doc.getLength(), "BUILD SUCCESSFUL", greenStyle);
					StyleConstants.setForeground(blackStyle, Color.black);
					StyleConstants.setUnderline(blackStyle, false);
					doc.insertString(doc.getLength(), " ", blackStyle);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			    
			} 
			*/
			
			translatedCode += "\n\npublic static void execute() {\n"+ 
					"main(null);\n}\n}";
			
			//printer.println(translatedCode);
			//System.out.println(translatedCode);
			Console.consoleTextArea.setText(translatedCode);
			
			//JarRunFile.execute();
			
			//printer.close();
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
	
		//HOW TO FIND JDK LOCATION: https://stackoverflow.com/questions/4681090/how-do-i-find-where-jdk-is-installed-on-my-windows-machine
		//System.setProperty("java.home", "C:\\Program Files\\Java\\jdk1.8.0_181");
		System.setProperty("java.home", State.JDKFilepath);
		
		PrintStream printStream = new PrintStream(new CustomOutputStream(Console.consoleTextArea));
        System.setOut(printStream);
        System.setErr(printStream);
        File jarFile = new File("src/JarRunFile.java");

        try {
        	
            PrintWriter pr = new PrintWriter(jarFile);
            
            pr.print(translatedCode);
            
            pr.close();
            
        } catch (IOException e) {
            System.out.println("Class file was not created");
            e.printStackTrace();
        }
        
        //Set to use JDK
//        String jdkReplace = "\\s*\\bsrc\\\\JarRunFile.java\\b\\s*";
//        String jdkPath = jarFile.getAbsolutePath().replaceAll(jdkReplace, "jdk\\\\jdk1.8.0_181");
//        System.setProperty("java.home", jdkPath);
        System.out.println(System.getProperty("java.home")); //Java home path
        
        System.out.println(jarFile.getAbsolutePath());
        
        String regex = "\\s*\\bsrc\\\\JarRunFile.java\\b\\s*";
        String binPath = jarFile.getAbsolutePath().replaceAll(regex, "bin");
        
        System.out.println(binPath);
        
		//SOURCE: https://stackoverflow.com/questions/2028193/specify-output-path-for-dynamic-compilation/7532171
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager sjfm = javaCompiler.getStandardFileManager(null, null, null); 

		String[] options = new String[] { "-d", binPath };
		File[] javaFiles = new File[] { new File("src/JarRunFile.java") };

		CompilationTask compilationTask = javaCompiler.getTask(null, null, null,
		        Arrays.asList(options),
		        null,
		        sjfm.getJavaFileObjects(javaFiles)
		);
		compilationTask.call();
		
		try {
			
			String[] params = null;
			Class<?> cls = Class.forName("JarRunFile");

			Method method;
			try {
				System.out.println("Executing JarRunFile.main");
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
		
	}
	
	public static int insertTabs(String codeblock) {
		
		int requiredTabs = 0;
		Scanner input = new Scanner(codeblock);
		
		while(true) {
			
			try {
				
				String line = input.nextLine();
				
				if(line.length() > 1) {
					
					for(int i = 1; i < line.length(); i++) {
						if(line.charAt(i) == '{' && line.charAt(i) != '\\') {
							
							requiredTabs++;
							
						} else if(line.charAt(i) == '}' && line.charAt(i) != '\\') {
							
							requiredTabs--;
							
						} 
					}
					
				} else {
					
					if(line.equals("}") && requiredTabs > 0) {
						
						requiredTabs--;
						
					}
					
				}
				
			} catch(NoSuchElementException error) {
				
				break;
				
			}
			
		}
		
		return requiredTabs;
		
	}
	
	public void splitMethod() {
		
		
		
	}
	
	public static void terminate(String message) {
		/*
		StyledDocument doc = Console.consoleTextArea.getStyledDocument();
		Style greenStyle = Console.consoleTextArea.addStyle("Red", null);
		Style blackStyle = Console.consoleTextArea.addStyle("Black", null);
	    StyleConstants.setForeground(greenStyle, Color.RED);

	    // Inherits from "Red"; makes text red and underlined
	    StyleConstants.setUnderline(greenStyle, true);
	    
	    try {
	    	if(!Console.consoleTextArea.getText().isEmpty()) {
	    		
	    		doc.insertString(doc.getLength(), "\n", greenStyle);
	    		
	    	} 
			doc.insertString(doc.getLength(), message + "\nBUILD FAILED", greenStyle);
			StyleConstants.setForeground(blackStyle, Color.black);
			StyleConstants.setUnderline(blackStyle, false);
			doc.insertString(doc.getLength(), " ", blackStyle);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		*/
	}
	
	/*
	 * JOptionPane.showMessageDialog(null,
						"File Creation Failed! \n\n"
								+ "click 'ok' to continue...",
						"FAILURE", JOptionPane.WARNING_MESSAGE);
				
				return;
	 */
	
}
