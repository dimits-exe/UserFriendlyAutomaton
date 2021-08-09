package automaton;

/**
 * Thrown when a reference is made to a non-existent state (node) in the {@link Automaton}.
 * 
 * @author dimits
 *
 */
public class InvalidNodeException extends IllegalArgumentException {

	public InvalidNodeException() {}

	public InvalidNodeException(String s) {
		super(s);
	}

	public InvalidNodeException(Throwable cause) {
		super(cause);
	}

	public InvalidNodeException(String message, Throwable cause) {
		super(message, cause);
	}

}
