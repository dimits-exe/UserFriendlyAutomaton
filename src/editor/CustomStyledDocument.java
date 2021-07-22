package editor;

import java.awt.Color;
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
 * @author alexm
 */
class CustomStyledDocument extends DefaultStyledDocument {
	
    /**
     * Encapsulates information about different types of strings that can appear in
     * the Document. Each type is identified by a regular expression (its pattern)
     * and is styled differently.
     */
	public static enum TextType{
        /** Type for Automaton commands */
        INTERPRETER(0, buildPattern(AutomatonInterpreter.getCommands()), boldText),

        /** Type for Preprocessor commands */
        PREPROCESSOR(1, buildPattern(Preprocessor.getCommands()), defaultAttributeSet),

        /** Type for Comments */
        COMMENTS(2, Pattern.compile(Preprocessor.COMMENT_REGEX), italicText),

        /** Type for Reserved words */
        RESERVED(3, buildPattern(AutomatonInterpreter.getReservedWords()), boldText);

        /** ??? */
		public final int index;


        private final Pattern pattern;
        private AttributeSet  attributes;

        /**
         * Constructs the TextType using an index (for spaghetti reasons), a pattern to
         * identify the strings of this TextType and an AttributeSet to define the style
         * of the strings.
         *
         * @param i          the index of ???
         * @param pattern    the pattern of the strings
         * @param attributes the style of the strings
         */
        TextType(int i, Pattern pattern, AttributeSet attributes) {
			index = i;

            this.pattern = pattern;
            this.attributes = attributes;
		}

        /**
         * Changes the color associated with this TextType.
         *
         * @param newColor the new color
         *
         * @return {@code true} if the color was changed, {@code false} if it remained
         *         the same
         */
        private boolean changeColor(Color newColor) {
            final boolean changed = attributes.getAttribute(StyleConstants.Foreground) != newColor;

            if (changed)
                attributes = styleContext.addAttribute(attributes, StyleConstants.Foreground,
                        newColor);

            return changed;
        }
	}
	
	private static final long serialVersionUID = -1507512583034707644L;

    private final static StyleContext styleContext = StyleContext.getDefaultStyleContext();
    private final static AttributeSet defaultAttributeSet, boldText, italicText;
	
    static {
        defaultAttributeSet = styleContext.addAttribute(styleContext.getEmptySet(),
                StyleConstants.Foreground, Color.BLACK);
        boldText = styleContext.addAttribute(defaultAttributeSet, StyleConstants.Bold, true);
        italicText = styleContext.addAttribute(defaultAttributeSet, StyleConstants.Italic, true);
    }
	
    private Boolean isWorking = false; //no that's not a typo, it's a mutex
    private boolean colorsChanged = false;

    public CustomStyledDocument(Color commandColor, Color preprocessorColor, Color commentColor, Color reservedColor) {
        TextType.INTERPRETER.changeColor(commandColor);
        TextType.PREPROCESSOR.changeColor(preprocessorColor);
        TextType.COMMENTS.changeColor(commentColor);
        TextType.RESERVED.changeColor(reservedColor);
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
    
    public void changeColors(TextType cmd, Color color) {  
    	if (cmd.changeColor(color)) {
    		if(isWorking)
    			colorsChanged = true;
    		else
    			handleTextChanged();
    	}
    }
    

    /**
     * Runs the document updates parallel to the EDT. Large documents could be too demanding for a time-sensitive operation like this.
     */
    public void handleTextChanged() {
    	if(!isWorking) {
    		synchronized (isWorking) {
				isWorking = true;
			}
        	new DocumentThread().execute(); //off-load the execution to a new worker thread only if the last one is not already occupied
    	}
    }

    /**
     * Build the regular expression that looks for the whole word of each word that
     * you wish to find. The regex will match any of the {@code words} provided that
     * are not immediately preceded or followed by another {@code \w} character.
     * Further details and limitations about the regex are described in the source
     * code.
     *
     * @param words the set of words for which a Pattern will be created.
     *
     * @return a pattern describing either one of all the words
     */
    private static Pattern buildPattern(String[] words) {
    	// construct the regex: (?<!\w)foo(?!\w)|(?<!\w)bar(?!\w)|...
    	// to match any of the keywords foo, bar separated by anything
    	// apart from a \w, with which keywords start and end

    	// note that this won't work for keywords like "->" for which
    	// a \w character may be allowed ("->a" is correctly interpreted
    	// as "-> a" but the "->" isn't matched)
    	StringBuilder sb = new StringBuilder();

    	for (String token : words) {
    	    sb.append("(?<!\\w)"); // ensure not preceded by a word character
    	    sb.append(token); // match the word
    	    sb.append("(?!\\w)"); // ensure not followed by a word character
    	    sb.append("|"); // provide alternative for the next word
    	}

    	if (sb.length() > 0)
    	    sb.deleteCharAt(sb.length() - 1); // Remove the trailing "|"

    	return Pattern.compile(sb.toString());
    }

    private void updateTextStyles() throws BadLocationException {
        // Clear existing styles
        this.setCharacterAttributes(0, this.getLength(), defaultAttributeSet, true);

        // Look for tokens and highlight them
        for (TextType type : TextType.values()) {
        	 Matcher matcher = type.pattern.matcher(this.getText(0, this.getLength()));
        	 
        	 if(colorsChanged) { //run again if the colors changed mid-update
        		 colorsChanged = false;
        		 updateTextStyles();
        	 }
        	 
             while (matcher.find()) {
                 // Change the color of recognized tokens
             	this.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), type.attributes, false);
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
