package org.qmpm.evaluation.processmining;

import java.util.HashSet;
import java.util.Set;

import org.qmpm.evaluation.trie.ModelNode;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.exceptions.ProcessTransitionException;

import com.google.common.collect.HashBiMap;

public abstract class GenericProcessModel {
	
	public abstract class ModelState {
		
		private Set<ElementLabel> outgoingActs = new HashSet<>();
		private Set<ModelNode> visitingNodes = new HashSet<>();
		public abstract Set<String> getValidMoves();
		protected abstract boolean customEquals(Object o);
		protected abstract String customToString();
		
		@Override
		public boolean equals(Object o) {
			return customEquals(o);
		}
		
		@Override
		public String toString() {
			return customToString();
		}
		
		public void addOutgoingAct(ElementLabel act) {
			outgoingActs.add(act);
		}

		public Set<ElementLabel> getOutgoingActs() {
			return outgoingActs;
		}
		
		public void addNode(ModelNode n) {
			visitingNodes.add(n);
		}
		
		public Set<ModelNode> getNodes() {
			return visitingNodes;
		}
	}	
	
	public static final String initStateAbbrev = "S0";
	protected ModelState currentState;
	protected HashBiMap<String, ModelState> stateAbbrevMap = HashBiMap.create();
	protected String currentStateAbbrev;
	private String name = "";
	private long buildTime = -1;
	
	public abstract String processTransition(String stateAbbrev, ElementLabel l) throws ProcessTransitionException;
	public abstract ModelState getInitialState();
	public abstract int getNumOfActivities();
	public abstract int getNumOfNodes();
	public abstract int getNumOfEdges();
	public abstract boolean isEmpty();
	//public String putState(ModelState s);
	
	public String currentStateAbbrev() {
		return currentStateAbbrev;
	}

	public String getStateAbbrev(ModelState state) {
		
		// TODO: Can this loop be avoided?
		for (ModelState s : stateAbbrevMap.values()) {
			
			if (s.equals(state)) {
				
				return stateAbbrevMap.inverse().get(s);
			}			
		}
		
		String abbrevS = "S" + stateAbbrevMap.size();
		stateAbbrevMap.put(abbrevS, state);
		
		return abbrevS;	
	}

	public ModelState getState(String stateAbbrev) {
		return stateAbbrevMap.get(stateAbbrev);
	}
	
	public Set<ModelState> getStates() {
		return stateAbbrevMap.values();
	}
	
	public ModelState getCurrentState() {
		return currentState;
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public String getName() {
		return name;
	}

	public long getBuildTime() {
		return buildTime;
	}
	
	public void setBuildTime(long t) {
		buildTime = t;
	}
	
	public int getModelSize() {
		return (isEmpty() ? -1 : getNumOfEdges());
		//return (isEmpty() ? -1 : getNumOfNodes() + getNumOfEdges());
	}
}
