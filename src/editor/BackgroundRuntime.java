package editor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import interpreter.AutomatonInterpreter;

/**
 * A class implementing periodic syntax and runtime checks on the code present in the code area  when needed
 * by internally calling a separate interpreter and pasting any errors in a separate text component.
 * 
 * The checks happen in a separate thread as not to disturb the EDT. Common output messages are ignored.
 * 
 * @author dimits
 */
class BackgroundRuntime {
	/**
	 * Time spent waiting for a change in the document to reset the timer.
	 */
	public static final int CHECKUP_TIME = 1000;
    
	private final Timer timer;
	private final Document doc;
	private final JTextComponent output; 
	private final AutomatonEditor autedit;
	private Boolean threadIsWorking;
	
	private int previousHashCode;

	/**
	 * Begin the runtime checks.
	 * 
	 * @param autedit the automaton editor instance that created this object
	 * @param output the text component that will display any error messages
	 * @param doc the document whose contents will be checked
	 */
	public BackgroundRuntime(AutomatonEditor autedit, JTextComponent output, Document doc) {
		timer = new Timer(CHECKUP_TIME,  new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!threadIsWorking && textChanged())
					new InterpreterWorker().run();
			}
		});
		this.doc = doc;

		this.previousHashCode = getText().hashCode();
		this.output = output;
		this.autedit = autedit;
		this.threadIsWorking = false;
	}

	/**
	 * Starts the BackgroundRuntime. Calling this method more than once has no
	 * effect.
	 */
	public void start() {
		// ensure additional method calls don't have any effect
		if (!timer.isRunning()) {
			doc.addUndoableEditListener(new UndoableEditListener() { //reset the counter if any change is detected (aka the user isn't done typing)
				@Override
				public void undoableEditHappened(UndoableEditEvent e) {
					timer.restart();
				}	
			});
			timer.start();
		}
	}
	
	/**
	 * Check if the document's contents have changed since the last check-up.
	 */
	private boolean textChanged() {
		int newHashCode;
		try {
			newHashCode = doc.getText(0, doc.getLength()).hashCode();
		} catch (BadLocationException e) {
			newHashCode = 0;
		}
		boolean changed = (newHashCode != previousHashCode);
		previousHashCode = newHashCode;
		return changed;
		
	}
	
	/**
	 * Get all the document's contents.
	 */
	private String getText() {
		String text;
		try {
			text = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			text = "";
		}
		return text;
	}
	
	/**
	 * Runs a new modified interpreter in a separate thread. 
	 * Locks and releases a mutex while the interpreter is running to prevent multiple simultaneous calls.
	 *
	 */
	@SuppressWarnings("rawtypes")
	private class InterpreterWorker extends SwingWorker{

		@Override
		protected Object doInBackground() throws Exception {
			synchronized(threadIsWorking) {
				threadIsWorking = true;
			}

			if(getText().isBlank()) {
				result(true);
				return null;
			}

			// clear area
			output.setText("");

			
			try(PrintStream noOutputStream = new PrintStream(new NullOutputStream());
					PrintStream textAreaOutputStream = new PrintStream(new ConsoleOutputStream(Color.RED, null, output.getDocument(), output, true),true);) {
				AutomatonInterpreter interp = new AutomatonInterpreter(noOutputStream, textAreaOutputStream);
				interp.executeBatch(doc.getText(0, doc.getLength()));
				result(interp.wasSuccessful());
			}

			return null;
		}

		@Override
		protected void done() {
			synchronized(threadIsWorking) {
				threadIsWorking = false;
			}
		}

		private void result(boolean success) {
			autedit.changeBorder(success);
			output.setVisible(!success);
		}
	}
	
	private class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			return;
		}
		
	}

}
