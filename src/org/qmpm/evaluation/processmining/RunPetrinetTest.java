package org.qmpm.evaluation.processmining;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.EfficientPetrinetSemantics;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.impl.EfficientPetrinetSemanticsImpl;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;
import org.qmpm.logtrie.trie.Trie;

public class RunPetrinetTest extends RunMiner {

	//private String path;
	private long runTime = -1;
		
	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException {

		long startMining = System.nanoTime();		
		
		Petrinet petrinetM2 = PetrinetFactory.newPetrinet("test");
		Marking initMarkingM2 = new Marking();
		
		Place start = petrinetM2.addPlace("start");
		Place p1 = petrinetM2.addPlace("p1");
		Place p2 = petrinetM2.addPlace("p2");
		Place p3 = petrinetM2.addPlace("p3");
		Place p4 = petrinetM2.addPlace("p4");
		Place end = petrinetM2.addPlace("end");
		
		Transition a = petrinetM2.addTransition("a");
		Transition c = petrinetM2.addTransition("c");
		Transition d = petrinetM2.addTransition("d");
		Transition e = petrinetM2.addTransition("e");
		Transition h = petrinetM2.addTransition("h");
		
		petrinetM2.addArc(start, a);
		petrinetM2.addArc(a, p1);
		petrinetM2.addArc(p1, c);
		petrinetM2.addArc(c, p2);
		petrinetM2.addArc(p2, d);
		petrinetM2.addArc(d, p3);
		petrinetM2.addArc(p3, e);
		petrinetM2.addArc(e, p4);
		petrinetM2.addArc(p4, h);
		petrinetM2.addArc(h, end);
		
		initMarkingM2.add(start);

		Object[] pnObj = {petrinetM2, initMarkingM2};
		model = new ImperativeModel(pnObj);
		model.setName(getLabel().toString());
		
		runTime = System.nanoTime() - startMining;
		
		finished();
		return Outcome.SUCCESS;
	}

	@Override
	public void processArgs(String[] args) {

	}

	@Override
	public MetricLabel getLabel() {
		return MinerLabel.PetrinetTest;
	}

	@Override
	public String parametersAsString() {
		return "";
	}
	
	public long getRunTime() {
		return runTime;
	}
}