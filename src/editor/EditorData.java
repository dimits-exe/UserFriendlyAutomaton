package editor;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;

/**
 * A class holding internal user data for the editor. 
 * Saved and read upon changing preferences and editor startup respectively.
 *
 */
class EditorData implements Serializable {
	private static final long serialVersionUID = 503080483445671398L;
	
	int UIPreferrence;
	String textFont;
	int textStyle;
	int textSize;
	Color errorColor;
	Color noErrorColor;
	
	/*
	 * Why do we need both the last file and the directory it's in?
	 * Because it's likely the user might delete the file meaning we would need 
	 * something to fall back on when attempting to open. Instead of throwing them to the program directory,
	 * why not the last place they were in?
	 */
	File lastDirectory;
	File lastFile;
}