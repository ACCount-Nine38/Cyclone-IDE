package display;

import java.awt.Color;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import assets.Images;
import objects.Class;
import objects.Project;
import utils.FileExecutionTool;

/*
 * The parent class of all states, top of the class hierarchy
 * contains default information to be used for other states and abstract methods
 */
public class State extends JFrame implements ActionListener {

	// screen dimension variables
	public static final double SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	public static final double SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	
	// global variables
	public static boolean darkTheme = false;
	public static Color utilityColor = new Color(250, 250, 250);
	public static Color textColor = Color.black;
	public static File currentFile = new File("tabs/testFile");
	public static String JDKFilepath;
	public static int numExecutions = 0;
	
	// menu components
	private JMenuBar menuBar = new JMenuBar();
	private JMenu fileMenu = new JMenu("File");
	private JMenuItem newProjectOption = new JMenuItem("New Project");
	private JMenuItem newClassOption = new JMenuItem("New Class");
	private JMenuItem saveCurrentTabOption = new JMenuItem("Save Current Tab");
	private JMenuItem saveAllTabsOption = new JMenuItem("Save All Tabs");
	private JMenuItem setJDKFilepathOption = new JMenuItem("Set JDK Filepath");
	private JMenuItem exportJavaFileOption = new JMenuItem("Export as .java file");
	private JMenuItem exitOption = new JMenuItem("Exit IDE");
	private JMenu editMenu = new JMenu("Edit");
	private JMenuItem keywordCustomizationOption = new JMenuItem("Customize Keywords");
	private JMenuItem utilityCustomizationOption = new JMenuItem("Utility Customizations");
	private JMenu runMenu = new JMenu("Run");
	private JMenuItem runOption = new JMenuItem("Run Project");
	private JMenu helpMenu = new JMenu("Help");
	private JMenuItem gettingStartedOption = new JMenuItem("Getting Started");
	private JMenuItem codingInCycloneOption = new JMenuItem("Coding in Cyclone");
	private JMenuItem keywordHelpOption = new JMenuItem("Keywords");
	
	// constructor of the State class initializes the images, frame, and enables the IDE to execute file
	public State() {

		new Images();
		new FileExecutionTool();
		
		customCursor();

	}

	// method that changes the cursor icon
	private void customCursor() {

		// using the java TookKit to change cursors
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		// load an image using ToolKit
		Image mouse = toolkit.getImage("images/cursor.png").getScaledInstance(25, 40, 0);

		// set the cursor icon giving a new image, point, and name
		setCursor(toolkit.createCustomCursor(mouse, new Point(0, 0), "Custom Cursor"));

	}
	
