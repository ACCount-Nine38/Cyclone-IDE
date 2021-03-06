package display;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import objects.Class;
import objects.Project;
import popup.CodingInCyclonePopup;
import popup.GettingStartedPopup;
import popup.KeywordCustomizationPopup;
import popup.KeywordHelpPopup;
import popup.UtilityCustomizationPopup;
import utils.FileExecutionTool;

//This class is used for creating the main frame of the Cyclone IDE
public class IDEInterface extends State {
	
	// perspectives
	private Console console;
	private Editor editor;
	private ProjectExplorer projectExplorer;
	
	//GUI panel
	private static JPanel GUIPanel = new JPanel(null);
	
	//Constructor method
	public IDEInterface() {
		
		//Read the JDK file path and load the utility settings
		readJDKFilepath();
		loadUtilitySettings();
		
		//Add the project explorer, editor, and console
		addPerspectives();
		
		//Add the menu bar
		addMenuBar();
		
		//Set up the frame
		frameSetup(this, "Cyclone IDE", (int) Perspective.screenWidth, (int) Perspective.screenHeight);
		
		//Warn user before closing the frame while edited tabs are open
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent arg0) {
				
				//If there are edited tabs opened, prompt the user to save them
				if(editor.editedTabsOpen()) {
					
					//Prompt the user to save edited tabs
					int option = JOptionPane.showConfirmDialog(IDEInterface.this, "Would you like to save edited tabs before closing");
					//Yes = 0, No = 1, Cancel = 2, Closing the window = -1
					if(option == 0) { //If the user selects yes, save all tabs and close the program
						editor.saveAllTabs();
						System.exit(0);
					} else if(option == 1) { //If the user selects no, exit without saving the tabs
						System.exit(0);
					} 
				} else { //If there are no edited tabs, exit the program
					System.exit(0);
				}
				
			}
			
		});
		
	}
	
	//This method adds the project explorer, console, and editor to the frame
	private void addPerspectives(){
		
		//Set up the console
		console = new Console();
		GUIPanel.add(console);
		
		//Set up the editor
		editor = new Editor(this);
		GUIPanel.add(editor);
		
		//Set up the project explorer
		projectExplorer = new ProjectExplorer(this);
		GUIPanel.add(projectExplorer);
		
		//Set up the GUI panel
		GUIPanel.setBounds(0, 0, (int)SCREEN_WIDTH, (int)SCREEN_HEIGHT);
		GUIPanel.setBackground(State.utilityColor);
		add(GUIPanel);
		
	}

	// method that sets up a frame
	public static void frameSetup(JFrame frame, String name, int width, int height) {

		// set the name and size of the frame, and now allowing user to resize
		frame.setTitle(name);
		frame.setSize((int)State.SCREEN_WIDTH, (int)State.SCREEN_HEIGHT);
		frame.setResizable(false);

		// disables auto layout, center program, exit frame when program closes
		frame.setLayout(null);
		frame.setFocusable(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set frame to appear on screen
		frame.setVisible(true);

	}
	
	//This method detects when a menu bar option is pressed
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//Create a new project or class
		if(e.getSource() == getNewProjectOption()) { //Create a new project
			createProject();
		} else if(e.getSource() == getNewClassOption()) { //Create a new class
			createClass();
		} else if(e.getSource() == getSaveCurrentTabOption()) { //Save the current tab
			editor.saveCurrentTab();
		} else if (e.getSource() ==  getSaveAllTabsOption()) { //Save all opened tabs
			editor.saveAllTabs();
		} else if (e.getSource() ==  getExportJavaFileOption()) { //Export the project as a .java file
			exportProject();
		} else if (e.getSource() ==  getSetJDKFilepathOption()) { //Set the JDK path
			setJDKFilepath();
		} else if(e.getSource() == getKeywordCustomizationOption()) { //Customize keywords
			this.setEnabled(false);
			new KeywordCustomizationPopup(this);
		} else if(e.getSource() == getUtilityCustomizationOption()) { //Customize utility options
			this.setEnabled(false);
			new UtilityCustomizationPopup(this);
		} else if (e.getSource() ==  getGettingStartedOption()) { //Show starting help screen
			this.setEnabled(false);
			new GettingStartedPopup(this);
		} else if (e.getSource() ==  getCodingInCycloneOption()) { //Show coding help screen
			this.setEnabled(false);
			new CodingInCyclonePopup(this);
		} else if(e.getSource() == getKeywordHelpOption()) {
			this.setEnabled(false);
			new KeywordHelpPopup(this);
		}
		
	}
	
	//This method allows the user to create a project
	private void createProject() {
		
		//Allow user to enter a project name
		String projectName = JOptionPane.showInputDialog("Enter a project name:").trim();
		
		boolean validName = true; //Determines if the project name is valid
		
		//If no project name was entered, it's not a valid name
		if(projectName.isEmpty()) {
			
			validName = false; //Set valid name to false
			
			//Display error message
			JOptionPane.showMessageDialog(null, "A project name was not input","INVALID", JOptionPane.WARNING_MESSAGE);
		}
		
		//Check if the project name is already taken
		for(Project currentProject: projectExplorer.getProjects()) {
			
			if(projectName.equalsIgnoreCase(currentProject.getProjectName())) {
				
				validName = false; //Set valid name to false
				
				//Display error message
				JOptionPane.showMessageDialog(null, "The project name is already taken","INVALID", JOptionPane.WARNING_MESSAGE);
				break;
			}
				
		}
		
		//A filename can't contain any of the following characters: \/:*?"<>|
		if(projectName.contains("\\") || projectName.contains("/") || 
				projectName.contains(":") || projectName.contains("*") || 
				projectName.contains("?") || projectName.contains("\"") || 
				projectName.contains("<") || projectName.contains(">") || 
				projectName.contains("|")) {
			JOptionPane.showMessageDialog(null, "A filename can't contain any of the following characters:\n\\/:*?\"<>|","INVALID", JOptionPane.WARNING_MESSAGE);
			validName = false;  //Set valid name to false
		}
		
		//If the project name is not taken, create the folder
		if(validName) {
			
			//Create the folder
			File file = new File(String.format("projects/%s", projectName));
			boolean directoryCreated = file.mkdir();
			
			//Add the project to the project explorer if it's successfully created
			if(directoryCreated) {
				projectExplorer.addNewProject(projectName); 
			}
			
		}
		
	}
	
	//This method is used to create a class
    public void createClass(){
    	
    	//Select the project
        String selectedProject = (String) JOptionPane.showInputDialog(null, "Choose a project to create a class in:", "Menu", JOptionPane.PLAIN_MESSAGE, null, 
        		projectExplorer.getProjectDirectories().toArray(), projectExplorer.getProjectDirectories().get(0));
        
        //Return from the method if nothing was entered
        if(selectedProject == null)
        	return;
        
        //Enter a class name
		String className = JOptionPane.showInputDialog("Enter a class name:").trim();
		
		boolean validName = true; //Determines if the class name is valid
		
		//Check if a class name was entered
		if(className.isEmpty()) {
			
			validName = false; //Set valid name to false
			
			//Display error message
			JOptionPane.showMessageDialog(null, "A class name was not input","INVALID", JOptionPane.WARNING_MESSAGE);
		}
		
		//Check if the class name is taken
		Project project = null; //Stores the current project
		
		//Initialize the variable of the current project
		for(Project currentProject: projectExplorer.getProjects()) {
			if(selectedProject.equals(currentProject.getProjectName())) {
				project = currentProject;
				break;
			}
		}
		
		//Check if theclass name is taken
		for(File currentClass: project.getFilepath().listFiles()) {
			
			if(className.equalsIgnoreCase(currentClass.getName())) {
				
				validName = false; //Set valid name to false
				
				//Display error message
				JOptionPane.showMessageDialog(null, "The class name is already taken","INVALID", JOptionPane.WARNING_MESSAGE);
				break;
			}
			
		}
		
		//A filename can't contain any of the following characters: \/:*?"<>|
		if(className.contains("\\") || className.contains("/") || 
				className.contains(":") || className.contains("*") || 
				className.contains("?") || className.contains("\"") || 
				className.contains("<") || className.contains(">") || 
				className.contains("|") || className.contains(" ")) {
			JOptionPane.showMessageDialog(null, "A class name can't contain any of the following characters:\n\\/:*?\"<>| or spaces","INVALID", JOptionPane.WARNING_MESSAGE);
			validName = false; //Set valid name to false
		}
		
		//If the project name is not taken, create the file
		if(validName) {
			
			//Class file
			File file = new File(String.format("projects/%s/%s", project.getProjectName(), className));
			
			try {
				file.createNewFile(); //Create the class file
				projectExplorer.addNewClass(selectedProject, className); //Add the new class to its project
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
        
    }
    
	//This method is used to create a class given the project name
    //This method is called from the project explorer
    public void createClass(String projectName){
    	
    	//Get the index of the selected project
    	int index = 0;
    	for(int i = 0; i < projectExplorer.getProjectDirectories().size(); i++) {
    		if(projectExplorer.getProjectDirectories().get(i).equals(projectName)) {
    			index = i;
    			break;
    		}
    	}
    	
    	//Select the project
        String selectedProject = (String) JOptionPane.showInputDialog(null, "Choose a project to create a class in:", "Menu", JOptionPane.PLAIN_MESSAGE, null, 
        		projectExplorer.getProjectDirectories().toArray(), projectExplorer.getProjectDirectories().get(index));
        
        //Return from the method if nothing was entered
        if(selectedProject == null)
        	return;
        
        //Enter a class name
		String className = JOptionPane.showInputDialog("Enter a class name:").trim();
		
		boolean validName = true; //Determines if the project name is valid
		
		//Check if a class name was entered
		if(className.isEmpty()) {
			validName = false;
			//Display error message
			JOptionPane.showMessageDialog(null, "A project name was not input","INVALID", JOptionPane.WARNING_MESSAGE);
		}
		
		//Check if the class name is taken
		Project project = null; //Stores the current project
		
		//Initialize the variable of the current project
		for(Project currentProject: projectExplorer.getProjects()) {
			if(selectedProject.equals(currentProject.getProjectName())) {
				project = currentProject;
				break;
			}
		}
		
		//Check if the class name is already taken
		for(File currentClass: project.getFilepath().listFiles()) {
			
			if(className.equalsIgnoreCase(currentClass.getName())) {
				validName = false;
				//Display error message
				JOptionPane.showMessageDialog(null, "The class name is already taken","INVALID", JOptionPane.WARNING_MESSAGE);
				break;
			}
			
		}
		
		//A filename can't contain any of the following characters: \/:*?"<>|
		if(className.contains("\\") || className.contains("/") || 
				className.contains(":") || className.contains("*") || 
				className.contains("?") || className.contains("\"") || 
				className.contains("<") || className.contains(">") || 
				className.contains("|")) {
			JOptionPane.showMessageDialog(null, "A filename can't contain any of the following characters:\n\\/:*?\"<>|","INVALID", JOptionPane.WARNING_MESSAGE);
			validName = false;
		}
		
		//If the project name is not taken, create the folder
		if(validName) {
			
			//Class file
			File file = new File(String.format("projects/%s/%s", project.getProjectName(), className));

			try {
				file.createNewFile(); //Create the new class file
				projectExplorer.addNewClass(selectedProject, className); //Add the new class to its project
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
        
    }
    
    //This method resets the color of GUI components when the theme is changed between light
    //and dark in the utility settings
    public void resetColor() {
    	
    	//Set the background colour
    	GUIPanel.setBackground(State.utilityColor);
    	
    	//Set the console colour
    	Console.consoleTextArea.setBackground(State.utilityColor);
    	Console.consoleTextArea.setForeground(State.textColor);
    	console.setBackground(State.utilityColor);
    	
    	//Set the project explorer colour
    	projectExplorer.setBackground(State.utilityColor);
    	projectExplorer.getProjectExplorerPanel().setBackground(State.utilityColor);
    	
    	//Set the editor colour
    	editor.getTabbedPane().setBackground(State.utilityColor);
    	editor.setBackground(State.utilityColor);
    	editor.repaint();
    	
    	//Set the colour of the components of each project
    	for(Project currentProject: projectExplorer.getProjects()) {
    		
    		//Set the colour of the project button
    		currentProject.getProjectButton().setBackground(State.utilityColor);
    		currentProject.getProjectButton().setForeground(State.textColor);
    		currentProject.getProjectButton().repaint();
    		
    		//Set the project class file panel
    		currentProject.getFilePanel().setBackground(State.utilityColor);
    		currentProject.getFilePanel().setForeground(State.textColor);
    		currentProject.getFilePanel().repaint();
    		
    		//Set the colour of the project panel
    		currentProject.getProjectPanel().setBackground(State.utilityColor);
    		currentProject.getProjectPanel().setForeground(State.textColor);
    		currentProject.getProjectPanel().repaint();
    		
    		//Adjust coulour settings of each of the project's classes
    		//Set the colour of each class button and the class' text area
    		for(Class currentClass: currentProject.getFileButtons()) {
    			currentClass.setBackground(State.utilityColor);
    			currentClass.setForeground(State.textColor);
    			currentClass.repaint();
    			currentClass.getEditorTextArea().setCaretColor(State.textColor);
    			currentClass.getEditorTextArea().setBackground(State.utilityColor);
    			currentClass.getEditorTextArea().setForeground(State.textColor);
    			currentClass.getEditorTextArea().repaint();
    		}
    		
    	}
    	
    	//Repaint components
    	GUIPanel.repaint();
    	console.repaint();
    	projectExplorer.repaint();
    	editor.getTabbedPane().repaint();
    	Console.consoleTextArea.repaint();
    	projectExplorer.getProjectExplorerPanel().repaint();
    	
    }
    
    //This method loads the font settings and the dark and light mode settings
    public void loadUtilitySettings() {
    	
		try {
			
			//Read from the utility settings file
			Scanner input = new Scanner(new File("settings/fonts"));
			
			//Set the fonts
			Class.editorFont = new Font(input.next(), Font.PLAIN, input.nextInt());
			Class.editorTabSize = input.nextInt();
			Console.consoleFont = new Font(input.next(), Font.PLAIN, input.nextInt());
			Console.consoleTabSize = input.nextInt();
			String theme = input.next();
			
			//Set the light or dark theme
			if(theme.equals("light")) {
				State.darkTheme = false;
				State.utilityColor = new Color(250, 250, 250);
				State.textColor = Color.black;
			} else if(theme.equals("dark")) {
				State.darkTheme = true;
				State.utilityColor = new Color(30, 30, 30);
				State.textColor = Color.white;
			}
			
			input.close();
			
		} catch(FileNotFoundException e) {
			
		}
    	
    }
    
	//This allows the user to export the project as a .java file
	private void exportProject() {
		
		//Return if there are no open tabs
		if(editor.getTabbedPane().getTabCount() == 0) {
			return;
		}
		
		//Set the current file based on the selected tab
		if(editor.getTabbedPane().getSelectedIndex() != -1) {
			
			for(Project currentProject: projectExplorer.getProjects()) {
				
				for(Class currentClass: currentProject.getFileButtons()) {
					
					if(editor.getTabbedPane().getSelectedComponent().equals(currentClass.getEditorTextAreaScroll())) {
						
						//Save the current tab
						editor.saveCurrentTab();
						
						//Set the current file
						currentFile = new File(String.format("projects/%s/%s", 
								currentClass.getProjectName(), currentClass.getClassName()));
						
					}
					
				}
			
			}
			
		}
		
		//Export the current class
		FileExecutionTool.exportFile(currentFile);

	}
    
	//This allows the user to export the project as a .java file
	private void setJDKFilepath() {
		
		//Find the current file path for the JDK
		String currentFilepath = "";
		
		try {
			//Read the current JDK file path from the file
			Scanner input = new Scanner(new File("settings/jdkFilepath"));
			if(input.hasNextLine())
				currentFilepath = input.nextLine();
			input.close();
		} catch (FileNotFoundException e) {
			System.out.println("Save Failed");
		}
		
		//Open a file chooser and use it to set the JDK folder file path
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setDialogTitle(String.format("Select the JDK folder, jdk1.8.0_181 (Current filepath: %s)", currentFilepath));
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        //Set the file path when the user clicks to confirm it
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
        	JOptionPane.showMessageDialog(this, String.format("JDK Filepath set to: %s", fileChooser.getSelectedFile().getAbsolutePath()));
        }
        
        //Check the selected file path
        String file = fileChooser.getSelectedFile().getAbsolutePath();
	    File filepath = new File(file);
	    
	    //Make sure that the file path is a directory
		if(filepath.isDirectory()) {
			
			try {
				
				//Save the selected file path to a file
				PrintWriter pr = new PrintWriter(new File("settings/jdkFilepath"));
				pr.println(file);
				pr.close();
				
				//Save JDK file path
		        JDKFilepath = file;

			} catch (FileNotFoundException e) {
				System.out.println("Save Failed");
			}

		}
	    
	}
	
	//This method reads the JDK filepath when the program is run
	private void readJDKFilepath() {
		
		try {
			//Write data to a file
			Scanner input = new Scanner(new File("settings/jdkFilepath"));
			if(input.hasNextLine())
				JDKFilepath = input.nextLine();
			input.close();
		} catch (FileNotFoundException e) {
			System.out.println("Save Failed");
		}
		
	}
	
	//Getters and setters
	public Console getConsole() {
		return console;
	}

	public void setConsole(Console console) {
		this.console = console;
	}

	public Editor getEditor() {
		return editor;
	}

	public void setEditor(Editor editor) {
		this.editor = editor;
	}

	public ProjectExplorer getProjectExplorer() {
		return projectExplorer;
	}

	public void setProjectExplorer(ProjectExplorer projectExplorer) {
		this.projectExplorer = projectExplorer;
	}

	public static JPanel getGUIPanel() {
		return GUIPanel;
	}
    
}

