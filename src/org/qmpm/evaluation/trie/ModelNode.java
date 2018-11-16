package org.qmpm.evaluation.trie;

import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.trie.Trie;
import org.qmpm.logtrie.trie.Trie.Node;
import org.qmpm.logtrie.trie.NodeImpl;

public class ModelNode extends NodeImpl {

	//private ModelTrieNode parent;
	//private Map<String, ModelTrieNode> children = new HashMap<String, ModelTrieNode>();
	//private Set<String> escapingEdges = null;
	//private Set<String> validMoves = new HashSet<String>();
	//private String state;
	//private ModelState state;
	private String stateAbbrev;
	private int numValidMoves = -1;
	
	/*
	ModelNode(ElementLabel parentEdgeLabel, Node parent, TrieImpl trie, ModelState s) {
		super(parentEdgeLabel, parent, trie);
		state = s; 
		// TODO Auto-generated constructor stub
	}
	*/

	ModelNode(ElementLabel parentEdgeLabel, Node parent, ModelTrie trie, String stateAbbrev) {
		super(parentEdgeLabel, parent, trie);
		this.stateAbbrev = stateAbbrev; 
		// TODO Auto-generated constructor stub
	}
	
	ModelNode(ElementLabel parentEdgeLabel, Node parent, ModelTrie trie) {
		super(parentEdgeLabel, parent, trie);
	}
	/*
	public void addEscapingEdge(String escapingEdge) {
		this.escapingEdges.add(escapingEdge);
	}
	
	public void addValidMove(String move) {
		this.validMoves.add(move);
	}
	
	public void addValidMoves(Set<String> moves) {
		this.validMoves.addAll(moves);
	}
	
	public Set<String> getEscapingEdges() {
		return this.escapingEdges;
	}
	
	public int getNumValidMoves() {
		return this.validMoves.size();
	}
	
	public ModelState getState() {
		return state;
	}
	 */
	
	public String getStateAbbrev() {
		return stateAbbrev;
	}
	/*
	public Set<String> getValidMoves() {
		return state.getValidMoves();
	}
	
	public void removeEscapingEdge(String escapingEdge) {
		this.escapingEdges.remove(escapingEdge);
	}
	
	public void setEscapingEdges() {
		this.escapingEdges = new HashSet<String>();
	}

	public void setEscapingEdges(Set<String> escapingEdges) {
		this.escapingEdges = escapingEdges;
	}

	public void setState(ModelState s) {
		state = s;
	}
	*/
	
	public void setStateAbbrev(String s) {
		stateAbbrev = s;
	}
	
	public int getNumValidMoves() {
		return numValidMoves;
	}

	public void setNumValidMoves(int n) {
		numValidMoves = n;
	}
}
