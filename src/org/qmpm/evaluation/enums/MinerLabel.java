package org.qmpm.evaluation.enums;

import org.qmpm.evaluation.processmining.RunDeclareMiner;
import org.qmpm.evaluation.processmining.RunFlowerMiner;
import org.qmpm.evaluation.processmining.RunInductiveMiner;
import org.qmpm.evaluation.processmining.RunMINERful;
import org.qmpm.evaluation.processmining.RunMINERfulAuto;
import org.qmpm.evaluation.processmining.RunPetrinetTest;
import org.qmpm.logtrie.core.Framework;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.metrics.Metric;

public enum MinerLabel implements MetricLabel {
	DeclareMiner("Declare Miner"),
	InductiveMiner("Inductive Miner"),
	FlowerMiner("Flower Miner"),
	MINERful("MINERful"),
	MINERfulAuto("MINERful Auto-Config"),
	PetrinetTest("Petrinet test");
	
	private String desc;
	
	MinerLabel(String shortDesc) {
		desc = shortDesc;
	}
	
	public Metric delegate(String args[]) {
		
		switch(this) {
		case DeclareMiner:
			RunDeclareMiner dm = new RunDeclareMiner();
			dm.setArgs(args);
			dm.processArgs(args);
			return dm;
		case InductiveMiner: 		
			RunInductiveMiner im = new RunInductiveMiner();
			im.setArgs(args);
			im.processArgs(args);
			return im;
		case FlowerMiner:
			RunFlowerMiner fm = new RunFlowerMiner();
			fm.setArgs(args);
			fm.processArgs(args);
			return fm;
		case MINERful: 		
			RunMINERful mFul = new RunMINERful();
			mFul.setArgs(args);
			mFul.processArgs(args);
			return mFul;
		case MINERfulAuto:
			RunMINERfulAuto mFulAuto = new RunMINERfulAuto();
			mFulAuto.setArgs(args);
			mFulAuto.processArgs(args);
			return mFulAuto;
		case PetrinetTest:
			RunPetrinetTest pnTest = new RunPetrinetTest();
			pnTest.setArgs(args);
			pnTest.processArgs(args);
			return pnTest;		
		default: 	
			System.err.println("Unable to delegate MinerLabel to RunMiner object");
			return null;
		}
	}

	@Override
	public String shortDescription() {
		return desc;
	}
	
	@Override
	public String labelType() {
		return this.getClass().getSimpleName();
	}
	
}
