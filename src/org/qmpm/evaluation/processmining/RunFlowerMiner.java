package org.qmpm.evaluation.processmining;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;
import org.qmpm.logtrie.trie.Trie;

public class RunFlowerMiner extends RunMiner {

	//private String path;
	private long runTime = -1;	
	private Set<String> activities = new HashSet<>();
	
	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException, FileLoadException {
		
		XLog log;
		
		if (xLog != null) {
			log = xLog;
			activities = XESTools.getAllActivities(log);
		} else if (path != null) {
			try {
				log = XESTools.loadXES(path, true);
				activities = XESTools.getAllActivities(log);
			} catch (FileLoadException e) {
				e.printStackTrace();
				throw e;
			}
		} else {
			try {
				
				for (ElementLabel e : t.getElementLabels()) {
					activities.add(e.toString());
				}
			} catch (Exception e1) {
				System.out.println(e1.getMessage());
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		if (activities == null) throw new LabelTypeException("");
		
		long startMining = System.nanoTime();

		Petrinet flower = PetrinetFactory.newPetrinet("flower");
		Marking initMarking = new Marking();
		
		Place p = flower.addPlace("p");
	
		for (String a : activities) {
			Transition trn = flower.addTransition(a);
			flower.addArc(p, trn);
			flower.addArc(trn, p);
		}
		
		initMarking.add(p);		
		
		Object[] flowerArray = {flower, initMarking};
				
		runTime = System.nanoTime() - startMining;

		model = new ImperativeModel(flowerArray);
		model.setName(getLabel().toString());

		finished();
		return Outcome.SUCCESS;
	}

	@Override
	public void processArgs(String[] args) {

		for (int i=0; i<args.length; i++) {
			
			switch(i) {
			case 0: 
				path = args[i];
				break;
			}
		}
	}

	@Override
	public MetricLabel getLabel() {
		return MinerLabel.FlowerMiner;
	}

	@Override
	public String parametersAsString() {
		return "";
	}
	
	public long getRunTime() {
		return runTime;
	}
	
}