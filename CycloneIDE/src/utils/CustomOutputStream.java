package utils;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

//SOURCE: https://stackoverflow.com/questions/5107629/how-to-redirect-console-content-to-a-textarea-in-java
public class CustomOutputStream extends OutputStream {
	
	//JTextArea
    private JTextArea textArea;
    
    //Constructor
    public CustomOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    //Send console output to text area
    @Override
    public void write(int b) throws IOException {
        // redirects data to the text area
        textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
        // keeps the textArea up to date
        textArea.update(textArea.getGraphics());
    }
    
}