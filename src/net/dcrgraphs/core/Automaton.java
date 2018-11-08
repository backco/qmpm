package net.dcrgraphs.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Automaton {
	
	private class Edge
	{
		@SuppressWarnings("unused")
		private String src;
		private String lbl;
		private String trg;
		
		public Edge (String s, String l, String t)
		{
			src = s;
			lbl = l;
			trg = t;
		}
		
		@Override
	    public int hashCode() {        
	        return this.lbl.hashCode();
	    }		
	}
	
	
	/*
	Map<String, DCRMarking> states = new HashMap<String, DCRMarking>(); // unsure if we need to know the markings, but in case it is useful...
	Set<String> acceptingStates = new HashSet<String>();
	Map<String, HashSet<String>> edges = new HashMap<String, HashSet<String>>();
	Map<String, HashSet<String>> reverseEdges = new HashMap<String, HashSet<String>>(); // could be useful for implementing an A* type search?
	*/
	DCRGraph dcrgraph;
	Set<String> states = new HashSet<String>(); // unsure if we need to know the markings, but in case it is useful...
	Set<String> acceptingStates = new HashSet<String>();
	Map<String, HashSet<Edge>> edges = new HashMap<String, HashSet<Edge>>();
	String currentState = "";
	//Map<Integer, HashSet<Integer>> reverseEdges = new HashMap<Integer, HashSet<Integer>>();
	
	public Automaton(DCRGraph g)
	{
		dcrgraph = g;		
		Build();
	}
	
	private void Build()
	{
		
		//System.out.println("Removing unconstrained events.");
		//dcrgraph.removeUnconstrianedEvents();
		
		//System.out.println("Building autmaton.");
		DCRMarking m;
		Queue<DCRMarking> fringe = new LinkedList<DCRMarking>();
		
		//Set<String> exploredStates = new HashSet<String>();		
		fringe.add(dcrgraph.marking);
		currentState = dcrgraph.marking.toString();
		states.add(currentState);
		edges.put(currentState, new HashSet<Edge>());

		while(!fringe.isEmpty())
		{
			//System.out.println("fringe: " + fringe);
			m = fringe.poll();
			String s = m.toString();
			
			//System.out.println(m);

			//exploredStates.add(s);
			//states.add(s);
			if (m.IsAccepting())
			{
				acceptingStates.add(s);
			}
			for (String e : dcrgraph.getAllEnabled(m))
			{
				String l = dcrgraph.name(e);
				DCRMarking m2 = dcrgraph.execute(m, e);
				String t = m2.toString();
				edges.get(s).add(new Edge(s, l, t));
				if (!states.contains(t))
				{
					fringe.add(m2);
					states.add(t);
					edges.put(t, new HashSet<Edge>());
				}
			}
		}
		/*
		System.out.println("Automaton build: ");	
		System.out.print("States: ");
		for (String s : states)
			{
				System.out.println(s  + ";");
				System.out.println("Hashcode: " + s.hashCode());
				if (s == currentState) System.out.println("INITIAL STATE");
				System.out.println("Edges: ");
				for (Edge e : edges.get(s))
				{
					System.out.print("===" + e.lbl  + "===>");
					System.out.println(e.trg.hashCode());
				}
				System.out.println("");		
				
			}
		System.out.println("");*/
	}
	
	public Set<String> inValidMoves(String src)
	{				
		//System.out.println("Looking for edge: " + src.hashCode() + " in: ");
		//for (String edge : edges.keySet()) {
		//	System.out.println("   " + edge.hashCode());
		//}
		
		Set<String> result = new HashSet<String>(this.dcrgraph.names());
		for (Edge v : edges.get(src))
			result.remove(v.lbl);
		return result;
	}
	
	
	public String Move(String src, String e) throws Exception
	{
		if (!dcrgraph.names().contains(e)) return src;
		
		for (Edge v : edges.get(src))
			if (v.lbl.equals(e))
			{
				return v.trg;				
			}
		throw new Exception("Invalid move!");
	}
	
	
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " Automaton {" + NEW_LINE);
		result.append(" States: ");
		for (final String s : states) {
			result.append(s + "; ");
			result.append(NEW_LINE);
			for (final Edge e : edges.get(s)) {
				result.append("--" + e.lbl + "--> " + e.trg);
				result.append(NEW_LINE);
			}
		}

		return result.toString();
	}
	
	
	/*
	public Set<String> possibleMoves()
	{
		return dcrgraph.getAllEnabled();
	}
	
	public Set<String> preferredMoves()
	{
		Set<String> result = new HashSet<String>(dcrgraph.getIncludedPending());
		result.retainAll(dcrgraph.getAllEnabled());
		return result; 
	}*/
	
	
	
	
	
}
