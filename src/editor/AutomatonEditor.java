package editor;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;

import java.util.Date;
import java.util.Scanner;

import interpreter.AutomatonInterpreter;
import interpreter.Preprocessor;

import editor.CustomStyledDocument.TextType;

/**
 * A GUI implementation of an automaton interpreter. Designed to be heavily modifiable and user-friendly.
 * Uses multithreading to deal with heavy procedures and avoid blocking of UI components.
 * 
 * @author dimits
 */
public final class AutomatonEditor extends JFrame {
	
//hard-coded data
	private static final long serialVersionUID = -5206786832243911971L;
	
	private static final String[] fonts = {Font.DIALOG, Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED};
	private static final Integer[] styles = {Font.PLAIN, Font.BOLD, Font.ITALIC};
	private static final String[] styleNames = {"None", "Bold", "Italic"};
	private static final Integer[] textSizes = {12,14,16,18,20,22,24};
	
	private static final Color[] colors = {Color.RED, Color.BLACK, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.GRAY};
	private static final String[] colorNames = {"Red", "Black", "Blue", "Green", "Purple", "Orange","Gray"};
	
	private static final String[] processorCommands = Preprocessor.getCommands();
	private static final String[] interpeterCommands = AutomatonInterpreter.getCommands();
	
	private static final LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
	
//user choices
	private static final String SETTINGS_FILE_NAME = "settings.aut";
	private static EditorData data;
	
	private final JRadioButtonMenuItem[] errorColorItems;
	private final JRadioButtonMenuItem[] noErrorColorItems;
	private final JRadioButtonMenuItem[] commentColorItems, reservedColorItems, commandColorItems, preprocessorColorItems;
	private final JRadioButtonMenuItem[] fontItems;
	private final JRadioButtonMenuItem[] styleItems;
	private final JRadioButtonMenuItem[] textSizeItems;
	private final JRadioButtonMenuItem[] lookAndFeelItems;
	
	private final JRadioButtonMenuItem[] processorHelpItems;
	private final JRadioButtonMenuItem[] interpHelpItems;
	
	
//components
	private final JLabel  codeAreaLabel, consoleLabel;
	private final JPanel  rightBuffer, leftBuffer; 	// give some horizontal room to the components of centerPanel by adding empty buffers to the left and right
	private final JPanel  screensPanel, consolePanel;
	private final JTextPane codeArea;
	private final MutableColorBorder codeBorder;
	private final JTextPane interpreterConsole;
	private final JTextPane singleLineCodeArea; 	//doesn't do anything, will keep it around for any future ideas
	private final JButton executeButton;
	
	private AutomatonInterpreter interp;
	private CustomStyledDocument textDocument;
	private final BackgroundRuntime backgroundRuntime;
	private int lastSavedStringCode; 				//to check if unsaved text is present in the codeArea
	
	public static void main(String[] args) {
		AutomatonEditor editor = new AutomatonEditor("Automaton Editor v1");
		editor.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		editor.setSize(800,700);
		editor.setVisible(true);
		editor.setResizable(true);
	}
	
