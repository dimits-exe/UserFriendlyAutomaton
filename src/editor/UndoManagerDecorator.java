package editor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

/**
*  	This class will merge individual edits into a single larger edit.
*  	That is, characters entered sequentially will be grouped together and
*  	undone as a group. Any attribute changes will be considered as part
*  	of the group and will therefore be undone when the group is undone. <br>
*  
*  	Uses the decorator pattern to attach functionality to a text component.
*
*	@author Robert Camick, dimits (class interface heavily modified because of Java 9 changes to listeners API).
*/
class UndoManagerDecorator extends UndoManager implements UndoableEditListener, DocumentListener {
	
	/**
	 * Creates a compound undo manager and attaches it to the provided text component.
	 * 
	 * @param textComponent the component which will be given undo-redo functionality.
	 */
	public static void decorate(JTextComponent textComponent) {
		new UndoManagerDecorator(textComponent);
	}
	
	private static final long serialVersionUID = 1L;
	private CompoundEdit compoundEdit;
	private JTextComponent textComponent;

	//  These fields are used to help determine whether the edit is an
	//  incremental edit. The offset and length should increase by 1 for
	//  each character added or decrease by 1 for each character removed.

	private int lastOffset;
	private int lastLength;
	
	/**
	* Creates a compound undo manager whose undo/redo methods can be called manually.
	*/
	public UndoManagerDecorator(JTextComponent textComponent) {
		this.textComponent = textComponent;
		textComponent.getDocument().addUndoableEditListener( this );
		textComponent.addKeyListener(new UndoRedoKeyListener());
	}


	/*
	**  Add a DocumentLister before the undo is done so we can position
	**  the Caret correctly as each edit is undone.
	*/
	public void undo() {
		try{
			textComponent.getDocument().addDocumentListener(this);
			super.undo();
			textComponent.getDocument().removeDocumentListener(this);
			textComponent.requestFocusInWindow();
		}
		catch (CannotUndoException ex) {}
	}

	/*
	**  Add a DocumentLister before the redo is done so we can position
	**  the Caret correctly as each edit is redone.
	*/
	public void redo() {
		try {
			textComponent.getDocument().addDocumentListener(this);
			super.redo();
			textComponent.getDocument().removeDocumentListener(this);
			textComponent.requestFocusInWindow();
		}
		catch (CannotRedoException ex) {}
	}

	/*
	**  Whenever an UndoableEdit happens the edit will either be absorbed
	**  by the current compound edit or a new compound edit will be started
	*/
	public void undoableEditHappened(UndoableEditEvent e)
	{
		//  Start a new compound edit

		if (compoundEdit == null)
		{
			compoundEdit = startCompoundEdit( e.getEdit() );
			return;
		}

		int offsetChange = textComponent.getCaretPosition() - lastOffset;
		int lengthChange = textComponent.getDocument().getLength() - lastLength;

		//  Check for an attribute change
		if (offsetChange == 0)
		{
			compoundEdit.addEdit(e.getEdit() );
			return;
		}
	

		//  Check for an incremental edit or backspace.
		//  The Change in Caret position and Document length should both be
		//  either 1 or -1.

//		int offsetChange = textComponent.getCaretPosition() - lastOffset;
//		int lengthChange = textComponent.getDocument().getLength() - lastLength;

		if (offsetChange == lengthChange
		&&  Math.abs(offsetChange) == 1)
		{
			compoundEdit.addEdit( e.getEdit() );
			lastOffset = textComponent.getCaretPosition();
			lastLength = textComponent.getDocument().getLength();
			return;
		}

		//  Not incremental edit, end previous edit and start a new one

		compoundEdit.end();
		compoundEdit = startCompoundEdit( e.getEdit() );
	}

	/*
	**  Each CompoundEdit will store a group of related incremental edits
	**  (ie. each character typed or backspaced is an incremental edit)
	*/
	private CompoundEdit startCompoundEdit(UndoableEdit anEdit)
	{
		//  Track Caret and Document information of this compound edit

		lastOffset = textComponent.getCaretPosition();
		lastLength = textComponent.getDocument().getLength();

		//  The compound edit is used to store incremental edits

		compoundEdit = new MyCompoundEdit();
		compoundEdit.addEdit( anEdit );

		//  The compound edit is added to the UndoManager. All incremental
		//  edits stored in the compound edit will be undone/redone at once

		addEdit( compoundEdit );
		return compoundEdit;
	}

//
//  Implement DocumentListener
//
	/*
	 *  Updates to the Document as a result of Undo/Redo will cause the
	 *  Caret to be repositioned
	 */
	public void insertUpdate(final DocumentEvent e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				int offset = e.getOffset() + e.getLength();
				offset = Math.min(offset, textComponent.getDocument().getLength());
				textComponent.setCaretPosition( offset );
			}
		});
	}

	public void removeUpdate(DocumentEvent e)
	{
		textComponent.setCaretPosition(e.getOffset());
	}

	public void changedUpdate(DocumentEvent e) {}


	class MyCompoundEdit extends CompoundEdit {

		private static final long serialVersionUID = -1824107970022500558L;

		public boolean isInProgress()
		{
			//  in order for the canUndo() and canRedo() methods to work
			//  assume that the compound edit is never in progress

			return false;
		}

		public void undo() throws CannotUndoException
		{
			//  End the edit so future edits don't get absorbed by this edit

			if (compoundEdit != null)
				compoundEdit.end();

			super.undo();

			//  Always start a new compound edit after an undo

			compoundEdit = null;
		}
	}
	
	/**
	 * Listens for CTRL-Z/CTRL-Y keyboard commands and executes undo/redo commands respectively.
	 * 
	 * @author dimits
	 */
	private class UndoRedoKeyListener implements KeyListener {

		@Override          
		public void keyPressed(KeyEvent e) {
			if ((e.getKeyCode() == KeyEvent.VK_Z) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK)) {
				UndoManagerDecorator.this.undo();
			} else if ((e.getKeyCode() == KeyEvent.VK_Y) && ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK)) {
				UndoManagerDecorator.this.redo();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {return;}
		
		@Override
		public void keyTyped(KeyEvent e) {return;}
		
	}
	

}
