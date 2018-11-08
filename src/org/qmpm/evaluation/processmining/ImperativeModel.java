package org.qmpm.evaluation.processmining;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.processmining.models.semantics.petrinet.EfficientPetrinetSemantics;
import org.processmining.models.semantics.petrinet.Marking;
import org.qmpm.evaluation.trie.PNPathTrie;
import org.qmpm.evaluation.trie.PNPathTrie.PNPathNode;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.exceptions.ProcessTransitionException;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.impl.EfficientPetrinetSemanticsImpl;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;

public class ImperativeModel extends GenericProcessModel {

	private class ImperativeState extends ModelState {
		
		Marking marking;

		ImperativeState(Marking m) {
			marking = m;
		}
		
		public Marking getState() {
			return marking;
		}
		
		@Override
		public Set<String> getValidMoves() {

			//System.out.println("ImperativeState.getValidMoves()");
			
			Set<String> validMoves = new HashSet<>();
			
			for (Transition t : enabledActivities(marking)) {
				validMoves.add(t.getLabel());
			}
			
			return validMoves;
		}

		@Override
		protected boolean customEquals(Object o) {

			if (o instanceof ImperativeState) {
				return marking.equals(((ImperativeState) o).getState());
			} else if (o instanceof Marking) {
				return marking.equals(o);
			} else {
				return false;
			}
		}
		
		@Override
		public String customToString() {
			
			return marking.toString();
		}
		
	}
	
	
	// A TransitionMap allows accessing the sources and targets of a Transition object, not possible with the original Transition class
	private class TransitionMap {
		
		private Set<Place> sources = new HashSet<Place>();
		private Set<Place> targets = new HashSet<Place>();
		private String label; // TODO: change to ElementLabel
		Transition transition;
		
