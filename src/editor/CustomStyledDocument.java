package editor;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import interpreter.AutomatonInterpreter;
import interpreter.Preprocessor;

/**
 * A document designed to color its text in accordance to {@link Preprocessor} and {@link AutomatonInterpreter} commands and syntax.
 * Effectively implements syntax highlighting for the TextPane that hosts it
 * 
 * @author dimits
 */
class CustomStyledDocument extends DefaultStyledDocument {

	private static final long serialVersionUID = -1507512583034707644L;

	private static final Set<String> interpreter_commands = new HashSet<String>();
	private static final Set<String> preprocessor_commands = new HashSet<String>();
	private static final Pattern[] patterns = new Pattern[3];
	private static final AttributeSet[] colors = new AttributeSet[patterns.length];
    private final static StyleContext styleContext = StyleContext.getDefaultStyleContext();
    
    private final AttributeSet defaultAttributeSet = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
    private Boolean isWorking = false; //no that's not a typo, it's a mutex
    
    static {
    	for (String com : AutomatonInterpreter.getCommands())
    		interpreter_commands.add(com);
    	
    	for (String com : Preprocessor.getCommands())
    		preprocessor_commands.add(com);
    	
    	patterns[0] = buildPattern(interpreter_commands);
    	patterns[1] = buildPattern(preprocessor_commands);
    	patterns[2] = Pattern.compile(Preprocessor.COMMENT_REGEX);
    	
    	colors[0] = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.MAGENTA); 	//interpreter
    	colors[1] = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.BLUE);		//preprocessor
    	colors[2] = styleContext.addAttribute(styleContext.getEmptySet(), StyleConstants.Foreground, Color.GRAY);   	//comments
    }
    
    @Override
    public void insertString(int offset, String text, AttributeSet attributeSet) throws BadLocationException {
        super.insertString(offset, text, attributeSet);
        handleTextChanged();
    }

    @Override
    public void remove(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
        handleTextChanged();
    }
    

    /**
     * Runs the document updates parallel to the EDT. Large documents could be too demanding for a time-sensitive operation like this.
     * @throws BadLocationException 
     */
    public void handleTextChanged() throws BadLocationException {
    	if(!isWorking) {
    		synchronized (isWorking) {
				isWorking = true;
			}
        	new DocumentThread().execute(); //off-load the execution to a new worker thread only if the last one is not already occupied
    	}
    }

    /**
     * Build the regular expression that looks for the whole word of each word that you wish to find. 
     * The "\\W" is the beginning or end of a word boundary.  The "|" is a regex "or" operator.
     * @return a pattern describing either of all the words
     */
    private static Pattern buildPattern(Set<String> words) {
        StringBuilder sb = new StringBuilder();
        for (String token : words) {
            sb.append("\\W"); // Start of word boundary
            sb.append(token);
            sb.append("\\W|"); // End of word boundary and an or for the next word
        }
        
        if (sb.length() > 0) 
            sb.deleteCharAt(sb.length() - 1); // Remove the trailing "|"
        
        return Pattern.compile(sb.toString());
    }


    private void updateTextStyles() throws BadLocationException {
        // Clear existing styles
        this.setCharacterAttributes(0, this.getLength(), defaultAttributeSet, true);

        // Look for tokens and highlight them
        for(int i=0; i<3; i++) {
        	 Matcher matcher = patterns[i].matcher(this.getText(0, this.getLength()));
        	 
             while (matcher.find()) {
                 // Change the color of recognized tokens
             	this.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), colors[i], false);
             }
        }
    }
	    
    
    @SuppressWarnings("rawtypes")
	private class DocumentThread extends SwingWorker {
		@Override
		protected Object doInBackground() throws InterruptedException {
    		try {
				updateTextStyles();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void done() {
			synchronized (isWorking) {
				isWorking = false;
			}
		}
		    	
    }

         

}
