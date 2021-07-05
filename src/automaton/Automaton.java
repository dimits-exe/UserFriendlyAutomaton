package automaton;
import java.util.NoSuchElementException;

/**
 * A class describing any computational machine, be it a finite state machine, a machine with limited memory or a Turing-like machine.
 *
 */
public interface Automaton {
	
	/**
	 * A special character that is always followed by the automaton.
	 */
	static final char EMPTY = 'e';
	
	/**
	 * Turns an already defined node into an accepted state.
	 * @param name the name of the node
	 * @throws NoSuchElementException if there isn't any node with the corresponding name.
	 */
	void makeAcceptState(char name) throws NoSuchElementException;
	
	/**
	 * Returns the number of nodes in the automaton.
	 */
	int size();
	
	/**
	 * Checks whether or not the node with the corresponding name is the initialization node
	 * (the node which will be first activated upon reading any word).
	 * There can only be one initialization node in an automaton and it's the first node to be created.
	 * 
	 * @param name
	 * @return true if the node is an initialization node, false otherwise.
	 */
	boolean isFirst(char name);
	
	/**
	 * Checks whether or not the node with the corresponding name is an accept-state of the automaton
	 * (a node that when read on the end of the word, accepts it).
	 * There can only be more than one accept-state nodes in an automaton, which are specified to be so at the time of creation.
	 * 
	 * @param name
	 * @return true if the node is an accept-state node, false otherwise.
	 */
	boolean isLast(char name);
	
	/**
	 * Returns a nice formatted string informing the user whether or not the supplied word is accepted by the automaton.
	 * @see #isAccepted()
	 */
	String formatAnswer(String word) throws IllegalStateException;
	
	/**
	 * Adds a new node to the automaton. The node should be connected to the others once all other nodes have been defined.
	 * If the nodes name already exists it will <b>replace</b> it.
	 * @param name the name of the new node. 
	 */
	void addNode(char name);
	
	/**
	 * Adds a new node to the automaton and makes it an accept-state. See {@link #addNode(char)}
	 * @param name the name of the new node. 
	 * @param isAcceptState whether or not to turn the node into an accept-state.
	 */
	void addNode(char name, boolean isAcceptState);
	
	/**
	 * Connects the node with the name c, to the nodes in the nodes array. 
	 * @param nodeName the name of the connectee
	 * @param letter the letter triggering the move
	 * @param targetName the name of the node to be connected
	 */
	void connect(char nodeName, char letter, char targetName);
	
	/**
	 * Takes any string and runs it through the automaton, returns whether or not it's accepted.
	 * @param word
	 * @return true if accepted, false otherwise
	 */
	boolean isAccepted(String word);
	
	/**
	 * Returns a description of the node with the name c.
	 */
	String getNodeInfo(char c);
}
