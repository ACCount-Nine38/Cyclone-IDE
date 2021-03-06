package objects;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import assets.Images;
import display.Perspective;
import display.ProjectExplorer;
import display.State;

//This class is used for creating project objects which contain components to be 
//used in the project explorer
public class Project {
	
	//Dimensions
	private static int width = (int) (Perspective.screenWidth/4 - 25);
	private static int buttonHeight = 50;
	
	//Each project has its own main panel - Box Layout
	//This panel will contain:
	//	-Button that when pressed reveals project files
	//	-Project file buttons that appear and disappear when project file is clicked
	private JPanel projectPanel = new JPanel();
	private JButton projectButton = new JButton(Images.folderImage);
	private JPanel filePanel = new JPanel(); //Within the main panel and stores buttons for each project file
	private ArrayList<Class> fileButtons = new ArrayList<Class>(); //Contains buttons for each file within the project
	
	//Project name and file variables
	private String projectName;
	private File filepath; 
	private boolean open; //Determines whether the classes are revealed within the project explorer
	
	//Constructor method
	public Project(String projectName) {
		
		//Set up the variables
		this.projectName = projectName;
		this.filepath = new File(String.format("projects/%s", projectName));
		open = true;
		
		//Set up the components
		setupJComponents();
		setupFileButtons();
		
		//Collapse the project
		collapse();
		
	}
	
	//Empty constructor method
	public Project() {
	}
	
	//This method sets up the panels and project button
	private void setupJComponents() {
		
		//Setup the main panel
		projectPanel.setLayout(new BoxLayout(projectPanel, BoxLayout.Y_AXIS));
		projectPanel.setBounds(0, 0, width, (filepath.listFiles().length + 1) * buttonHeight);
		projectPanel.setMaximumSize(projectPanel.getSize());
		projectPanel.setMinimumSize(projectPanel.getSize());
		projectPanel.setPreferredSize(projectPanel.getSize());
		projectPanel.setBackground(Color.white);
		
		//Setup the folder button
		projectButton.setSize(width, buttonHeight);
		projectButton.setMaximumSize(projectButton.getSize());
		projectButton.setMinimumSize(projectButton.getSize());
		projectButton.setPreferredSize(projectButton.getSize());
		projectButton.setBackground(State.utilityColor);
		projectButton.setForeground(State.textColor);
		projectButton.setOpaque(true);
		projectButton.setText(projectName);
		projectButton.setContentAreaFilled(false);
		projectButton.setBorderPainted(false);
		projectButton.setHorizontalAlignment(SwingConstants.LEFT);
		projectButton.addMouseListener(new MouseAdapter() { //Open a popup when the project button is right clicked

			public void mouseClicked(MouseEvent e) {
				
				if(e.getButton() == MouseEvent.BUTTON3) {
					//Create the pop up menu
					ProjectExplorer.projectPopup(Project.this);
					
				}
				
			}
			
		});
		projectPanel.add(projectButton);
		
		//Setup the file panel
		filePanel.setLayout(null);
		filePanel.setBackground(State.utilityColor);
		filePanel.setOpaque(true);
		filePanel.setBounds(0, 50, width, filepath.listFiles().length * buttonHeight);
		projectPanel.add(filePanel);
		
	}
	
	//This method sets up the buttons used to access individual files within the project
	public void setupFileButtons() {
		
		//Clear the panel and button array list
		filePanel.removeAll();
		fileButtons.clear();
		
		//Set the dimensions of the project panel
		projectPanel.setBounds(0, 0, width, (filepath.listFiles().length + 1) * buttonHeight);
		projectPanel.setMaximumSize(projectPanel.getSize());
		projectPanel.setMinimumSize(projectPanel.getSize());
		projectPanel.setPreferredSize(projectPanel.getSize());
		projectPanel.setBackground(State.utilityColor);
		projectPanel.setForeground(State.textColor);
		
		//Set the bounds of the file panel
		filePanel.setBounds(0, 50, width, filepath.listFiles().length * buttonHeight); 
		
		//Loop through the project directory array and create a file button and tab for each directory
		for(int i = 0; i < filepath.listFiles().length; i++) {
			
			//Setup the file button
			Class fileButton = new Class(projectName, filepath.listFiles()[i].getName());
			fileButton.setBounds(width / 10, i * buttonHeight, width * 9 / 10, buttonHeight);
			fileButton.setMaximumSize(fileButton.getSize());
			fileButton.setMinimumSize(fileButton.getSize());
			fileButton.setPreferredSize(fileButton.getSize());
			fileButton.setBackground(State.utilityColor);
			fileButton.setForeground(State.textColor);
			filePanel.add(fileButton);
			fileButtons.add(fileButton);
			
		}
		
		//Set panel dimensions if project is collapsed
		if(!open) {
			
			//Set the project panel size
			projectPanel.setSize(width, buttonHeight);
			projectPanel.setMaximumSize(projectPanel.getSize());
			projectPanel.setMinimumSize(projectPanel.getSize());
			projectPanel.setPreferredSize(projectPanel.getSize());
			
			//Set the file panel size
			filePanel.setSize(0, 0);
			
			//Revalidate and repaint
			projectPanel.revalidate();
			projectPanel.repaint();
			
		}
		
		//Revalidate and repaint
		filePanel.revalidate();
		filePanel.repaint();
		
	}
	