	public AutomatonEditor(String title) throws HeadlessException {
		super(title);
		
	//define custom close operation to ask user if he wants to save his work before exiting
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
			
	//create menus
		errorColorItems = new JRadioButtonMenuItem[colors.length];
		noErrorColorItems = new JRadioButtonMenuItem[colors.length];
		fontItems = new JRadioButtonMenuItem[fonts.length];
		styleItems = new JRadioButtonMenuItem[styles.length];
		textSizeItems = new JRadioButtonMenuItem[textSizes.length];
		processorHelpItems = new JRadioButtonMenuItem[processorCommands.length];
		interpHelpItems = new JRadioButtonMenuItem[interpeterCommands.length];
		lookAndFeelItems = new JRadioButtonMenuItem[looks.length];
		
		commentColorItems = new JRadioButtonMenuItem[colors.length];
		reservedColorItems = new JRadioButtonMenuItem[colors.length];
		commandColorItems = new JRadioButtonMenuItem[colors.length];
		preprocessorColorItems= new JRadioButtonMenuItem[colors.length];

		setJMenuBar(createMenus());
		loadSettings();
		
		if(data.UIPreferrence != -1) {
			setLookAndFeel(data.UIPreferrence);
		}
		
	//create components
		rightBuffer = new JPanel();
		leftBuffer = new JPanel();
		screensPanel = new JPanel(); 	//central panel
		consolePanel = new JPanel(); 	//contains console + text area for individual commands
		
		//console
		consoleLabel = new JLabel("Output Console");
		interpreterConsole = new JTextPane();
		interpreterConsole.setEditable(false);
		JScrollPane interpreterScroll = new JScrollPane(interpreterConsole, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		MessageConsole mc = new MessageConsole(interpreterConsole);
		mc.redirectOut(data.noErrorColor,null);
		mc.redirectErr(data.errorColor,null);
		
		//code area
		singleLineCodeArea = new JTextPane();
		singleLineCodeArea.setEditable(false);
		singleLineCodeArea.getDocument().addDocumentListener(new LimitLinesDocumentListener(3));
		singleLineCodeArea.setVisible(false);
		
		codeBorder = new MutableColorBorder(data.noErrorColor);				
		codeAreaLabel = new JLabel("Editor Area");
		
		textDocument = new CustomStyledDocument(data.syntaxColors[0],data.syntaxColors[1],data.syntaxColors[2],data.syntaxColors[3]);
		codeArea = new JTextPane(textDocument); 
		codeArea.setFont(new Font(data.textFont, data.textStyle, data.textSize));
		codeArea.setBorder(codeBorder);
		new CompoundUndoManager(codeArea);

		backgroundRuntime = new BackgroundRuntime(AutomatonEditor.this, singleLineCodeArea, codeArea.getDocument());
		backgroundRuntime.start();

				
		JScrollPane codeAreaScroll = new JScrollPane(codeArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		codeAreaScroll.setRowHeaderView(new TextLineNumber(codeArea));
		codeAreaScroll.setMinimumSize(new Dimension(400,400));
		

		//rest of screens
		executeButton = new JButton("Execute");		
		interpreterConsole.setFont(new Font(data.textFont, data.textStyle, data.textSize));
		
	//add listeners
		executeButton.addActionListener(
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					executeCode();
				}
		});
		
	//add components
		consolePanel.setPreferredSize(new Dimension(700, 500));
		consolePanel.add(consoleLabel);
		consolePanel.add(interpreterScroll);
		
		screensPanel.add(codeAreaLabel);
		screensPanel.add(Box.createRigidArea(new Dimension(50,10)));
		screensPanel.add(codeAreaScroll);
		screensPanel.add(Box.createRigidArea(new Dimension(50,10)));
		screensPanel.add(singleLineCodeArea);
		screensPanel.add(Box.createRigidArea(new Dimension(50,10)));
		screensPanel.add(executeButton);
		screensPanel.add(Box.createRigidArea(new Dimension(50,35)));
		screensPanel.add(consolePanel);
		
		leftBuffer.add(Box.createRigidArea(new Dimension(20,50)));
		rightBuffer.add(Box.createRigidArea(new Dimension(20,50)));
		
		add(leftBuffer);
		add(screensPanel);
		add(rightBuffer);
		
	//setLayouts
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		screensPanel.setLayout(new BoxLayout(screensPanel,BoxLayout.Y_AXIS));
		consolePanel.setLayout(new BoxLayout(consolePanel,BoxLayout.Y_AXIS));
		
	//build interpreter
		interp = new AutomatonInterpreter();
		
	//attempt to retrieve last text file
		if(data.lastFile != null && data.lastFile.exists()) {
			String lastFileText = readFile(data.lastFile);
			if(lastFileText != null) {
				codeArea.setText(lastFileText);
				lastSavedStringCode = codeArea.getText().hashCode(); 
			}	
		}else
			codeArea.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"); //this is necessary because BoxLayout has a stroke otherwise
		
	//add Save listener
		JPanel app = ((JPanel)this.getContentPane());
		app.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), "save");
		app.getActionMap().put("save", new AbstractAction() {
			private static final long serialVersionUID = 2561258219327746202L;
			@Override
			public void actionPerformed(ActionEvent e) {
				AutomatonEditor.this.save();
			}
		});
		
	}

	void changeBorder(boolean wasSuccesfull) { 
		if(wasSuccesfull) 
			codeBorder.setColor(data.noErrorColor);
		else
			codeBorder.setColor(data.errorColor);
		
		codeArea.repaint();
	}
	
