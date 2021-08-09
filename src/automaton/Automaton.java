package automaton;

/**
 * A class describing any computational machine, be it a finite state machine, a machine with limited memory or a Turing-like machine.
 *
 * @author dimits
 */
public interface Automaton {
	
	/**
	 * A special character marking a transition that is always executed by the automaton.
	 */
	static final char EMPTY = 'e';
	
	/**
	 * Turns an already defined node into an accepted state.
	 * @param name the name of the node
	 * @throws InvalidNodeException if there isn't any node with the corresponding name.
	 */
	void makeAcceptState(char name) throws InvalidNodeException;
	
	/**
	 * Returns the number of nodes in the automaton.
	 */
	int size();
	
	/**
	 * Checks whether or not the node with the corresponding name is the initialization node
	 * (the node which will be first activated upon reading any word).
	 * There can only be one initialization node in an automaton and it's the first node to be created.
	 * 
	 * @param name the nodes name
	 * @return true if the node is an initialization node, false otherwise.
	 */
	boolean isFirst(char name);
	
	/**
	 * Checks whether or not the node with the corresponding name is an accept-state of the automaton
	 * (a node that when read on the end of the word, accepts it).
	 * There can only be more than one accept-state nodes in an automaton, which are specified to be so at the time of creation.
	 * 
	 * @param name the nodes name
	 * @return true if the node is an accept-state node, false otherwise.
	 */
	boolean isLast(char name);
	
	/**
	 * Returns a nice, formatted string informing the user whether or not the supplied word is accepted by the automaton.
	 * @returns a message for the end-user
	 * @see #isAccepted()
	 * @throws InvalidAutomatonException if the Automaton's construction is unfinished.
	 */
	String testAndGetMessage(String word) throws InvalidAutomatonException;
	
	/**
	 * Adds a new node to the automaton. The node should be connected to the others once all other nodes have been defined.
	 * If the nodes name already exists it will <b>replace</b> it.
	 * @param name the name of the new node. 
	 * @throws InvalidNodeException if there already exists a node with the given name.
	 */
	void addNode(char name) throws InvalidNodeException;
	
	/**
	 * Adds a new node to the automaton and makes it an accept-state. See {@link #addNode(char)}
	 * @param name the name of the new node. 
	 * @param isAcceptState whether or not to turn the node into an accept-state.
	 * @throws InvalidNodeException if there already exists a node with the given name.
	 */
	void addNode(char name, boolean isAcceptState) throws InvalidNodeException;
	
	/**
	 * Connects the node with the name c, to the nodes in the nodes array. 
	 * @param nodeName the name of the connectee
	 * @param letter the letter triggering the move
	 * @param targetName the name of the node to be connected
	 * @throws InvalidTransitionException if the letter parameter is invalid for the Automaton's alphabet
	 * @throws InvalidNodeException if there already exists a node with the given name.
	 */
	void connect(char nodeName, char letter, char targetName) throws InvalidNodeException, InvalidTransitionException;
	
	/**
	 * Takes any string and runs it through the automaton, returns whether or not it's accepted.
	 * @param word the string representing the word being computed
	 * @return true if accepted, false otherwise
	 * @throws InvalidAutomatonException if the Automaton's construction is unfinished
	 */
	boolean isAccepted(String word) throws InvalidAutomatonException;
	
	/**
	 * Returns a description of the node with the name c.
	 * @throws InvalidNodeException if there is no node with the given name.
	 */
	String getNodeInfo(char c) throws InvalidNodeException;
}
