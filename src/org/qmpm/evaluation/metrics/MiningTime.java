package org.qmpm.evaluation.metrics;

import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.processmining.GenericProcessModel;
import org.qmpm.evaluation.trie.ModelTrie;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.tools.MathTools;
import org.qmpm.logtrie.trie.Trie;

public class MiningTime extends Metric {
	
	String modelName = "";

	public MiningTime() {
		super();
		setSigDigits(3);
	}
	
	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException {
		
		GenericProcessModel pm = ((ModelTrie) t).getModel();
		modelName = ((ModelTrie) t).getModel().getName();
		
		updateProgress(1.0);
		if (getOutcome() != Outcome.CONTINUE) {
			return getOutcome();
		}
		
		finished();
		
		value = (double) pm.getBuildTime() / 1000000;
		
 		return Outcome.SUCCESS;
	}

	@Override
	public void processArgs(String[] args) {
		// Takes no arguments
	}

	@Override
	public MetricLabel getLabel() {
		return EvaluationMetricLabel.MiningTime_ms;
	}

	@Override
	public String parametersAsString() {
		return "";
	}

}
