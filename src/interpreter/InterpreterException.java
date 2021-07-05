package interpreter;

/**
 * An exception to indicate the failure of any command caused by the automaton itself.
 * This does not necessarily indicate that a syntax error didn't occur, just that it wasn't detected by the interpreter itself.
 */
@SuppressWarnings("serial")
class InterpreterException extends RuntimeException {
	public static final String ERROR_MESSAGE = "Execution Error: ";
	
	public InterpreterException(String message) {
		super(ERROR_MESSAGE+ message);
	}

	public InterpreterException(String message, Throwable cause) {
		super(ERROR_MESSAGE + message, cause);
	}

	public InterpreterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(ERROR_MESSAGE + message, cause, enableSuppression, writableStackTrace);
	}

}
