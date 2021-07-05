package automaton;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * A class describing any finite-state machine that can accept or reject individual words.
 *
 */
public abstract class FiniteAutomaton implements Automaton{
	//Data
	protected Node first;
	protected final HashMap<Character,Node> last;
	
	protected final char[] alphabet;
	protected final TreeMap<Character,Node> nodes;
	
	//Interface
	/**
	 * Creates a new automaton with the specified alphabet
	 * @param alphabet an array with all the letters the automaton can recognize
	 * @throws IllegalArgumentException if the alphabet contains duplicates or is null
	 */
	public FiniteAutomaton(char[] alphabet) throws IllegalArgumentException {
		if(hasDuplicates(alphabet))
			throw new IllegalArgumentException("The alphabet can't contain duplicate characters.");
		if(alphabet == null)
			throw new IllegalArgumentException("The alphabet can't be null.");
		
		this.alphabet = alphabet;
		
		nodes = new TreeMap<Character,Node>();
		last = new HashMap<Character,Node>();
	}
	
	@Override
	public final void makeAcceptState(char name) throws NoSuchElementException {
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
	public String formatAnswer(String word) throws IllegalStateException  {
		return word + " " + (isAccepted(word)?"is": "is not") + " an accepted word";
	}
	
	@Override
	public final String getNodeInfo(char c) {
		return node(c).toString();
	}
	
	@Override
	public final void addNode(char name) {
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
	public final void addNode(char name, boolean isAcceptState) {
		addNode(name);
		if(isAcceptState) makeAcceptState(name);
	}
	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		for(Entry<Character, FiniteAutomaton.Node> n : nodes.entrySet()) //for every node
			str.append("Node " + n.getValue() + "\n");
		
		if(str.length() == 0)
			return "There are no nodes in this automaton.";
		else
			return str.toString();
	}
	
	//utility methods
	
	/**
	 * Returns the node in the automaton.
	 * @param c a character representing the name of the node
	 * @returns the node with the corresponding name
	 * @throws NoSuchElementException if there isn't any node with the corresponding name.
	 */
	protected Node node(char name) throws NoSuchElementException{
		Node n = nodes.get(name);
		if(n == null)
			throw new NoSuchElementException("There is no node named "+ name);
		return n;
	}
	
	/**
	 * Standard linear search function looking for a character inside the alphabet
	 * @return -1 if not found, index of s else.
	 */
	protected int findInAlphabet(char s) {	
		for(int i=0; i<alphabet.length; i++)
			if(alphabet[i] == s)
				return i;
		return -1;
	}	
	
	private boolean hasDuplicates(char[] arr) { //checks alphabet validity
		for(int i=0; i<=arr.length-1; i++) 
			for(int j=0; j<=arr.length-1; j++) 
				if(i!=j) 
					if(arr[i] == arr[j])
						return true;			
		return false;
	}
	
	// Node interface
	protected abstract class Node {
		
		protected final char name;
		
		Node(char name){
			this.name = name;
		}
		
		@Override
		public abstract String toString();

	}
}