	//This method displays or hides the project class buttons
	public void collapse() {
		
		if(open) { //Collapse if the project is currently not collapsed
			
			open = false; //Set open to false
			
			//Set the project panel size
			projectPanel.setSize(width, buttonHeight);
			projectPanel.setMaximumSize(projectPanel.getSize());
			projectPanel.setMinimumSize(projectPanel.getSize());
			projectPanel.setPreferredSize(projectPanel.getSize());
			
			//Set the file panel size
			filePanel.setSize(0, 0);
			
			//Revalidate and repaint
			projectPanel.revalidate();
			projectPanel.repaint();
			filePanel.revalidate();
			filePanel.repaint();
			
		} else { //Expand if the project is currently collapsed
			
			open = true; //Set open to false
			
			//Set the project panel size
			projectPanel.setSize(width, (filepath.listFiles().length + 1) * buttonHeight);
			projectPanel.setMaximumSize(projectPanel.getSize());
			projectPanel.setMinimumSize(projectPanel.getSize());
			projectPanel.setPreferredSize(projectPanel.getSize());
			
			//Set the file panel size
			filePanel.setSize(width, filepath.listFiles().length * buttonHeight);
			
			//Revalidate and repaint
			projectPanel.revalidate();
			projectPanel.repaint();
			filePanel.revalidate();
			filePanel.repaint();
			
		}
		
	}
	
	//This method reformats the class file buttons after a class is deleted or added
	public void reformatFileButtons() { //This method is used when adding a project
		
		//Clear the panel and button array list
		filePanel.removeAll();
		
		//Set the dimensions of the panels
		projectPanel.setBounds(0, 0, width, (filepath.listFiles().length + 1) * buttonHeight);
		projectPanel.setMaximumSize(projectPanel.getSize());
		projectPanel.setMinimumSize(projectPanel.getSize());
		projectPanel.setPreferredSize(projectPanel.getSize());
		
		filePanel.setBounds(0, 50, width, filepath.listFiles().length * buttonHeight);
		
		//Sort the file buttons alphabetically
		Collections.sort(fileButtons, alphaSorter);
		
		//Set the bounds of the class file buttons
		int i = 0;
		for(Class fileButton: fileButtons) {
			fileButton.setBounds(width / 10, i * buttonHeight, width * 9 / 10, buttonHeight);
			fileButton.setMaximumSize(fileButton.getSize());
			fileButton.setMinimumSize(fileButton.getSize());
			fileButton.setPreferredSize(fileButton.getSize());
			filePanel.add(fileButton);
			i++;
		}
		
		//Set panel dimensions if project is collapsed
		if(!open) {
			
			//Set the project panel size
			projectPanel.setSize(width, buttonHeight);
			projectPanel.setMaximumSize(projectPanel.getSize());
			projectPanel.setMinimumSize(projectPanel.getSize());
			projectPanel.setPreferredSize(projectPanel.getSize());
			
			//Set the file panel size
			filePanel.setSize(0, 0);
			
			//Revalidate and repaint
			projectPanel.revalidate();
			projectPanel.repaint();
			
		}
		
		//Revalidate and repaint
		filePanel.revalidate();
		filePanel.repaint();
		
	}
	
	//This method adds a new class to the project
	public void addNewClass(String className) {
		
		//Add a new class to the file buttons array list
		Class fileButton = new Class(projectName, className);
		fileButton.setSize(width * 9 / 10, buttonHeight);
		fileButton.setMaximumSize(fileButton.getSize());
		fileButton.setMinimumSize(fileButton.getSize());
		fileButton.setPreferredSize(fileButton.getSize());
		fileButtons.add(fileButton);
		
		//Reformat the file buttons
		reformatFileButtons();
		
	}
	
	//Comparator for sorting classes alphabetically
	public static Comparator<Class> alphaSorter = new Comparator<Class>(){
		public int compare(Class class1, Class class2) {
			return class1.getClassName().compareTo(class2.getClassName());
		}
	};
	
	//Getters and setters
	public JPanel getProjectPanel() {
		return projectPanel;
	}

	public void setProjectPanel(JPanel projectPanel) {
		this.projectPanel = projectPanel;
	}

	public JButton getProjectButton() {
		return projectButton;
	}

	public void setProjectButton(JButton projectButton) {
		this.projectButton = projectButton;
	}

	public JPanel getFilePanel() {
		return filePanel;
	}

	public void setFilePanel(JPanel filePanel) {
		this.filePanel = filePanel;
	}

	public ArrayList<Class> getFileButtons() {
		return fileButtons;
	}

	public void setFileButtons(ArrayList<Class> fileButtons) {
		this.fileButtons = fileButtons;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public File getFilepath() {
		return filepath;
	}

	public void setFilepath(File filepath) {
		this.filepath = filepath;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

}
