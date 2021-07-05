package editor;

import java.awt.Color;
import java.io.PrintStream;

import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/***
 *  Create a simple console to display text messages.
 *
 *  Messages can be directed here from different sources. Each source can
 *  have its messages displayed in a different color.
 *
 *  Messages can either be appended to the console or inserted as the first
 *  line of the console
 *
 *  You can limit the number of lines to hold in the Document.
 *  
 *  @author Rob Camick,  http://www.camick.com/java/source/MessageConsole.java (slightly modified)
 */
class MessageConsole {
	private JTextComponent textComponent;
	private Document document;
	private boolean isAppend;
	private DocumentListener limitLinesListener;

	public MessageConsole(JTextComponent textComponent){
		this(textComponent, true);
	}

	/***
	 *	Use the text component specified as a simply console to display
	 *  text messages.
	 *
	 *  The messages can either be appended to the end of the console or
	 *  inserted as the first line of the console.
	 */
	public MessageConsole(JTextComponent textComponent, boolean isAppend)
	{
		this.textComponent = textComponent;
		this.document = textComponent.getDocument();
		this.isAppend = isAppend;
		textComponent.setEditable( false );
	}

	/***
	 *  Redirect the output from the standard output to the console
	 *  using the default text color and null PrintStream
	 */
	public void redirectOut()
	{
		redirectOut(null, null);
	}

	/***
	 *  Redirect the output from the standard output to the console
	 *  using the specified color and PrintStream. When a PrintStream
	 *  is specified the message will be added to the Document before
	 *  it is also written to the PrintStream.
	 */
	public void redirectOut(Color textColor, PrintStream printStream)
	{
		ConsoleOutputStream cos = new ConsoleOutputStream(textColor, printStream, document, textComponent, isAppend);
		System.setOut( new PrintStream(cos, true) );
	}

	/***
	 *  Redirect the output from the standard error to the console
	 *  using the default text color and null PrintStream
	 */
	public void redirectErr()
	{
		redirectErr(null, null);
	}

	/***
	 *  Redirect the output from the standard error to the console
	 *  using the specified color and PrintStream. When a PrintStream
	 *  is specified the message will be added to the Document before
	 *  it is also written to the PrintStream.
	 */
	public void redirectErr(Color textColor, PrintStream printStream)
	{
		ConsoleOutputStream cos = new ConsoleOutputStream(textColor, printStream, document, textComponent, isAppend);
		System.setErr( new PrintStream(cos, true) );
	}

	/***
	 *  To prevent memory from being used up you can control the number of
	 *  lines to display in the console
	 *
	 *  This number can be dynamically changed, but the console will only
	 *  be updated the next time the Document is updated.
	 */
	public void setMessageLines(int lines)
	{
		if (limitLinesListener != null)
			document.removeDocumentListener( limitLinesListener );

		limitLinesListener = new LimitLinesDocumentListener(lines, isAppend);
		document.addDocumentListener( limitLinesListener );
	}

	
}