		public TransitionMap(Transition t) {
			this.transition = t;
			this.label = t.getLabel();
			/*
			try {
				this.label = t.getLabel();//LabelFactory.build(t).toString();
			} catch (LabelTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		
		public void addSource(Place src) {
			this.sources.add(src);
		}
		
		public void addTarget(Place trg) {
			this.targets.add(trg);
		}
		
		public String getLabel() {
			return this.label;
		}
		
		public Set<Place> getSources() {
			return this.sources;
		}
		
		public Set<Place> getTargets() {
			return this.targets;
		}
		
		@SuppressWarnings("unused")
		public Transition getTransition() {
			return this.transition;
		}
		
		@Override
		public String toString() {
			String out = "Transition: " + getLabel() + System.lineSeparator();
			out += "   Sources:" + System.lineSeparator();
			for (Place src : getSources()) {
				out += "      " + src.getLabel() + System.lineSeparator();
			}
			out += "   Targets:" + System.lineSeparator();
			for (Place trg : getTargets()) {
				out += "      " + trg.getLabel() + System.lineSeparator();
			}
			return out;
		}
	}
	
	//private enum ModelType { DCRGraph, Declare, MINERful, PetriNet }; 
	
	//private ModelType type;
	//private String name = "";
	//private String log = "";
	//private Map<MinerLabel.MinerOption, Number> minerParameters = new HashMap<MinerLabel.MinerOption, Number>();
	//private Map<AttributeLabel, Number> modelAttributes = new HashMap<AttributeLabel, Number>();
	private final Petrinet model;
	private EfficientPetrinetSemantics semantics;
	private final Marking initialMarking;
	private final ModelState initialState;
	private PNPathTrie pathTrie;
	private HashMap<Transition, TransitionMap> transitionMaps = new HashMap<Transition, TransitionMap>();
	//private final DCRGraph dcrGraphModel;
	//private final DeclareMinerOutput declareMinerModel;
	//private final ProcessModel minerfulModel;
	//private final Object[] petriNetModel;
	
	public ImperativeModel (Object[] petriNet) {
		//type = ModelType.PetriNet;
		model = (Petrinet) petriNet[0];
		buildTransitionMaps();
		initialMarking = (Marking) petriNet[1];
		currentState = new ImperativeState(initialMarking);
		initialState = currentState;
		currentStateAbbrev = getStateAbbrev(currentState);
		semantics = new EfficientPetrinetSemanticsImpl(model, initialMarking);
		semantics.setCurrentState(initialMarking);
	}
	
	/*
	public void setInitialState(Object initialState) {
		ModelState init = new ImperativeState((Marking) initialState);
		stateAbbrevMap.put(initStateAbbrev, init);
	}
	*/
	
	public ModelState getInitialState() {
		return initialState;
	}
		
	
	@Override
	public String processTransition(String stateAbbrev, ElementLabel l) throws ProcessTransitionException {
		
		ModelState oldStateMS = getState(stateAbbrev);
		ImperativeState oldState = (ImperativeState) oldStateMS;
		semantics.setCurrentState(oldState.getState());
		Transition activityAsTransition = null;
		Set<Transition> enabledTransitions =  enabledActivities(oldState.getState()); 
		
		for (Transition t :enabledTransitions) {
			if (t.getLabel().equals(l.toString())) {
				activityAsTransition = t;
				break;
			}
		}
		
		if (!(activityAsTransition == null)) {
			executeWithSilentTransitions(activityAsTransition);
		} 
		
		else { throw new ProcessTransitionException("Cannot execute " + l.toString() + " in current state");
			
			/*
			System.out.println("*** Found activity in log: " + l.toString() + " which cannot be executed in model. Model should be perfectly fitting.");
			System.out.println("***    Parent marking    : " + oldState.toString());
			System.out.println("***    Current marking   : " + this.semantics.getCurrentState());
			System.out.println("***    Enabled activities: " + enabledTransitions);
			//System.out.println("***    Trace             : " + Metrics.xTraceToString(this.getVisitingPrefix(node)));
			System.out.println("");
			System.out.println("MODEL");
			System.out.println("");
			System.out.println(transitionMaps);
			*/
		}
		
		
		Marking markingCurrent = semantics.getCurrentState();
		String newState = getStateAbbrev(new ImperativeState(markingCurrent));
		//node.setState(state);
		
		// get enabledActivities, convert to Set of Strings
		/*
		Set<Transition> newEnabledActivities = enabledActivities(markingCurrent);
		for (Transition t : newEnabledActivities) {
			node.addValidMove(t.getLabel());
		}
		*/
		if (newState == null) throw new ProcessTransitionException("Unable to process transition " + l + " from state " + stateAbbrev);
		
		return newState;
	}
	
	private Set<Transition> enabledActivities(Marking marking) {

		//System.out.println("");
		//System.out.println("\\/===========================================\\/");	
		//System.out.println("");
		
		//System.out.println("ImperativeModel.enabledActivities()");
		
		pathTrie = new PNPathTrie();
		buildPathTrie(null, marking, new HashSet<Marking>(), (PNPathNode) pathTrie.getRoot(), 0);
		//pathTrie.draw();
		Set<Transition> result = pathTrie.getReachableRealTransitions();
		semantics.setCurrentState(marking);
		
		//System.out.println("ENABLED ACTIVITIES IN MARKING " + marking + ": " + result );
		//System.out.println("");
		//System.out.println("^^===========================================^^");	
		//System.out.println("");
		
		return result;
	}
	
	public Marking executeTransitionMap(Marking originalMarking, TransitionMap tm) {
		
		//System.out.println("ImperativeModel.executeTransitionMap()");
		//System.out.println("   Trying to execute " + tm.label + " in " + originalMarking);
		//System.out.println("   semantics.getCurrentMarking():" + semantics.getCurrentState());
		
		if (originalMarking.containsAll(tm.getSources())) {
			
			Marking newMarking = new Marking();
			newMarking.addAll(originalMarking);	
			newMarking.removeAll(tm.getSources());
			newMarking.addAll(tm.getTargets());
			return newMarking;
			
		} else {
			
			System.out.println("");
			System.out.println("Transition is not enabled in this marking: " + originalMarking);
			System.out.println(tm.toString());
			System.out.println("OriginalMarking");
			for (Place p : originalMarking)
				System.out.println(p);
			System.out.println("tm.getSources()");
			for (Place p : tm.getSources())
				System.out.println(p);
			System.exit(1);
			return null;
		}
	}
	
	private void executeWithSilentTransitions(Transition t) {
		
		//System.out.println("ImperativeModel.executeWithSilentTransitions()");
		
		Marking originalMarking = semantics.getCurrentState();
		this.pathTrie = new PNPathTrie();
		buildPathTrie(t, originalMarking, new HashSet<Marking>(), (PNPathNode) pathTrie.getRoot(), 0);
		Set<List<Transition>> shortestPaths = pathTrie.getShortestPath();
		if (shortestPaths.size() == 0) {
			System.out.println("No shortest path (via silent transitions) to transition found!");
			System.out.println("Transition: " + t.getLabel() + "(" + t.getId().hashCode() + ")");
		}
		for (Transition transition : shortestPaths.iterator().next()) {
			// EXECUTE TRANSITIONS
			Marking oldMarking = semantics.getCurrentState();
			Marking newMarking = executeTransitionMap(oldMarking, transitionMaps.get(transition));
			semantics.setCurrentState(newMarking);
		}
	}
	
	private void buildPathTrie(Transition t, Marking marking, Set<Marking> exploredMarkings, PNPathNode lastNode, int recursionDepth) {
		
		//System.out.println("");
		//System.out.println("");
		//System.out.println("ImperativeModel.buildPathTrie() [" + lastNode.getPathLength() + "]");
		//System.out.println("BUILDING PATH TRIE FOR MARKING: " + marking);
		Set<Marking> newExploredMarkings = new HashSet<Marking>();
		newExploredMarkings.addAll(exploredMarkings);
		newExploredMarkings.add(marking);
		Set<Transition> realTransitions = new HashSet<Transition>();
		Set<Transition> silentTransitions = new HashSet<Transition>();
		
		//System.out.println("   marking: " + marking);
		//System.out.println("   semantics.getCurrentState(): " + semantics.getCurrentState());
		//System.out.println("   semantics.getExecutableTransitions(): " + semantics.getExecutableTransitions());
		//System.out.println(semantics.getExecutableTransitions());
		
		semantics.setCurrentState(marking);
		
		for (Transition enabled : semantics.getExecutableTransitions()) {
			if (enabled.getLabel().contains("tau")) {
				//System.out.println("FOUND TAU TRANSITION: " + enabled.getLabel());
				silentTransitions.add(enabled);
			} else {
				realTransitions.add(enabled);
			}
		}
		
		//System.out.println("   realTransitions: " + realTransitions);
		//System.out.println(realTransitions);
		
		for (Transition realTransition : realTransitions) {
			semantics.setCurrentState(marking);
			boolean isGoal  = false;
			String activity = realTransition.getLabel();
			if (!(t == null) && (activity.equals(t.getLabel()))) {
				isGoal = true;
			}
			Marking m = semantics.getCurrentState();
			pathTrie.insert(lastNode, realTransition, m, isGoal);
			pathTrie.addReachableRealTransition(realTransition);
		}
		
		//System.out.println("   silentTransitions: " + silentTransitions);
		//System.out.println(silentTransitions);
		
		for (Transition silentTransition : silentTransitions) {
			
			//System.out.println("semantics.setCurrentState(" + marking + ")");
			semantics.setCurrentState(marking);
			Marking m = semantics.getCurrentState();
			//if (semantics.getExecutableTransitions().contains(silentTransition)) {
			Marking newMarking = executeTransitionMap(marking, transitionMaps.get(silentTransition));
			
			if (!(exploredMarkings.contains(newMarking))) {	
				semantics.setCurrentState(newMarking);
				PNPathNode newNode = pathTrie.insert(lastNode, silentTransition, m);
				buildPathTrie(t, newMarking, newExploredMarkings, newNode, recursionDepth+1);
			} 
			//}
		}
		semantics.setCurrentState(marking);
	}

	private void buildTransitionMaps() {
		
		//System.out.println("ImperativeModel.buildTransitionMaps()");
		
		for (Transition t : model.getTransitions()) {
		
			TransitionMap tm = new TransitionMap(t);
			
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : model.getEdges()) {
			
				if (edge.getTarget().equals(t)) {
					tm.addSource((Place) edge.getSource()); 
				} else if (edge.getSource().equals(t)) {
					tm.addTarget((Place) edge.getTarget());
				}

			}
			
			this.transitionMaps.put(t, tm);
		}
		
	}
	
	@Override
	public String toString() {
		
		String out = "=== Petri Net transitions ===\n\n";
		
		for (TransitionMap tm : transitionMaps.values()) {
			
			out += tm.toString() + "\n\n";
		}
		
		return out;
	}

	@Override
	public int getModelSize() {
		return (model == null ? -1 : model.getEdges().size());
	}
}
