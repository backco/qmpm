package org.qmpm.evaluation.trie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.elementlabel.LabelFactory;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.trie.NodeImpl;
import org.qmpm.logtrie.trie.Trie;
import org.qmpm.logtrie.trie.TrieImpl;

public class PNPathTrie extends TrieImpl {

	public class PNPathNode extends NodeImpl {
		
		public PNPathNode(ElementLabel parentEdgeLabel, Node parent, Trie trie) {
			super(parentEdgeLabel, parent, trie);
		}

		//private PNPathNode parent;
		//private Map<String, PNPathNode> children = new HashMap<String, PNPathNode>();
		private int pathLength;
		private Transition transition = null;
		private Marking marking;
			
		public PNPathNode (Transition t, Node parent, Trie trie, Marking m) throws LabelTypeException {
			super(LabelFactory.build(t), parent, trie);			
			transition = t;
			marking = m;
		}
		
		public PNPathNode (String activity, Node parent, Trie trie, Marking m) throws LabelTypeException {
			super(LabelFactory.build(activity), parent, trie);
			marking = m;
		}
		//System.out
		
/*
		@Override
		public void addChild(ElementLabel l, Node n) {
			String act = l.toString();
			// Ensure that tau transitions have unique activity names
			if (l.toString().contains("tau")) {
				System.err.println("PNPathTrie.addChild()");
				System.err.println("   n:\n " + n);
				System.err.println("   n.getTransition(): " + ((PNPathNode) n).getTransition());
				System.err.println("   ...getId()       : " + ((PNPathNode) n).getTransition().getId());
				System.err.println("   ...toString      : " + ((PNPathNode) n).getTransition().getId().toString());
				
				act = "tau-" + ((PNPathNode) n).getTransition().getId().toString();
			}
			
			ElementLabel newLabel = null;
			
			try {
				newLabel = LabelFactory.build(act);
			} catch (LabelTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			super.addChild(newLabel, n);
		}
		*/
		
		public void setParent(Node n) {
			parent = n;
		}
	
		public int getPathLength() {
			return this.pathLength;
		}
		
		public Transition getTransition() {
			return this.transition;
		}
		
		public void setPathLength(int pathLength) {
			this.pathLength = pathLength;
		}
		
		public void setTransition(Transition transition) {
			this.transition = transition;
		}
	
		public Marking getMarking() {
			return marking;
		}
	}
	
	private Set<Node> goalNodes = new HashSet<>();
	private Set<Transition> reachableRealTransitions = new HashSet<>();
	
	public PNPathTrie() {
		super();
		((PNPathNode) root).setPathLength(0);
	}

	private void addGoalNode(PNPathNode node) {
		this.goalNodes.add(node);
	}
	
	public void addReachableRealTransition(Transition t) {
		this.reachableRealTransitions.add(t);
	}
	
	public <A> PNPathNode createNode(String activity, Node parent, Transition t, Marking m) {
		PNPathNode node = null;
		try {
			node = new PNPathNode(t, parent, this, m);
		} catch (LabelTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return node;
	}
	
	public PNPathNode insert(PNPathNode parent, PNPathNode newNode) {
		if (parent.getChildEdgeLabels().contains(newNode.getParentEdgeLabel())) {
			return (PNPathNode) parent.getChildren().get(newNode.getParentEdgeLabel());
		} else {
			PNPathNode node = newNode;
			ElementLabel activity = newNode.getParentEdgeLabel();
			Transition transition = newNode.getTransition();
			node.setParent(parent);
			node.getParent().addChild(activity, node);
			node.getParent().setIsLeaf(false);
			removeGoalNode(node.getParent());
			node.setTransition(transition);
			node.setIsLeaf(true);
			node.setPathLength(((PNPathNode) node.getParent()).getPathLength()+1);
			return node;
		}
	}
	
	public PNPathNode insert(PNPathNode parent, Transition newTransition, Marking m) {
		
		PNPathNode newNode = null
				;
		try {
			newNode = new PNPathNode(newTransition, parent, this, m);
		} catch (LabelTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PNPathNode returnNode = insert(parent, newNode);
		
		return returnNode;
	}

	public PNPathNode insert(PNPathNode parent, Transition newTransition, Marking m, boolean isGoal) {
		
		PNPathNode newNode = null;
		
		try {
			newNode = new PNPathNode(newTransition, parent, this, m);
		} catch (LabelTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PNPathNode returnNode = insert(parent, newNode);
		
		if (isGoal) 
			this.addGoalNode(returnNode);
		
		return returnNode;
	}
	
	public <E> String getActivity(E transition) {
		Transition t = (Transition) transition;
		return t.getLabel();
	}
	
	public Set<Node> getGoalNodes() {
		return goalNodes;
	}
	
	public Set<Transition> getReachableRealTransitions() {
		return this.reachableRealTransitions;
	}
	
	public Set<List<Transition>> getShortestPath() {
		
		//System.err.println("PNPathTrie.getShortestPath()");
		
		int shortestPathLength = -1;
		Set<PNPathNode> shortestPathNodes = new HashSet<>();
		
		//System.err.println("GoalNodes");
		
		for (Node node : getGoalNodes()) {
			//System.err.println(((PNPathNode) node).getTransition().getLabel() + "[" + ((PNPathNode) node).getDepth() + "]"); 
			//PNPathNode p = (PNPathNode) node.getParent();
			//for (int i = ((PNPathNode) node).getDepth(); i>1; i--) {
			//	System.err.println("   " + p.getTransition().getLabel());
				
			//}
			if ((((PNPathNode) node).getDepth()<=shortestPathLength) || (shortestPathLength == -1)) {
				shortestPathLength = ((PNPathNode) node).getDepth();
			}
		}
		
		for (Node node : getGoalNodes()) {
			if (((PNPathNode) node).getDepth() == shortestPathLength)
				shortestPathNodes.add((PNPathNode) node);
		}
		
		Set<List<Transition>> result = new HashSet<>();
		for (PNPathNode node : shortestPathNodes) {
			List<Transition> shortestPath = new ArrayList<>();
			Node n = node;
			while (!(n.getParent() == null)) {
				shortestPath.add(0, ((PNPathNode) n).getTransition());
				n = n.getParent();
			}
			result.add(shortestPath);
		}
		
		return result;
	}

	private void removeGoalNode(Node node) {
		goalNodes.remove(node);
	}
	
	@Override
	public void setRoot(ElementLabel rootActivity, String rootName, Node rootParent) {
		root = new PNPathNode(rootActivity, rootParent, this);
		root.setName(rootName);
		((PNPathNode) root).setPathLength(0);
	}
	
	/*
 	public <S> void setRootState(S marking) {
 		Marking initialMarking = (Marking) marking;
 		String abbrevS = putState(initialMarking);
 		this.getRoot().setState(abbrevS);
 	}
 	*/
}
