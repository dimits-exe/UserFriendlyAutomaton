package automaton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class NFA extends FiniteAutomaton {
	
	/*
	 * This wasn't planned to be included originally, so a lot of code here could be
	 * rewritten in a better way. Still, it's functional and relatively simple, so 
	 * a rewrite is unnecessary.
	 */
	
	public NFA(char[] alphabet) throws IllegalArgumentException {
		super(alphabet);
	}
	
	@Override
	protected Node createNode(char name) {
		return new NFANode(name);
	}
	
	@Override
	public void connect(char nodeName, char letter, char targetName) throws InvalidNodeException, InvalidTransitionException {
		if(letter != EMPTY && findInAlphabet(letter) == -1)
			throw new InvalidTransitionException("The letter " + letter + " does not belong in the alphabet");
		
		((NFANode) node(nodeName)).connect(letter, (NFANode) node(targetName));
	}
	
	/**
	 * Takes any string and runs it through the automaton, returns whether or not it's accepted.
	 * @param word
	 * @return true if accepted, false otherwise
	 * @throws InvalidAutomatonException if there are no nodes in the automaton.
	 */
	@Override
	public boolean isAccepted(String word) throws InvalidAutomatonException {
		if(size() == 0)
			throw new InvalidAutomatonException("This automaton is empty");
		
		boolean oneSuccessful = false; //at least one state was accepted
		NFANode n = (NFANode) first;
		HashSet<NFANode> currentNodes;
		
		for(char c : word.toCharArray()) {
			currentNodes = E(n.move(c));
			
			if(currentNodes.isEmpty()) //if no valid connection
				return false;
			
			for(Node state : currentNodes) {
				if(isLast(state.name))
					oneSuccessful = true;
			}
		} 
		
		return oneSuccessful; 
	}
	
	//return all empty states from all nodes
	private static HashSet<NFANode> E(HashSet<NFANode> nodes) { 
		if(nodes != null)
			for(NFANode node : nodes) 
				nodes.addAll(node.emptyMoves());
		else
			nodes = new HashSet<NFANode>(); //return empty set
		
		return nodes;
	}
	
	
	private class NFANode extends Node {	
		final HashMap<Character,HashSet<NFANode>> adjacents;
		
		NFANode(char name){
			super(name);
			adjacents = new HashMap<Character,HashSet<NFANode>>();
		}
		
		void connect(char letter, NFANode target) {
			if(adjacents.get(letter) == null)  
				adjacents.put(letter, new HashSet<NFANode>());
			 adjacents.get(letter).add(target);
		}
		
		HashSet<NFANode> move(char c) {
			return adjacents.get(c);
		}
		
		//get all e-moves from this node
		HashSet<NFANode> emptyMoves() { 
			HashSet<NFANode> e_nodes = new HashSet<NFANode>();
			if(adjacents.get(EMPTY) != null)
				for(NFANode node : adjacents.get(EMPTY)) 
					e_nodes.addAll(node.emptyMoves()); //keep collecting e-moves for every e-node
			
				
			return e_nodes;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(name + " = ");
			
			//attach connections
			for(Map.Entry<Character,HashSet<NFANode>> e : adjacents.entrySet()) {
				sb.append(String.format("\t%c --> ",e.getKey()));
				for(NFANode n : e.getValue()) sb.append(n.name+",");
				sb.append("\n");
			}
			
			return sb.toString();
		}
	}
}
