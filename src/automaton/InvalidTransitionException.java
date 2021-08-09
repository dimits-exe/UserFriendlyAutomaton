package automaton;

/**
 * Thrown when a illegal transition between two states of an {@link Automaton} is detected.
 * @author dimits
 *
 */
public class InvalidTransitionException extends IllegalArgumentException {

	public InvalidTransitionException() {}

	public InvalidTransitionException(String s) {
		super(s);
	}

	public InvalidTransitionException(Throwable cause) {
		super(cause);
	}

	public InvalidTransitionException(String message, Throwable cause) {
		super(message, cause);
	}

}
