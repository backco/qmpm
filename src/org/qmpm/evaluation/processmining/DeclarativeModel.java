package org.qmpm.evaluation.processmining;

import java.util.Set;

import org.processmining.plugins.declareminer.visualizing.DeclareMinerOutput;
import org.qmpm.evaluation.processmining.Declare2DCR;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.exceptions.ProcessTransitionException;

import datamodel.Graph;
import minerful.concept.ProcessModel;
import net.dcrgraphs.core.Automata;
import net.dcrgraphs.core.DCRGraph;
import net.dcrgraphs.core.DCRMarking;
import net.dcrgraphs.core.Automata.State;

public class DeclarativeModel extends GenericProcessModel {
	
	private class DeclarativeState extends ModelState {
		
		State autState = null;
		DCRMarking dcrMarking = null;
		
		DeclarativeState(State s) {
			super();
			autState = s;
		}
		
		DeclarativeState(DCRMarking m) {
			super();
			dcrMarking = m;
		}
		
		public State getAutomataState() {
			return autState;
		}

		public DCRMarking getDCRMarking() {
			return dcrMarking;
		}
		
		@Override
		public Set<String> getValidMoves() {
			
			if (autState == null && dcrMarking != null) {
				return dcrGraph.getAllEnabled(dcrMarking);
			} else if (autState != null && dcrMarking == null) {
				return autModel.validMoves(autState);
			} else return null;
		}

		public boolean isDCR() {
			
			return (autState == null && dcrMarking != null);
		}
		
		public boolean isAutomata() {
			
			return (autState != null && dcrMarking == null);
		}
	
		@Override
		protected boolean customEquals(Object o) {
			
			if (o instanceof DeclarativeState) {
				return ((isAutomata() ? autState.equals(((DeclarativeState) o).getAutomataState()) : false) 
						|| (isDCR() ? dcrMarking.equals(((DeclarativeState) o).getDCRMarking()) : false));
			} else if (o instanceof State) {
				return autState.equals(o);
			} else if (o instanceof DCRMarking) {
				return dcrMarking.equals(o);
			} else {
				return false;
			}
		}
		
		@Override
		public String customToString() {
			
			if (isAutomata()) {
				return autState.toString();
			} else if (isDCR()) {
				return dcrGraph.toString();
			} else return "ERROR";
		}
	}
	//private enum ModelType { DCRGraph, Declare, MINERful, PetriNet }; 
	
	//private ModelType type;
	//private String name = "";
	//private String log = "";
	//private Map<MinerLabel.MinerOption, Number> minerParameters = new HashMap<MinerLabel.MinerOption, Number>();
	//private Map<AttributeLabel, Number> modelAttributes = new HashMap<AttributeLabel, Number>();
	private Automata autModel = null;
	private DCRGraph dcrGraph = null;
	private final ModelState initialState;
	private final int numOfActivities;
	
	public DeclarativeModel(DCRGraph dg) {
		
		//type = ModelType.DCRGraph;
		dcrGraph = dg;
		//model = new Automata();
		//model.addDCRGraph(dcrGraph);
		currentState = new DeclarativeState(dcrGraph.marking);
		initialState = currentState;
		currentStateAbbrev = getStateAbbrev(currentState);
		numOfActivities = dg.getActivities().size();
	}
	
	public DeclarativeModel(DeclareMinerOutput declareMap) {
		//type = ModelType.Declare;
		autModel = Declare2DCR.convert(declareMap);
		currentState = new DeclarativeState(autModel.currentState());
		initialState = currentState;
		currentStateAbbrev = getStateAbbrev(currentState);
		numOfActivities = declareMap.getAllActivities().size();
	}

	public DeclarativeModel(ProcessModel minerfulModel) {
		//type = ModelType.MINERful;
		autModel = MINERful2DCR.convert(minerfulModel);
		currentState = new DeclarativeState(autModel.currentState());
		initialState = currentState;
		currentStateAbbrev = getStateAbbrev(currentState);
		
		numOfActivities = minerfulModel.getProcessAlphabet().size();
	}

	
	public ModelState getInitialState() {
		return initialState;
	}
	
	@Override
	public String processTransition(String stateAbbrev, ElementLabel l) throws ProcessTransitionException{
		
		DeclarativeState oldState = (DeclarativeState) stateAbbrevMap.get(stateAbbrev);
		DeclarativeState newState = null;
		
		try {
			newState = move(l.toString(), oldState, oldState.getValidMoves());
		} catch (ProcessTransitionException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return getStateAbbrev(newState);
	}
	
	private DeclarativeState move(String event, DeclarativeState oldState, Set<String> validMoves) throws Exception, ProcessTransitionException {
		
		if (oldState.isAutomata()) {
			State os = oldState.getAutomataState();
			State ns = autModel.move(event, os, validMoves);
			if (ns == null) {
				throw new ProcessTransitionException("Unable to process transition " + event);
			}
			return new DeclarativeState(ns);
		} else if (oldState.isDCR()) {
			DCRMarking os = oldState.getDCRMarking();
			if (!dcrGraph.enabled(os, event)) {
				throw new ProcessTransitionException("Unable to process transition " + event); 
			} else {
				return new DeclarativeState(dcrGraph.execute(os, event));
			}
		} else throw new ProcessTransitionException("Unable to process transition " + event);
	}

	public int getNumOfConstraints() {
		
		if (autModel == null && dcrGraph != null) {
			return dcrGraph.getNumOfRelations();
		} else if (autModel != null && dcrGraph == null) {
			return autModel.getNumOfAutomata();
		} else return -1;
	}


	@Override
	public int getNumOfActivities() {
		return numOfActivities;
	}

	
	@Override
	public int getNumOfNodes() {
		return getNumOfActivities();
	}

	
	@Override
	public int getNumOfEdges() {
		return getNumOfConstraints();
	}

	@Override
	public boolean isEmpty() {
		return (autModel == null && dcrGraph == null);
	}

}
