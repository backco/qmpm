package org.qmpm.evaluation.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

//import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.id.XIDFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.qmpm.evaluation.enums.CrossValidationType;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.evaluation.metrics.Fitness;
import org.qmpm.evaluation.metrics.GeneralizationEventBased;
import org.qmpm.evaluation.metrics.GeneralizationStateBased;
import org.qmpm.evaluation.metrics.Precision;
import org.qmpm.evaluation.processmining.GenericProcessModel;
import org.qmpm.evaluation.processmining.ImperativeModel;
import org.qmpm.evaluation.trie.ModelTrie;
import org.qmpm.evaluation.trie.ModelTrieMediator;
import org.qmpm.logtrie.exceptions.FileLoadException;
//import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.tools.XESTools;
//import org.qmpm.logtrie.tools.FileInfo;
import org.qmpm.logtrie.trie.Trie;

class EvaluationTest {

	static ModelTrieMediator trieMediator = new ModelTrieMediator();
	static ModelTrieMediator trieMediatorFlat = new ModelTrieMediator();
	static List<Trie> tries = new ArrayList<>();
	static List<Trie> triesFlat = new ArrayList<>();
	static int sigDigs = 4;
	
	static String[] files = {"logs//WIRES_log.xes"};
	static Petrinet petrinetM2 = PetrinetFactory.newPetrinet("test");
	static Marking initMarkingM2 = new Marking();
	