//user settings
	private void loadSettings() {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SETTINGS_FILE_NAME))) {
	         data = (EditorData) in.readObject();
		} catch(IOException e) {
			loadDefaultSettings();
		} catch (ClassNotFoundException ce) {
			System.err.println("What the fuck have you done");
			throw new Error(ce); //terminate no matter what
		}
		setMenuData();		

	}
	
	private void saveSettings() {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SETTINGS_FILE_NAME))){
	         out.writeObject(data);
	    } catch (IOException ioe) {
	         System.err.println("Error: Unable to save new settings to file.");
	    }
	}
	
	private void loadDefaultSettings() {
		data = new EditorData();
		data.textFont = Font.DIALOG;
		data.textStyle = Font.PLAIN;
		data.textSize = 14;
		data.errorColor = Color.RED;
		data.noErrorColor = Color.BLUE;
		data.UIPreferrence = 0;
		
		data.syntaxColors = new Color[4];
		data.syntaxColors[TextType.INTERPRETER.index] = Color.ORANGE;
		data.syntaxColors[TextType.PREPROCESSOR.index] = Color.BLUE;
		data.syntaxColors[TextType.COMMENTS.index] = Color.GRAY;
		data.syntaxColors[TextType.RESERVED.index] = Color.ORANGE;
		
		for(TextType type : TextType.values())
			textDocument.changeColors(type, data.syntaxColors[type.index]);
		
		saveSettings();
		setMenuData();	
	}
	
	/**
	 * Changes the selected buttons upon loading different settings
	 */
	private void setMenuData() {
		errorColorItems[findObject(colors, data.errorColor)].setSelected(true);
		noErrorColorItems[findObject(colors, data.noErrorColor)].setSelected(true);
		fontItems[findObject(fonts, data.textFont)].setSelected(true);
		styleItems[findObject(styles, data.textStyle)].setSelected(true);
		textSizeItems[findObject(textSizes,data.textSize)].setSelected(true);
		lookAndFeelItems[data.UIPreferrence].setSelected(true);	
		
		commentColorItems[findObject(colors, data.syntaxColors[TextType.COMMENTS.index])].setSelected(true);
		reservedColorItems[findObject(colors, data.syntaxColors[TextType.RESERVED.index])].setSelected(true);
		commandColorItems[findObject(colors, data.syntaxColors[TextType.INTERPRETER.index])].setSelected(true);
		preprocessorColorItems[findObject(colors, data.syntaxColors[TextType.PREPROCESSOR.index])].setSelected(true);
		
	}
	
