package org.qmpm.evaluation.metrics;

import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.processmining.GenericProcessModel;
import org.qmpm.evaluation.trie.ModelTrie;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.trie.Trie;

public class ModelSize extends Metric {

	String modelName = "";

	public ModelSize() {
		super();
		this.setSigDigits(0);
	}

	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException {

		if (!(t instanceof ModelTrie)) {
			return Outcome.ERROR;
		} else {

			GenericProcessModel pm = ((ModelTrie) t).getModel();
			this.modelName = ((ModelTrie) t).getModel().getName();

			this.updateProgress(1.0);
			if (this.getOutcome() != Outcome.CONTINUE) {
				return this.getOutcome();
			}

			this.finished();

			this.value = (double) pm.getModelSize();

			return Outcome.SUCCESS;
		}
	}

	@Override
	public void processArgs(String[] args) {
		// Takes no arguments
	}

	@Override
	public MetricLabel getLabel() {
		return EvaluationMetricLabel.ModelSize;
	}

	@Override
	public String parametersAsString() {
		return "";
	}

}
