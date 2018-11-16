package org.qmpm.evaluation.trie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.qmpm.evaluation.processmining.GenericProcessModel;
import org.qmpm.evaluation.processmining.GenericProcessModel.ModelState;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.exceptions.ProcessTransitionException;
import org.qmpm.logtrie.trie.TrieImpl;

public class ModelTrie extends TrieImpl {

	private GenericProcessModel model;
	private int failedTransitions = 0;

	public ModelTrie() {
		super();
	}	
	
//	public ModelTrie(GenericProcessModel pm) {
//		super();
//		setup(pm);
//	}
	
	
	public ModelTrie(GenericProcessModel pm) {
		super();
		setup(pm);
	}

	public void setup(GenericProcessModel pm) {
		model = pm;
		if (model != null) {
			ModelState initialState = model.getInitialState();
			((ModelNode) root).setStateAbbrev(model.getStateAbbrev(initialState));
			((ModelNode) root).setNumValidMoves(initialState.getValidMoves().size());
			initialState.addNode((ModelNode) root);
		}
	}
	
	@Override
	public void toDoAtEndOfInsert(Node n) {
	}
	
	@Override
	public Node createNode(ElementLabel parentEdgeLabel, Node parent) throws ProcessTransitionException {
		
		if (model == null) {
			System.out.println("model==null"); 
			throw new ProcessTransitionException("Empty model!");
		}
		
		String stateAbbrev = "S";
		String parStateAbbrev = ((ModelNode) parent).getStateAbbrev();
		
		try {
			stateAbbrev = model.processTransition(parStateAbbrev, parentEdgeLabel);
		} catch (ProcessTransitionException e) {
			failedTransitions++;
			throw e;
		}
		
		ModelState state = model.getState(stateAbbrev);
		ModelState parState = model.getState(parStateAbbrev);
		ModelNode node = new ModelNode(parentEdgeLabel, parent, this, stateAbbrev);
		node.setStateAbbrev(stateAbbrev);
		node.setNumValidMoves(state.getValidMoves().size());
		//Framework.permitOutput();
		//System.out.println(state.getValidMoves());
		//Framework.resetQuiet();
		
		state.addNode((ModelNode) node); 
		parState.addOutgoingAct(parentEdgeLabel);
		
		return node;
	}

	@Override
	public String draw() {
		
		String out = "";
		List<Node> frontier = new ArrayList<>();
		List<Node> newFrontier = new ArrayList<>();
		frontier.add(this.getRoot());
		out += "0: " + getRoot().getParentEdgeLabel().toString() + "(" + getRoot().getVisits() + ")[" + ((ModelNode) getRoot()).getStateAbbrev() + "], ";
		
		for (int i=1; i<=this.getLongestBranch(); i++) {
			out += "\n" + i + ": ";
			newFrontier.clear();
			for (Node node : frontier) {
				newFrontier.addAll((Collection<? extends Node>) node.getChildren().values());
				for (Node child : node.getChildren().values()) {
					ModelNode modChild = (ModelNode) child;
					out += modChild.getParentEdgeLabel().toString() + "(" + modChild.getVisits() + ")[" + modChild.getStateAbbrev() + "]" + (modChild.getIsEnd() ? "E" : " ") + ", ";
				}
				out += "  ";
			}		
			frontier.clear();
			frontier.addAll(newFrontier);
		}
		
		return out;
	}
	
	// TODO Find better solution to setting root
	@Override
	public void setRoot(ElementLabel rootActivity, String rootName, Node rootParent) {
		root = new ModelNode(rootActivity, rootParent, this);
    	root.setName(rootName);
    	//this.root.setParent((XESLogNode) rootParent);
	}

	public GenericProcessModel getModel() {
		return model;
	}

	public void setModel(GenericProcessModel pm) {
		model = pm;
	}
	
	public int getFailedTransitions() {
		return failedTransitions;
	}

}
