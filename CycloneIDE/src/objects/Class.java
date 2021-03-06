package objects;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import assets.Images;
import display.Perspective;
import display.ProjectExplorer;
import display.State;
import utils.FileExecutionTool;
import utils.FileInput;
import utils.LineNumberComponent;
import utils.LineNumberModelImpl;

//This class is used for creating class button objects to be added to the project explorer
public class Class extends JButton {
	
	//Button dimensions
	private static int width = (int) (Perspective.screenWidth/4*3 - 50);
	private static int height = (int) (Perspective.screenHeight/3*2 - 25);
	
	//Font settings
	public static Font editorFont = new Font("Consolas", Font.PLAIN, 22);
	public static int editorTabSize = 2;
	
	//Editor text area
	private JTextArea editorTextArea = new JTextArea();
	private JScrollPane editorTextAreaScroll = new JScrollPane(editorTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private LineNumberModelImpl lineNumberModel = new LineNumberModelImpl(editorTextArea);
	private LineNumberComponent lineNumberComponent = new LineNumberComponent(lineNumberModel);
	
	//Class and project names
	private String projectName;
	private String className; //Stores the name of the class
	
	private TabComponent tab; //Used in the editor tabbed pane
	
	private boolean edited; //Set to true when the pane is edited
	private Object highlightTag;
	
	//Consstructor method
	public Class(String projectName, String className) {
		
		this.projectName = projectName;
		this.className = className;
		edited = false;
		
		//Set the button text and alignment
		setText(className); //Set the button text
		setBackground(State.utilityColor);
		setForeground(State.textColor);
		setHorizontalAlignment(SwingConstants.LEFT);
		
		//Setup the tab component
		tab = new TabComponent(className);
		
		addJComponents();
		setupText();
		
		//Add mouse listener to display a popup when right clicked
		addMouseListener(new MouseAdapter() {
			
			public void mouseClicked(MouseEvent e) {
				
				if(e.getButton() == MouseEvent.BUTTON3) {
					//Create the pop up menu
					ProjectExplorer.classPopup(Class.this);
					
				}
				
			}
			
		});
		
	}
	
	//This method adds the required JComponents
	public void addJComponents() {
		
		//Set up the button
		setIcon(Images.classImage);
		setOpaque(false);
		setContentAreaFilled(false);
		setBorderPainted(false);
		
		//Set up the text area
		editorTextArea.setCaretColor(State.textColor);
		editorTextArea.setFont(editorFont);
		editorTextArea.setForeground(State.textColor);
		editorTextArea.setBackground(State.utilityColor);
		editorTextArea.setOpaque(true);
		editorTextArea.setBounds(0, 0, width, height);
		editorTextArea.setLineWrap(true);
		editorTextArea.setWrapStyleWord(true);
		editorTextArea.setTabSize(editorTabSize);
		editorTextArea.addKeyListener(new KeyListener() {  
			
			@Override
	        public void keyPressed(KeyEvent e) {
				
				//Insert appropriate number of spaces when the enter key is pressed  
	            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
	            	e.consume();
	            	editorTextArea.insert("\n", editorTextArea.getCaretPosition());
	            	
	            	int requiredTabs = FileExecutionTool.insertTabs(editorTextArea.getText().substring(0, editorTextArea.getCaretPosition()));
	            	
	            	for(int i = 0; i < requiredTabs; i++) {
	            		
	            		System.out.println("");
	            		editorTextArea.insert("\t", editorTextArea.getCaretPosition());
	            		
	            	}
	            	
	            }
	            
	        }
	            
			@Override
			public void keyTyped(KeyEvent e) {
	            
	        }

	        @Override
	        public void keyReleased(KeyEvent e) {
	            // TODO Auto-generated method stub      
	        }
	        
	    });
		
		//Update the line component and set the edited variable to true when new text is added
		//Also remove any highlights
		editorTextArea.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void changedUpdate(DocumentEvent arg0) {

				lineNumberComponent.adjustWidth();
				setEdited(true);
				if(highlightTag != null)
					editorTextArea.getHighlighter().removeHighlight(highlightTag);

			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {

				lineNumberComponent.adjustWidth();
				setEdited(true);
				if(highlightTag != null)
					editorTextArea.getHighlighter().removeHighlight(highlightTag);
				
			}
			
			@Override
			public void removeUpdate(DocumentEvent arg0) {

				lineNumberComponent.adjustWidth();
				setEdited(true);
				if(highlightTag != null)
					editorTextArea.getHighlighter().removeHighlight(highlightTag);

			}

		});
		
		//Set the bounds of the text area and add the line number component
		editorTextAreaScroll.setBounds(0 , 0, width, height);
		editorTextAreaScroll.setRowHeaderView(lineNumberComponent);
		
	}
	
	//This method sets up the text for the text area by reading from the class' file
	public void setupText() {
		editorTextArea.setText(FileInput.loadFileAsString(String.format("projects/%s/%s", projectName, className)));
		setEdited(false);
	}
	
	//Getters and Setters
	public JTextArea getEditorTextArea() {
		return editorTextArea;
	}

	public void setEditorTextArea(JTextArea editorTextArea) {
		this.editorTextArea = editorTextArea;
	}

	public JScrollPane getEditorTextAreaScroll() {
		return editorTextAreaScroll;
	}

	public void setEditorTextAreaScroll(JScrollPane editorTextAreaScroll) {
		this.editorTextAreaScroll = editorTextAreaScroll;
	}

	public LineNumberModelImpl getLineNumberModel() {
		return lineNumberModel;
	}

	public void setLineNumberModel(LineNumberModelImpl lineNumberModel) {
		this.lineNumberModel = lineNumberModel;
	}

	public LineNumberComponent getLineNumberComponent() {
		return lineNumberComponent;
	}

	public void setLineNumberComponent(LineNumberComponent lineNumberComponent) {
		this.lineNumberComponent = lineNumberComponent;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public TabComponent getTab() {
		return tab;
	}

	public void setTab(TabComponent tab) {
		this.tab = tab;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
		
		//Show on the tab component when the class is edited or saved
		if(edited)
			tab.showEdited();
		else
			tab.showSaved();
	}

	public Object getHighlightTag() {
		return highlightTag;
	}

	public void setHighlightTag(Object highlightTag) {
		this.highlightTag = highlightTag;
	}
	
}
