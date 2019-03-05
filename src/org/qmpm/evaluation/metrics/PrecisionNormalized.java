package org.qmpm.evaluation.metrics;

import java.util.List;

import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.processmining.RunFlowerMiner;
import org.qmpm.evaluation.trie.ModelTrie;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.trie.Trie;

public class PrecisionNormalized extends Metric {

	String modelName = "";

	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException {

		if (!(t instanceof ModelTrie)) {
			return Outcome.ERROR;
		} else {

			this.modelName = ((ModelTrie) t).getModel().getName();

			// GenericProcessModel origModel = ((ModelTrie) t).getModel();

			double P = 0.0;
			double P_upper = 1.0;
			double P_lower = 0.0;

			Precision prec = new Precision();
			prec.compute(t);

			Trie assocTrie = t.getAssociatedTrie() == null ? t : t.getAssociatedTrie();
			RunFlowerMiner fm = new RunFlowerMiner();
			fm.compute(assocTrie);

			ModelTrie flowerTrie = new ModelTrie(fm.getModel());

			for (List<? extends Object> seq : t.rebuildSequences(false)) {
				flowerTrie.insert(seq, false);
			}

			Precision precFlower = new Precision();
			precFlower.compute(flowerTrie);

			if (prec.getOutcome() == Outcome.SUCCESS) {
				P = prec.getValue();
			} else {
				return prec.getOutcome();
			}

			if (precFlower.getOutcome() == Outcome.SUCCESS) {
				P_lower = precFlower.getValue();
			} else {
				return precFlower.getOutcome();
			}

			this.finished();

			this.value = (P - P_lower) / (P_upper - P_lower);
			return Outcome.SUCCESS;
		}
	}

	@Override
	public void processArgs(String[] args) {
		// Takes no arguments
	}

	@Override
	public MetricLabel getLabel() {
		return EvaluationMetricLabel.PrecisionNormalized;
	}

	@Override
	public String parametersAsString() {
		return "";
	}
}