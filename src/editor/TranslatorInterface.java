package editor;

import java.io.PrintStream;

/**
 * An interface describing all the actions an interpreter or compiler will be
 * asked to perform by the {@link Editor}. 
 * 
 * @author dimits
 */
public interface TranslatorInterface {
	
	/**
	 * Test for compilation or execution errors in the background without printing output messages.
	 * Return whether or not there are any errors.
	 * 
	 * @param code the code to be checked.
	 * @param errorOutput a print stream where the compilation / execution errors will be shown to the user.
	 * The stream will be <i>closed</i> after the translator's test.
	 * @return true if errors exist, false otherwise.
	 */
	boolean testForErrors(String code, PrintStream errorOutput);
	
	/**
	 * Execute the translator.
	 * 
	 * @param code the code to be executed
	 */
	void execute(String code);
	
	/**
	 * Return whether or not the last execution was successful.
	 * 
	 * @return true if the last execution was successful, false otherwise.
	 */
	boolean wasSuccessful();
		
	/**
	 * Make any necessary operations to prepare for the next translation.
	 */
	void reset();
	
	/**
	 * Perform cleanup and execute procedures before shutting the translator off.
	 * Does nothing by default.  
	 */
	default void close() {}
	
	/**
	 * Return whether or not the translator has been shut off by a call in {@link #close()}.
	 * Always returns false by default.
	 * 
	 * @return true if the translator won't accept any new operations, false otherwise.
	 */
	default boolean isClosed() {
		return false;
	}
	
	/**
	 * Change the destination of any output messages during execution.
	 * 
	 * @param out a {@link java.io.PrintStream} where the translator's output will be redirected.
	 */
	void setOutputStream(PrintStream out);
	
	/**
	 * Change the destination of any error messages during translation or execution.
	 * 
	 * @param err a {@link java.io.PrintStream} where the translator's error messages will be redirected.
	 */
	void setErrorStream(PrintStream err);
		
	/**
	 * Get all commands used in the language itself.
	 * 
	 * @return an array or collection holding all the available commands
	 * for the translator's language.
	 */
	Iterable<String> getPrimaryCommands();
	
	/**
	 * Get all commands not used in the language itself, but in a supporting program that
	 * makes any operations on the code itself, such as a preprocessor.
	 * 
	 * @return an array or collection holding all the available commands 
	 * for a program that manipulates the code before execution.
	 */
	Iterable<String> getSecondaryCommands();
	
	/**
	 * Get all strings that don't represent commands but have special meaning in the
	 * translator's language, for example tokens such as "true", "false", "null" etc.
	 * 
	 * @return an array or collection holding all the reserved words of the translator's 
	 * language, ignoring commands themselves.
	 */
	Iterable<String> getReservedWords();
	
	/**
	 * Return a string describing the command or any reserved word to be used as 
	 * a help message for the end user.
	 * 
	 * @param command any reserved word.
	 * @return a help string describing the command.
	 * @throws IllegalArgumentException if the command doesn't exist in the 
	 * translator's language.
	 */
	String getCommandDescription(String command) throws IllegalArgumentException;
	
	/**
	 * Return a string regex describing the format of a comment. 
	 * By default returns a regex that matches with nothing.
	 * 
	 * @return the regex of the comment's format, if comments are supported.
	 */
	default String getCommentRegex() {
		return "a^";
	}

}