	// method that adds the menu bar
	public void addMenuBar() {
		// create a new JMenuBar item that stores different menus
		setJMenuBar(menuBar);

		// create a new menu called control and add it to the menu bar
		menuBar.add(fileMenu);
		
		//create options to create new project or class
		fileMenu.add(newProjectOption);
		newProjectOption.addActionListener(this);
		fileMenu.add(newClassOption);
		newClassOption.addActionListener(this);
		
		// creating the exit option under the control menu
		// add an action listener for button actions when clicked
		exitOption.addActionListener(new ActionListener() {

			// method handles the current button's actions
			@Override
			public void actionPerformed(ActionEvent e) {
				
				System.exit(1);

			}

		});
		
		// add options to file menu
		fileMenu.add(saveCurrentTabOption);
		saveCurrentTabOption.addActionListener(this);
		fileMenu.add(saveAllTabsOption);
		saveAllTabsOption.addActionListener(this);
		fileMenu.add(exportJavaFileOption);
		exportJavaFileOption.addActionListener(this);
		fileMenu.add(setJDKFilepathOption);
		setJDKFilepathOption.addActionListener(this);
		fileMenu.add(exitOption);

		// create a new menu for settings
		menuBar.add(editMenu);
		
		editMenu.add(keywordCustomizationOption);
		keywordCustomizationOption.addActionListener(this);
		editMenu.add(utilityCustomizationOption);
		utilityCustomizationOption.addActionListener(this);
		
		// create a new menu to run the project
		menuBar.add(runMenu);

		// creating the exit option under the control menu
		// add an action listener for button actions when clicked
		runOption.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				//If there are no tabs opened in the editor, return
				if(State.this instanceof IDEInterface && 
						((IDEInterface)State.this).getEditor().getTabbedPane().getSelectedIndex() == -1) {
					return;
				}
				
				//Set the current file based on the selected tab
				if(State.this instanceof IDEInterface) {
					
					//Find the current tab by looping through each project and class
					for(Project currentProject: ((IDEInterface)State.this).getProjectExplorer().getProjects()) {
						
						for(Class currentClass: currentProject.getFileButtons()) {
							
							//If the current tab is found, save the current tab and set the current file
							if(((IDEInterface)State.this).getEditor().getTabbedPane().getSelectedComponent().equals(currentClass.getEditorTextAreaScroll())) {
								
								//Save the current tab
								((IDEInterface)State.this).getEditor().saveCurrentTab();
								
								//Set the current file
								currentFile = new File(String.format("projects/%s/%s", 
										currentClass.getProjectName(), currentClass.getClassName()));
								
							}
							
						}
					
					}
					
				}
				
				// runs the current project
				FileExecutionTool.executeFile(currentFile);

			}

		});
		
		runMenu.add(runOption); // add run option

		// create a new menu to show help commands
		menuBar.add(helpMenu);
		helpMenu.add(gettingStartedOption);
		gettingStartedOption.addActionListener(this);
		helpMenu.add(codingInCycloneOption);
		codingInCycloneOption.addActionListener(this);
		helpMenu.add(keywordHelpOption);
		keywordHelpOption.addActionListener(this);
		
	}

	// getters and setters
	public JMenu getFileMenu() {
		return fileMenu;
	}

	public void setFileMenu(JMenu fileMenu) {
		this.fileMenu = fileMenu;
	}

	public JMenuItem getNewProjectOption() {
		return newProjectOption;
	}

	public void setNewProjectOption(JMenuItem newProjectOption) {
		this.newProjectOption = newProjectOption;
	}

	public JMenuItem getNewClassOption() {
		return newClassOption;
	}

	public void setNewClassOption(JMenuItem newClassOption) {
		this.newClassOption = newClassOption;
	}

	public JMenuItem getSaveCurrentTabOption() {
		return saveCurrentTabOption;
	}

	public void setSaveCurrentTabOption(JMenuItem saveCurrentTabOption) {
		this.saveCurrentTabOption = saveCurrentTabOption;
	}

	public JMenuItem getSaveAllTabsOption() {
		return saveAllTabsOption;
	}

	public void setSaveAllTabsOption(JMenuItem saveAllTabsOption) {
		this.saveAllTabsOption = saveAllTabsOption;
	}

	public JMenuItem getExportJavaFileOption() {
		return exportJavaFileOption;
	}

	public void setExportJavaFileOption(JMenuItem exportJavaFileOption) {
		this.exportJavaFileOption = exportJavaFileOption;
	}

	public JMenuItem getSetJDKFilepathOption() {
		return setJDKFilepathOption;
	}

	public void setSetJDKFilepathOption(JMenuItem setJDKFilepathOption) {
		this.setJDKFilepathOption = setJDKFilepathOption;
	}

	public JMenuItem getExitOption() {
		return exitOption;
	}

	public void setExitOption(JMenuItem exitOption) {
		this.exitOption = exitOption;
	}

	public JMenu getEditMenu() {
		return editMenu;
	}

	public void setEditMenu(JMenu editMenu) {
		this.editMenu = editMenu;
	}

	public JMenuItem getKeywordCustomizationOption() {
		return keywordCustomizationOption;
	}

	public void setKeywordCustomizationOption(JMenuItem keywordCustomizationOption) {
		this.keywordCustomizationOption = keywordCustomizationOption;
	}
	
	public JMenuItem getUtilityCustomizationOption() {
		return utilityCustomizationOption;
	}

	public void setUtilityCustomizationOption(JMenuItem utilityCustomizationOption) {
		this.utilityCustomizationOption = utilityCustomizationOption;
	}

	public JMenu getRunMenu() {
		return runMenu;
	}

	public void setRunMenu(JMenu runMenu) {
		this.runMenu = runMenu;
	}

	public JMenuItem getRunOption() {
		return runOption;
	}

	public void setRunOption(JMenuItem runOption) {
		this.runOption = runOption;
	}

	public JMenu getHelpMenu() {
		return helpMenu;
	}

	public void setHelpMenu(JMenu helpMenu) {
		this.helpMenu = helpMenu;
	}

	public JMenuItem getGettingStartedOption() {
		return gettingStartedOption;
	}

	public void setGettingStartedOption(JMenuItem gettingStartedOption) {
		this.gettingStartedOption = gettingStartedOption;
	}

	public JMenuItem getCodingInCycloneOption() {
		return codingInCycloneOption;
	}

	public void setCodingInCycloneOption(JMenuItem codingInCycloneOption) {
		this.codingInCycloneOption = codingInCycloneOption;
	}

	public JMenuItem getKeywordHelpOption() {
		return keywordHelpOption;
	}

	public void setKeywordHelpOption(JMenuItem keywordHelpOption) {
		this.keywordHelpOption = keywordHelpOption;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}

}