	@BeforeAll
	public static void init() {
		
		try {
			trieMediator.addFiles(Arrays.asList(files));
			trieMediatorFlat.addFiles(Arrays.asList(files));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		trieMediator.loadFiles();
		trieMediator.buildTries(false);
		tries = trieMediator.getTries();		

		trieMediatorFlat.loadFiles();
		trieMediatorFlat.buildTries(true);
		triesFlat = trieMediatorFlat.getTries();
		
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
		Transition h = petrinetM2.addTransition("f");
		
		petrinetM2.addArc(start, a);
		petrinetM2.addArc(p1, a);
		petrinetM2.addArc(p1, c);
		petrinetM2.addArc(c, p2);
		petrinetM2.addArc(p2, d);
		petrinetM2.addArc(d, p3);
		petrinetM2.addArc(p3, e);
		petrinetM2.addArc(e, p4);
		petrinetM2.addArc(p4, h);
		petrinetM2.addArc(h, end);
		
		initMarkingM2.add(start);
	}
	
	//@Test
	void MINERful() {
		//ProcessModel pm = MinerManager.runMINERful("C:\\Users\\wvm405\\phd\\research\\data\\logs\\synthetic\\BPAI\\log1.xes", 1.0, 1.0, false);
		//GenericProcessModel gpm = new DeclarativeModel(pm);
		ModelTrieMediator modTrieMediator = new ModelTrieMediator();
		modTrieMediator.setVerbose(true);
		
		try {
			modTrieMediator.addFile("logs//WIRES_log.xes");
			modTrieMediator.addFile("C://Users//wvm405//phd//research//data//logs//real//BPI2013//BPI_Challenge_2013_incidents_lifecycletransition_in_conceptname.xes");

		} catch (LabelTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] args = {"0.2", "0.1"};
		modTrieMediator.addMiner(MinerLabel.MINERful, args);
		modTrieMediator.run();
		
		for (Trie t : modTrieMediator.getTries()) {
			//ModelTrie modTrie = (ModelTrie) t;
			//modTrie.draw();
			Metric precision = new Precision();
			precision.compute(t);
			Metric genE = new GeneralizationEventBased();
			genE.compute(t);
			Metric genS = new GeneralizationStateBased();
			genS.compute(t);
			
			System.out.println(precision.getResult() + ", " + genE.getResult() + ", " + genS.getResult());
		}
	}

	//@Test
	void MINERfulAuto() {
		//ProcessModel pm = MinerManager.runMINERful("C:\\Users\\wvm405\\phd\\research\\data\\logs\\synthetic\\BPAI\\log1.xes", 1.0, 1.0, false);
		//GenericProcessModel gpm = new DeclarativeModel(pm);
		ModelTrieMediator modTrieMediator = new ModelTrieMediator();
		modTrieMediator.setVerbose(true);
		
		try {
			//modTrieMediator.addFile("logs//WIRES_log.xes");
			//modTrieMediator.addFile("C://Users//wvm405//phd//research//data//logs//real//BPI2013//BPI_Challenge_2013_incidents_lifecycletransition_in_conceptname.xes");
			//modTrieMediator.addFile("C://Users//wvm405//phd//research//data//logs//real//Activities of daily living of several individuals//data//dailyactivities_alllogs_lifecycletransition_in_conceptname.xes");
			modTrieMediator.addFile("C://Users//wvm405//phd//research//data//logs//real//Sepsis Cases//Sepsis Cases - Event Log.xes");
		} catch (LabelTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] args = {"0.05", "58"};
		modTrieMediator.addMiner(MinerLabel.MINERfulAuto, args);
		modTrieMediator.run();
		
		for (Trie t : modTrieMediator.getTries()) {
			ModelTrie modTrie = (ModelTrie) t;
			//modTrie.draw();
			Metric precision = new Precision();
			precision.compute(t);
			Metric genE = new GeneralizationEventBased();
			genE.compute(t);
			Metric genS = new GeneralizationStateBased();
			genS.compute(t);
			
			System.out.println(modTrie.getModel().getName());
			System.out.println(precision.getResult() + ", " + genE.getResult() + ", " + genS.getResult());
		}
	}
	
	//@Test
	void InductiveMiner() {
		
		//PrintStream ps = System.out;
		//System.setOut(new PrintStream(new NullOutputStream()));
		
		//Object[] pm = MinerManager.runInductiveMiner("logs\\road.xes");
		//GenericProcessModel gpm = new ImperativeModel(pm);
		ModelTrieMediator modTrieMediator = new ModelTrieMediator();
		modTrieMediator.setVerbose(true);
		
		try {
			//modTrieMediator.addFile("logs//WIRES_log.xes");
			//modTrieMediator.addFile("C://Users//wvm405//phd//research//data//logs//real//BPI2013//BPI_Challenge_2013_incidents_lifecycletransition_in_conceptname.xes");
			modTrieMediator.addFile("C://Users//wvm405//phd//research//data//logs//real//Activities of daily living of several individuals//data//dailyactivities_alllogs_lifecycletransition_in_conceptname.xes");
		} catch (LabelTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		modTrieMediator.addMiner(MinerLabel.InductiveMiner);
		modTrieMediator.run();
		
		for (Trie t : modTrieMediator.getTries()) {
			//ModelTrie modTrie = (ModelTrie) t;
			//modTrie.draw();
			Metric precision = new Precision();
			precision.compute(t);
			Metric genE = new GeneralizationEventBased();
			genE.compute(t);
			Metric genS = new GeneralizationStateBased();
			genS.compute(t);
			
			System.out.println(precision.getResult() + ", " + genE.getResult() + ", " + genS.getResult());
		}	
	}
	
	//@Test
	void crossValidation() {
		
		//Object[] pnObj = {petrinetM2, initMarkingM2};
		//ImperativeModel pnModel = new ImperativeModel(pnObj);
		//ModelTrie mTrie = new ModelTrie(pnModel);
		ModelTrieMediator mTrieMediator = new ModelTrieMediator();
		mTrieMediator.setVerbose(true);
		mTrieMediator.setCrossValidation(CrossValidationType.KFold, 3);
		try {
			mTrieMediator.addFile("C://Users//wvm405//phd//research//data//logs//real//Sepsis Cases//Sepsis Cases - Event Log.xes");
		} catch (LabelTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mTrieMediator.addMiner(MinerLabel.InductiveMiner);
		mTrieMediator.run();
		
		for (Trie t : mTrieMediator.getTries()) {
			//ModelTrie modTrie = (ModelTrie) t;
			//modTrie.draw();
			System.out.println(mTrieMediator.getTrieAttributes(t).getFile().getName());
			Metric fitness = new Fitness();
			fitness.compute(t);
			Metric precision = new Precision();
			precision.compute(t);
			Metric genE = new GeneralizationEventBased();
			genE.compute(t);
			Metric genS = new GeneralizationStateBased();
			genS.compute(t);
			
			System.out.println(fitness.getResult() + ", " + precision.getResult() + ", " + genE.getResult() + ", " + genS.getResult());
		}		
	}
	
	//@Test
	void files() {
		
		for (int i=1; i<=10; i++) {
			XLog log = null;
			try {
				log = XESTools.loadXES("C:\\Users\\wvm405\\phd\\research\\code\\qmpm\\logs\\WIRES_log-training-" + i + ".xes");
			} catch (FileLoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("size of training log " + i + ": " + log.size());
		}
	}
 
	//@Test
	void iter() {
		System.out.println("Iter");
		List<Integer> list = new ArrayList<Integer>();
		ListIterator<Integer> iter = list.listIterator();

		for (Integer i : Arrays.asList(5,6,7)) {
			iter.add(i);
		}
		
		while(iter.hasPrevious()) {
			Integer n = iter.previous();
			System.out.println(n);
			if (n>0) {
				iter.add(n-1);
			}
		}
	}
	
	@Test
	public void xLogFileClone() {
		
		System.out.println("xLogFileClone");
		
		XFactory xFactory = new XFactoryBufferedImpl();		
		XLog log1 = xFactory.createLog();

		XEvent e1 = xFactory.createEvent();
		XEvent e2 = xFactory.createEvent();
		XEvent e3 = xFactory.createEvent();
		
		XTrace t1 = xFactory.createTrace();
		XTrace t2 = xFactory.createTrace();
		XTrace t3 = xFactory.createTrace();

		t1.add(e1);
		t1.add(e2);
		t1.add(e3);
		
		t2.add(e3);
		t2.add(e2);
		t2.add(e1);
		
		t3.add(e3);
		t3.add(e3);
		t3.add(e3);
		
		log1.add(t1);
		log1.add(t2);
		
		XLog log2 = (XLog) log1.clone();
		
		log1.remove(1);
		log1.add(t3);
		
		System.out.println("log1.size(): " + log1.size());
		System.out.println("log2.size(): " + log2.size());
		
		for (int i = 0; i < log1.size(); i++) {
			System.out.println("trace " + i);
			for (int j = 0; j < log1.get(i).size(); j++) {
				System.out.println("event " + j);
				
				System.out.println(log1.get(i).get(j).equals(log2.get(i).get(j)));
				//assertEquals(log1.get(i).get(j), log2.get(i).get(j));
			}
		}
		System.out.println("done");
	}
	
}