//user actions
	/**
	 * Decides whether the application can save the data in the last used directory.
	 * If not, shows a dialog box
	 */
	private void save() {
		if(data.lastFile != null && data.lastFile.exists()) 
			saveText(data.lastFile);
		else
			showSaveAsDialog();
		
	}
	
	/**
	 * Show a dialog window prompting the user to select a directory to save the file.
	 */
	private void showSaveAsDialog() {
		JFileChooser fc = new JFileChooser();
		
		if(data.lastDirectory != null && data.lastDirectory.exists())
			fc.setCurrentDirectory(data.lastDirectory);
		else
			fc.setCurrentDirectory(FileSystems.getDefault().getPath(".").toFile());
		
		fc.setDialogTitle("Save As");
		int returnVal = fc.showSaveDialog(AutomatonEditor.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	saveText(fc.getSelectedFile());
        	//update settings
        	data.lastDirectory = fc.getSelectedFile().getParentFile();
        	data.lastFile = fc.getSelectedFile();
        	saveSettings();
        }
	}
	
	/**
	 * Actually performs the save operation.
	 * @param file the file chosen for the text to be written in.
	 */
	private void saveText(File file) {
		//save text as is
    	try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
    		out.write(codeArea.getText());
    		System.out.println("File saved succesfully.");
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage().length() == 0 ?
					"An error occured while opening the file" : ioe.getMessage()); //throw an exception that can be caught in the main function
		}
    	lastSavedStringCode = codeArea.getText().hashCode();
	}
	
	private void showOpenDialog() {
		JFileChooser fc = new JFileChooser();
		
		if(data.lastDirectory != null && data.lastDirectory.exists())
			fc.setCurrentDirectory(data.lastDirectory);
		else
			fc.setCurrentDirectory(FileSystems.getDefault().getPath(".").toFile());
		
		fc.setDialogTitle("Open File");
		int returnVal = fc.showOpenDialog(AutomatonEditor.this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String code = readFile(fc.getSelectedFile());
			
			if(code != null) {
				codeArea.setText(code);
				data.lastDirectory = fc.getSelectedFile().getParentFile();
	        	data.lastFile = fc.getSelectedFile();
		    	saveSettings();
			}
		 }
	}
	
	//used for showOpenDialog as well as editor initialization
	private String readFile(File sourceFile) {
		StringBuilder builder = new StringBuilder();
		try (Scanner in = new Scanner(sourceFile)){
			//read lines
			while(in.hasNext()) {
				builder.append(in.nextLine());
				builder.append('\n');
			}				
			//execute lines
			in.close();			
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage().length() == 0 ?
					"An error occured while opening the file" : ioe.getMessage()); 
			return null;
		}
		return builder.toString();
		
	}
	
	private void executeCode() {
		new MainWorkerThread().run();
	}
	
	/**
	 * Verifies user decision to exit if needed. Saves code, performs cleanup and exits if the exit was confirmed.
	 */
	private void exit() {
		boolean exitConfirmed = true;
		
		if(lastSavedStringCode != codeArea.getText().hashCode()) {
			int answer = JOptionPane.showConfirmDialog(this, "You have unsaved work. Would you like to save before exiting?");
			switch(answer) {
			case JOptionPane.YES_OPTION:
				showSaveAsDialog();
				exitConfirmed = true;
				break;
				
			case JOptionPane.NO_OPTION:
				exitConfirmed = true;
				break;
				
			default:
				exitConfirmed = false;
			}
		}
		
		if(exitConfirmed) {
			saveSettings();
			interp.close();
			System.exit(0);
		}
	}
	
	//helper methods for menu loading
	private void setLookAndFeel(int index) {
		try {
			UIManager.setLookAndFeel(looks[index].getClassName());
		} catch (Exception e) {
			System.err.println("Custom look and feel could not be loaded");
			data.UIPreferrence = 0;
			saveSettings();
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e2) {throw new Error(e2);}
		}
		
		SwingUtilities.updateComponentTreeUI(this);
	}

	private static <T> int findObject(T[] array, T object) {
		for (int i = 0; i < array.length; i++)
			if (array[i].equals(object))
				return i;
		throw new RuntimeException("Value no longer in array");
	}

	private <T> void populateMenu(T[] info, JRadioButtonMenuItem[] menuItems, JMenu menu, ActionListener l) {
		ButtonGroup bgroup = new ButtonGroup();
		for (int i = 0; i < info.length; i++) {
			menuItems[i] = new JRadioButtonMenuItem(info[i].toString());
			menu.add(menuItems[i]);
			bgroup.add(menuItems[i]);
			menuItems[i].addActionListener(l);
		}
	}

	//menu stuff
	private JMenuBar createMenus() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");

		//File Menu
		JMenuItem newItem = new JMenuItem("New");
		JMenuItem openItem = new JMenuItem("Open...");
		JMenuItem saveItem = new JMenuItem("Save");
		JMenuItem saveAsItem = new JMenuItem("Save As...");
		JMenuItem clearConsoleItem = new JMenuItem("Clear Console");
		JMenuItem exitItem = new JMenuItem("Exit");

		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(clearConsoleItem);
		fileMenu.add(exitItem);

		newItem.addActionListener(e -> {
			interp.reset();
			codeArea.setText("");
			codeBorder.setColor(data.noErrorColor);
			singleLineCodeArea.setText("");
			interpreterConsole.setText("Output will be shown here.");
		});

		//Open As
		openItem.addActionListener(e -> showOpenDialog());

		saveItem.addActionListener(e -> save());

		//Save As
		saveAsItem.addActionListener(e -> showSaveAsDialog());

		clearConsoleItem.addActionListener(e -> interpreterConsole.setText(""));

		//Exit
		exitItem.addActionListener(e -> {
			exit();
			System.exit(0);
		});

		//Customization Menu
		JMenu customMenu = new JMenu("Customize");

		//look menu
		JMenu lookAndFeelMenu = new JMenu("Look and Feel");
		ButtonGroup bgroup = new ButtonGroup();
		for (int i = 0; i < looks.length; i++) {
			lookAndFeelItems[i] = new JRadioButtonMenuItem(looks[i].getName());
			lookAndFeelMenu.add(lookAndFeelItems[i]);
			bgroup.add(lookAndFeelItems[i]);
			lookAndFeelItems[i].addActionListener(new LookAndFeelHandler());
		}

		//error color
		JMenu errorColorMenu = new JMenu("Error Color");
		populateMenu(colorNames, errorColorItems, errorColorMenu, new OutputColorHandler());

		//no error color
		JMenu noErrorColorMenu = new JMenu("Standard Color");
		populateMenu(colorNames, noErrorColorItems, noErrorColorMenu, new OutputColorHandler());

		//syntaxHighlighting
		JMenu syntaxMenu = new JMenu("Syntax highlighting");

		//comments
		JMenu commentColorMenu = new JMenu("Comments");
		populateMenu(colorNames, commentColorItems, commentColorMenu,
				new SyntaxColorHandler(commentColorItems, TextType.COMMENTS));

		//commands
		JMenu commandColorMenu = new JMenu("Commands");
		populateMenu(colorNames, commandColorItems, commandColorMenu,
				new SyntaxColorHandler(commandColorItems, TextType.INTERPRETER));

		//preprocessor
		JMenu preprocessorColorMenu = new JMenu("Preprocessor");
		populateMenu(colorNames, preprocessorColorItems, preprocessorColorMenu,
				new SyntaxColorHandler(preprocessorColorItems, TextType.PREPROCESSOR));

		//reserved words
		JMenu reservedColorMenu = new JMenu("Reserved words");
		populateMenu(colorNames, reservedColorItems, reservedColorMenu,
				new SyntaxColorHandler(reservedColorItems, TextType.RESERVED));

		syntaxMenu.add(commentColorMenu);
		syntaxMenu.add(commandColorMenu);
		syntaxMenu.add(preprocessorColorMenu);
		syntaxMenu.add(reservedColorMenu);

		//text options
		JMenu textMenu = new JMenu("Text Options");
		TextChoiceHandler textHandler = new TextChoiceHandler();

		//font menu
		JMenu fontMenu = new JMenu("Text Font");
		populateMenu(fonts, fontItems, fontMenu, textHandler);

		//style menu
		JMenu styleMenu = new JMenu("Text Style");
		populateMenu(styleNames, styleItems, styleMenu, textHandler);

		//sizeMenu
		JMenu sizeMenu = new JMenu("Text Size");
		populateMenu(textSizes, textSizeItems, sizeMenu, textHandler);

		textMenu.add(fontMenu);
		textMenu.add(styleMenu);
		textMenu.add(sizeMenu);

		//default choices
		JMenuItem revertItem = new JMenuItem("Restore Default");
		revertItem.addActionListener(e -> {
			errorColorItems[0].setSelected(true);
			noErrorColorItems[2].setSelected(true);
			fontItems[0].setSelected(true);
			styleItems[0].setSelected(true);
			textSizeItems[1].setSelected(true);

			loadDefaultSettings();
			System.out.println("Default settings loaded. A restart might be needed for some changes to apply.");
		});

		customMenu.add(lookAndFeelMenu);
		customMenu.add(textMenu);
		customMenu.add(noErrorColorMenu);
		customMenu.add(errorColorMenu);
		customMenu.add(syntaxMenu);
		customMenu.add(revertItem);

		//Help Menu
		JMenu helpMenu = new JMenu("Help");

		//preprocessor help
		JMenu processorHelpMenu = new JMenu("Preprocessor");
		populateMenu(processorCommands, processorHelpItems, processorHelpMenu, new ProcessorHelpHandler());

		//interpreter help
		JMenu interpreterHelpMenu = new JMenu("Interpreter");
		populateMenu(interpeterCommands, interpHelpItems, interpreterHelpMenu, new InterpreterHelpHandler());

		helpMenu.add(processorHelpMenu);
		helpMenu.add(interpreterHelpMenu);

		menuBar.add(fileMenu);
		menuBar.add(customMenu);
		menuBar.add(helpMenu);

		return menuBar;
	}
	
