package editor;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/***
 *	Class to intercept output from a PrintStream and add it to a Document.
 *  The output can optionally be redirected to a different PrintStream.
 *  The text displayed in the Document can be color coded to indicate
 *  the output source.
 */
class ConsoleOutputStream extends ByteArrayOutputStream
{
	private final String EOL = System.getProperty("line.separator");
	private SimpleAttributeSet attributes;
	private PrintStream printStream;
	private StringBuffer buffer = new StringBuffer(80);
	private boolean isFirstLine;
	
	private final Document document;
	private final JTextComponent textComponent;
	private final boolean isAppend;
	
	/**
	 * Create a new custom stream to a document
	 * 
	 * @param textColor the color of the words printed in the document
	 * @param printStream an optional printStream where the words will also be redirected 
	 * @param document the document where the words will be printed on
	 * @param textComponent the parent container of the document
	 * @param isAppend whether to replace or append the new words each time
	 */
	public ConsoleOutputStream(Color textColor, PrintStream printStream, Document document, JTextComponent textComponent, boolean isAppend)
	{
		if (textColor != null)
		{
			attributes = new SimpleAttributeSet();
			StyleConstants.setForeground(attributes, textColor);
		}

		this.printStream = printStream;
		this.document = document;
		this.textComponent = textComponent;
		this.isAppend = isAppend;
		
		if (isAppend)
			isFirstLine = true;
	}

	/***
	 *  Override this method to intercept the output text. Each line of text
	 *  output will actually involve invoking this method twice:
	 *
	 *  a) for the actual text message
	 *  b) for the newLine string
	 *
	 *  The message will be treated differently depending on whether the line
	 *  will be appended or inserted into the Document
	 */
	public void flush()
	{
		String message = toString();

		if (message.length() == 0) return;

		if (isAppend)
		    handleAppend(message);
		else
		    handleInsert(message);

		reset();
	}

	/***
	 *	We don't want to have blank lines in the Document. The first line
	 *  added will simply be the message. For additional lines it will be:
	 *
	 *  newLine + message
	 */
	private void handleAppend(String message)
	{
		//  This check is needed in case the text in the Document has been
		//	cleared. The buffer may contain the EOL string from the previous
		//  message.

		if (document.getLength() == 0)
			buffer.setLength(0);

		if (EOL.equals(message))
		{
			buffer.append(message);
		}
		else
		{
			buffer.append(message);
			clearBuffer();
		}

	}
	/***
	 *  We don't want to merge the new message with the existing message
	 *  so the line will be inserted as:
	 *
	 *  message + newLine
	 */
	private void handleInsert(String message)
	{
		buffer.append(message);

		if (EOL.equals(message))
		{
			clearBuffer();
		}
	}

	/**
	 *  The message and the newLine have been added to the buffer in the
	 *  appropriate order so we can now update the Document and send the
	 *  text to the optional PrintStream.
	 */
	private void clearBuffer()
	{
		//  In case both the standard out and standard err are being redirected
		//  we need to insert a newline character for the first line only

		if (isFirstLine && document.getLength() != 0)
		{
		    buffer.insert(0, "\n");
		}

		isFirstLine = false;
		String line = buffer.toString();

		try
		{
			if (isAppend)
			{
				int offset = document.getLength();
				document.insertString(offset, line, attributes);
				textComponent.setCaretPosition( document.getLength() );
			}
			else
			{
				document.insertString(0, line, attributes);
				textComponent.setCaretPosition( 0 );
			}
		}
		catch (BadLocationException ble) {}

		if (printStream != null)
		{
			printStream.print(line);
		}

		buffer.setLength(0);
	}
}
