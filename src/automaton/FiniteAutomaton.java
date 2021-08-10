package automaton;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * A class handling the common functionality of any finite-state machine that can accept or reject individual words.
 * 
 * @author dimits
 *
 */
abstract class FiniteAutomaton implements Automaton {
	//Data
	protected Node first;
	protected final HashMap<Character,Node> last;
	
	/**
	 * An array of unique characters that are available for the automaton's transitions.
	 */
	protected final char[] alphabet;
	protected final TreeMap<Character,Node> nodes;
	
	//Interface
	/**
	 * Creates a new automaton with the specified alphabet
	 * @param alphabet an array with all the letters the automaton can recognize
	 * @throws IllegalArgumentException if the alphabet contains duplicates or is null
	 */
	public FiniteAutomaton(char[] alphabet) throws IllegalArgumentException { //no need for a custom exception here
		if(hasDuplicates(alphabet))
			throw new IllegalArgumentException("The alphabet can't contain duplicate characters.");
		if(alphabet == null)
			throw new IllegalArgumentException("The alphabet can't be null.");
		
		this.alphabet = alphabet;
		
		nodes = new TreeMap<Character,Node>();
		last = new HashMap<Character,Node>();
	}
	
	@Override
	public final void makeAcceptState(char name) throws InvalidTransitionException {
		last.put(name,node(name));
	}
	
	@Override
	public final int size() {
		return nodes.size();
	}
	
	@Override
	public final boolean isFirst(char name) {return node(name) == first;} 

	@Override
	public final boolean isLast(char name) {return last.containsKey(name);}
	
	@Override
	public String testAndGetMessage(String word) throws InvalidAutomatonException  {
		return word + " " + (isAccepted(word)?"is": "is not") + " an accepted word";
	}
	
	@Override
	public final String getNodeInfo(char c) throws InvalidNodeException {
		if(!nodes.containsKey(c))
			throw new InvalidNodeException("There is no node named " + c);
		
		return node(c).toString();
	}
	
	@Override
	public final void addNode(char name) throws InvalidNodeException {
		if(nodes.containsKey(name))
			throw new InvalidNodeException("There already exists a node named " + name);
		
		Node newNode =  createNode(name);
		if(first == null)
			first = newNode;
		nodes.put(name, newNode);
	}
	
	/**
	 * A method returning a node of each Automaton subclass to be added to the node set
	 */
	protected abstract Node createNode(char name);
	
	@Override
	public final void addNode(char name, boolean isAcceptState) throws InvalidNodeException {
		addNode(name);
		
		if(isAcceptState) 
			makeAcceptState(name);
	}
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		for(Entry<Character, FiniteAutomaton.Node> n : nodes.entrySet()) //for every node
			str.append(String.format("Node %s: %s\n",n.getValue().name, n.getValue()));
		
		if(str.length() == 0)
			return "There are no nodes in this automaton.";
		else
			return str.toString();
	}
	
	//Utility methods
	
	/**
	 * Returns the node in the automaton.
	 * @param c a character representing the name of the node
	 * @returns the node with the corresponding name
	 * @throws InvalidNodeException if there isn't any node with the corresponding name.
	 */
	protected Node node(char name) throws InvalidNodeException{
		Node n = nodes.get(name);
		if(n == null)
			throw new InvalidNodeException("There is no node named "+ name);
		return n;
	}
	
	/**
	 * Returns the index of the provided character in the automaton's alphabet, if it exists.
	 * 
	 * @return The index of the character, -1 else.
	 */
	protected int indexInAlphabet(char c) {	
		for(int i=0; i<alphabet.length; i++)
			if(alphabet[i] == c)
				return i;
		
		return -1;
	}		
	
	private static boolean hasDuplicates(char[] arr) { //checks alphabet validity
		for(int i=0; i<=arr.length-1; i++) 
			for(int j=0; j<=arr.length-1; j++) 
				if(i!=j) 
					if(arr[i] == arr[j])
						return true;			
		return false;
	}
	
	/**
	 * A node interface to implement the {@link FiniteAutomaton#toString()} method in
	 * a node-implementation-independent way.
	 *
	 */
	protected abstract class Node {
		
		protected final char name;
		
		Node(char name){
			this.name = name;
		}
		
		@Override
		public abstract String toString();

	}
}
