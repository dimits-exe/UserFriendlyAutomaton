package interpreter;

/**
 * A exception thrown when the Interpreter detects an error in the syntax of a given command
 */
@SuppressWarnings("serial")
class SyntaxException extends IllegalArgumentException {
	public static final String ERROR_MESSAGE = "Syntax Error: ";
	
	public SyntaxException(String s) {
		super(ERROR_MESSAGE + s);
	}

	public SyntaxException(String message, Throwable cause) {
		super(ERROR_MESSAGE + message, cause);
	}

}
