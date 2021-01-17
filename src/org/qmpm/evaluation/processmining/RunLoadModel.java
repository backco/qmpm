package org.qmpm.evaluation.processmining;

import java.io.File;

import org.qmpm.evaluation.enums.MinerLabel;
import org.qmpm.io.FileParser;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.trie.Trie;

import minerful.concept.ProcessModel;

public class RunLoadModel extends RunMiner {

	// private String path;
	private long runTime = -1;
	private String path;

	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException {

		long startMining = System.nanoTime();

		try {

			if (path.toLowerCase().endsWith("pnml")) {

				Object[] pn = (Object[]) FileParser.getModelPNML(new File(path));

				model = new ImperativeModel(pn);

			} else if (path.toLowerCase().endsWith("decl")) {

				ProcessModel dec = FileParser.getModelDECL(new File(path));

				model = new DeclarativeModel(dec);

			} else {

				throw new IllegalArgumentException();
			}

		} catch (Exception e) {
			
			throw new IllegalArgumentException("Could not load model file: " + path);
		}

		model.setName(getLabel().toString());

		runTime = System.nanoTime() - startMining;

		finished();
		return Outcome.SUCCESS;
	}

	@Override
	public void processArgs(String[] args) {

		System.out.println("processArgs: " + args[0]);
		
		if (args != null && args.length > 1) {

			path = args[1];

		} else {

			throw new IllegalArgumentException();
		}
	}

	@Override
	public MetricLabel getLabel() {
		return MinerLabel.LoadModel;
	}

	@Override
	public String parametersAsString() {
		return "";
	}

	public long getRunTime() {
		return runTime;
	}
}