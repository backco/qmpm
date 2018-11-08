package net.dcrgraphs.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Automata {
	
	public class State
	{
		private List<String> states = new ArrayList<String>();
		List<String> path;
		
		public State(List<String> p)
		{
			path = new ArrayList<String>(p);
		}
		
		public void addStep(String l)
		{
			path.add(l);
		}
		
		public int size()
		{
			return states.size();
		}
		
		public boolean add(String e)
		{
			return states.add(e);
		}
		
		public String get(int i)
		{
			return states.get(i);
		}
		
		@Override
		public boolean equals(Object obj) {
		    if (obj == null) {
		        return false;
		    }
		    
		    if (!State.class.isAssignableFrom(obj.getClass())) {
		        return false;
		    }
		    final State other = (State) obj;
		    
		    if (!(this.size() == other.size()))
		    	return false;
		    
		    for (int i = 0; i < this.states.size(); i++) {
				if (!this.states.get(i).equals(other.states.get(i)))
						return false;
			}  
		    	    
		    return true;
		}
		
		@Override
		public String toString() {
			final StringBuilder result = new StringBuilder();
			final String NEW_LINE = System.getProperty("line.separator");

			result.append(this.getClass().getName() + " State {" + NEW_LINE);
			
			for(String s : states)
			{
			result.append(s.toString());
			result.append(NEW_LINE + "---------" + NEW_LINE);
			}
			return result.toString();
		}		

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			for( String s : states )
			{
			    result = result * prime + s.hashCode();
			}
			return result;
		}		
	}
	
	List<Automaton> automata = new ArrayList<Automaton>();
	
	public Automata()
	{}
	
	public void addDCRGraph(DCRGraph g)
	{
		automata.add(new Automaton(g));
	}
	
	private boolean isAccepting(State s)
	{
		for (int i = 0; i < this.automata.size(); i++) {
			if (!automata.get(i).acceptingStates.contains(s.get(i)))
				return false;
		}
		return true;
	}
	
	public Set<String> validMoves(State s)
	{
		if (s == null) {
			System.out.println("state == null");
			s = currentState();
		}
		
		Set<String> result = new HashSet<String>();
		
		for (int i = 0; i < this.automata.size(); i++) {
			result.addAll(automata.get(i).dcrgraph.names());
		}
		
		for (int i = 0; i < this.automata.size(); i++) {
			result.removeAll(automata.get(i).inValidMoves(s.get(i)));
		}		
		
		return result;		
	}
		
	public State currentState()
	{
		State result = new State(new ArrayList<String>());		
		for (Automaton a : automata)
		{
			result.add(a.currentState);			
		}
		return result;
	}

	public void permanentMove(String e) throws Exception
	{
		if (!validMoves(currentState()).contains(e))
			return;
		for (Automaton a : automata)
		{
			a.currentState = a.Move(a.currentState, e);			
		}		
	}
	
	
	public State move(String e) throws Exception
	{
		if (!validMoves(currentState()).contains(e))
			return null;

		State result = new State(new ArrayList<String>());
		
		for (Automaton a : automata)
		{
			result.add(a.Move(a.currentState, e));			
		}		
		return result;
	}
	
	
	/*
	 * @author Christoffer
	 * 
	 * Modified move() method which takes a given state and performs a move from there
	 * Also takes validMoves to avoid computing this twice
	 */
	public State move(String e, State state, Set<String> validMoves) throws Exception
	{
		if (!validMoves.contains(e))
			return null;

		State result = new State(new ArrayList<String>());
		
		if (!(state.size() == this.automata.size()))
			return null;
		
		for (int i = 0; i < state.size(); i++)
		{
			Automaton a = this.automata.get(i);
			result.add(a.Move(state.get(i), e));			
		}		
		return result;
	}
	
	/*
	 * @ author Christoffer
	 */
	public int getNumOfAutomata() {
		return this.automata.size();
	}
	
	public List<List<String>> safeMoves(State s) throws Exception
	{		
		System.out.println("safeMoves");
		ArrayList<List<String>> result = new ArrayList<List<String>>();
		if (isAccepting(s))
		result.add(new ArrayList<String>());
		
		for (String e : validMoves(s))
		{
			State newstate = move(e);
			List<String> path = BFS(newstate);
			if (path != null)
			{
				path.add(0, e);
				result.add(path);
			}
		}
		
		Collections.sort(result, new Comparator<List<String>>(){
		    public int compare(List<String> a1, List<String> a2) {
		        return a1.size() - a2.size();
		    }});
		
		// Collections.sort(arrayList, new SizeComarator());
		return result;
	}
	
	public List<String> BFS(State s) throws Exception
	{	
		System.out.println("BFS");

		if (isAccepting(s))
			return new ArrayList<String>();
		
		
		Queue<State> fringe = new LinkedList<State>();		
		Set<State> exploredStates = new HashSet<State>();		
		fringe.add(s);		
		exploredStates.add(s);			
		
		while(!fringe.isEmpty())
		{
			State current = fringe.poll();			
		    //System.out.println("Trying:" + current.toString());
		    //System.out.print("Valid Moves: {");
			for (String e : validMoves(current))
			{
				//System.out.print(e + "; ");
				State newState = new State(current.path);
				newState.addStep(e);
				
				for (int i = 0; i < this.automata.size(); i++) {
					Automaton a = automata.get(i);
					newState.add(a.Move(current.get(i), e));
				}
				
				if (!exploredStates.contains(newState))
				{
					if (isAccepting(newState))
					{
						//System.out.println("Found a path");
						return newState.path;
					}
					fringe.add(newState);
					exploredStates.add(newState);
				}
				
			}				
			//System.out.println("Fring size: " + fringe.size() + " Explored States size: " + exploredStates.size());
		}		
		return null;
	}	
	
	

	public List<String> BFS() throws Exception
	{	
		State initialState = new State(new ArrayList<String>());		
		for (Automaton a : automata)
		{
			initialState.add(a.currentState);			
		}	
		return BFS(initialState);
	}
	
	
}
