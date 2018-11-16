package org.qmpm.evaluation.processmining;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;
import org.qmpm.logtrie.trie.Trie;

public class RunInductiveMiner extends RunMiner {

	//private String path;
	private long runTime = -1;	
	
	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException, FileLoadException {
		
		XLog imLog;
		
		if (xLog != null) {
			imLog = xLog;
		} else {
			try {
				imLog = XESTools.loadXES(path, true);
			} catch (FileLoadException e) {
				e.printStackTrace();
				throw e;
			}
		}
		
		long startMining = System.nanoTime();
		
		Object[] imResult = IMPetriNet.minePetriNet(imLog, new MiningParametersIM(), new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		});
		
		runTime = System.nanoTime() - startMining;

		model = new ImperativeModel(imResult);
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
		return MinerLabel.InductiveMiner;
	}

	@Override
	public String parametersAsString() {
		return "";
	}
	
	public long getRunTime() {
		return runTime;
	}
	
}