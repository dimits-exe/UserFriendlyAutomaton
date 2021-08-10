package automaton;

/**
 * Thrown when an {@link Automaton}'s state is deemed non-functional when attempting to execute 
 * a computational procedure.
 * 
 * @author dimits
 *
 */
public class InvalidAutomatonException extends IllegalStateException {

	public InvalidAutomatonException() {}

	public InvalidAutomatonException(String s) {
		super(s);
	}

	public InvalidAutomatonException(Throwable cause) {
		super(cause);
	}

	public InvalidAutomatonException(String message, Throwable cause) {
		super(message, cause);
	}

}
