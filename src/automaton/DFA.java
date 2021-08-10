package automaton;
import java.util.Arrays;
import java.util.Map.Entry;

/**
 * A class representing a theoretical Deterministic Finite Automaton (DFA), with its own alphabet and nodes.
 *
 * @author dimits
 */
public class DFA extends FiniteAutomaton {
		
	public DFA(char[] alphabet) throws IllegalArgumentException  {
		super(alphabet);
		for (int i=0; i<=alphabet.length-1;i++)
			if (alphabet[i] == EMPTY) 
				throw new IllegalArgumentException ("The character 'empty move' should not be used in a Deterministic Automaton");
			
	}
	
	@Override
	public void connect(char nodeName, char letter, char targetName) throws InvalidNodedException, InvalidTransitionException {
		if(letter == EMPTY)
			throw new InvalidTransitionException("The constant of the empty move should not be used in a Deterministic Automaton");
		
		((DFANode) node(nodeName)).addNeighbors(letter, targetName);
	}
	
	@Override
	protected Node createNode(char name) {
		return new DFANode(name);
	}
	
	/**
	 * Takes any string and runs it through the automaton, returns whether or not it's accepted.
	 * @param word
	 * @return true if accepted, false otherwise
	 * @throws InvalidAutomatonException if any node isn't connected to all possible letters of its alphabet.
	 */
	public boolean isAccepted(String word) throws InvalidAutomatonException {
		word = word.replace(Character.toString(EMPTY), ""); //there's no ' ' char so we have to use strings
		
		//state checks
		if(!isComplete())
			throw new InvalidAutomatonException(errorMessage()
					+"\nThis automaton does not represent a finite-state-machine");
		
		if(size() == 0)
			throw new InvalidAutomatonException("This automaton is empty");
		
		if(last.isEmpty()) { 
			throw new InvalidAutomatonException("*Warning*: No accept-states specified: no word can be accepted");
		}
		
		//calculation		
		DFANode curr = (DFANode) first;
		int index = 0;
		
		while(index != word.length()) 
			curr = (DFANode) node(curr.getNeighbour(word.charAt(index++)));
		
		if(isLast(curr.name)) 
			return true;
		else
			return false;	
	}
	

	private boolean isComplete() { //check to see if all nodes are connected to exactly one other node
		for(Entry<Character, FiniteAutomaton.Node> n : nodes.entrySet()) 
			if(!((DFANode) n.getValue()).isComplete())
				return false;
		
		return true;
	}
	
	private String errorMessage() { //to be called when isComplete() check fails
		StringBuilder sb = new StringBuilder("");
		
		for(Entry<Character, FiniteAutomaton.Node> e : nodes.entrySet()) {
			DFANode n = (DFANode) e.getValue();
			if(!n.isComplete())
				sb.append(String.format("Missing path in node %c: \n %s ", n.name,n));
		}
			
		return sb.toString();
	}

	/**
	 * A class representing a Node connected to another Node or itself for every letter of a given alphabet
	 */
	private class DFANode extends Node {
		
		private final char[] adjacents;
		
		/**
		 * Construct a new node and provide a new alphabet for all future nodes.
		 * @param name
		 * @param new_alphabet a char array representing all the letters that can be read by an automaton
		 */
		DFANode(char name) {			
			super(name);
			this.adjacents = new char[DFA.this.alphabet.length];
		}
		
		/**
		 * Build a copy of the node
		 * @param n the node to be copied
		 */
		DFANode(DFANode n) {
			super(n.name);
			adjacents = new char[n.adjacents.length];
			System.arraycopy(n.adjacents, 0, adjacents, 0, n.adjacents.length);
		}
		
		/**
		 * Adds a connection between this node and the targetNode via a letter
		 * @param letter the letter that leads the transition
		 * @param targetName the name of the node to be connected
		 * @throws InvalidTransitionException if the letter doesn't belong to the alphabet
		 */
		void addNeighbors(char letter, char targetName) throws InvalidTransitionException {
			int index = indexInAlphabet(letter);
			
			if(index == -1) 
				throw new InvalidTransitionException("Letter " + letter + " is not in the alphabet " + Arrays.toString(alphabet));
			
			adjacents[index] = targetName;
		}
		
		/**
		 * Returns the name of the node corresponding to the given letter of the alphabet.
		 * 
		 * @param letter the transition's character
		 * @return The referenced node's name
		 * @throws InvalidTransitionException if the input was outside of the alphabet
		 */
		char getNeighbour(char letter) throws InvalidTransitionException{
			int index = int index = indexInAlphabet(letter);
			
			if(index == -1) 
				throw new InvalidTransitionException("Letter " + letter + " is not in the alphabet " + Arrays.toString(alphabet));
			
			return adjacents[index];
		}
		
		/**
		 * Checks whether or not every letter of the alphabet is connected to one node.
		 * @return true if the node is connected to another node for every character in the alphabet.
		 */
		boolean isComplete() {
			for(char c_in : adjacents) 
				if(c_in == '\u0000')
					return false;
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder str = new StringBuilder(this.name);
			for(int i = 0; i<alphabet.length;i++) {
				char neighbour = getNeighbour(alphabet[i]);
				str.append(String.format("\t: %c --> %c\n", alphabet[i], (neighbour!='\u0000')?neighbour:'?'));
			}
				
			return str.toString();
		}
				
	}

}