//private classes
	/**
	 * Off-loads the execution onto a new separate thread. This ensures the EDT remains unblocked during the only 
	 * demanding process of this class.
	 *
	 */
	@SuppressWarnings("rawtypes")
	private class MainWorkerThread extends SwingWorker {

		@Override
		protected Object doInBackground() throws Exception {
			executeButton.setEnabled(false);
			
			System.out.println("***************************");
			System.out.println("Execution started at " + new Date(System.currentTimeMillis()) +":");
			
			changeBorder(interp.executeBatch(codeArea.getText()));
			
			System.out.println("***************************");
			
			if(!interp.isClosed())
				executeButton.setEnabled(true);
			return null;
		}
		
	}

//handlers	
	private class LookAndFeelHandler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i=0; i< looks.length;i++){
				if(lookAndFeelItems[i].isSelected()) {
					data.UIPreferrence = i;
					break;
				}	
			}
			AutomatonEditor.this.setLookAndFeel(data.UIPreferrence);
			saveSettings();
		}
	}
	
	private class OutputColorHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			for (int i=0; i< colors.length;i++){
				if(errorColorItems[i].isSelected()) {
					data.errorColor = colors[i];
					break;
				}	
			}
			
			for (int i=0; i< colors.length;i++){
				if(noErrorColorItems[i].isSelected()) {
					data.noErrorColor = colors[i];
					break;
				}	
			}
			repaint();
			saveSettings();
		}
	}
	
	private class SyntaxColorHandler implements ActionListener {
		private TextType type;
		private JRadioButtonMenuItem[] items;
		
		SyntaxColorHandler(JRadioButtonMenuItem[] items, TextType type){
			this.type = type;
			this.items = items;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i=0; i< colors.length;i++){
				if(items[i].isSelected()) {
					textDocument.changeColors(type, colors[i]);
					data.syntaxColors[type.index] = colors[i];
					break;
				}	
			}
			saveSettings();
		}
		
	}
	
	private class TextChoiceHandler implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i=0; i< fontItems.length; i++) {
				if(fontItems[i].isSelected()) {
					data.textFont = fonts[i];
					break;
				}		
			}
			
			for (int i=0; i< styleItems.length; i++) {
				if(styleItems[i].isSelected()) {
					data.textStyle = styles[i];
					break;
				}		
			}
			
			for (int i=0; i< textSizeItems.length; i++) {
				if(textSizeItems[i].isSelected()) {
					data.textSize = textSizes[i];
					break;
				}		
			}	
			saveSettings();
		}
		
	}//TextChoiceHandler
	
	/*
	 * Had to make these 2 separate classes so they don't both print every time the user
	 * clicks on a help button.
	 */
	
	private class ProcessorHelpHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			for (int i=0; i < processorHelpItems.length;i++) {
				if(processorHelpItems[i].isSelected()) {
					System.out.println("\n" + Preprocessor.getCommandDescription(processorCommands[i]));
					break;
				}
			}
		}
	}
	
	private class InterpreterHelpHandler implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			for (int i=0; i < interpHelpItems.length;i++) {
				if(interpHelpItems[i].isSelected()) {
					System.out.println("\n" + AutomatonInterpreter.getCommandDescription(interpeterCommands[i]));
					break;
				}
			}
		}
	}
	
}
