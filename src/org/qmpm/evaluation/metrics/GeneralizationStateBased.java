package org.qmpm.evaluation.metrics;

import java.util.HashSet;
import java.util.Set;

import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.processmining.GenericProcessModel.ModelState;
import org.qmpm.evaluation.trie.ModelNode;
import org.qmpm.evaluation.trie.ModelTrie;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.trie.Trie;
import org.qmpm.logtrie.trie.Trie.Node;

public class GeneralizationStateBased extends Metric {

	// private String modelName = "";

	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException {

		if (!(t instanceof ModelTrie)) {
			return Outcome.ERROR;
		} else {

			// modelName = ((ModelTrie) t).getModel().getName();

			Set<ElementLabel> diff = new HashSet<>();
			Set<Node> sim = new HashSet<>();
			;
			double generalization = 0;
			Set<ModelState> stateSet = ((ModelTrie) t).getModel().getStates();

			int progress = 0;
			int total = stateSet.size();

			for (ModelState state : stateSet) {

				Set<ModelNode> visitingEvents = state.getNodes();

				for (ModelNode parent : visitingEvents) {
					sim.addAll(parent.getChildren().values());
					diff.addAll(parent.getChildren().keySet());
				}

				generalization += this.pnew(diff.size(), sim.size());

				// Update progress of associated Metric object, check for timeout or error
				this.updateProgress((double) ++progress / total);
				if (this.getOutcome() != Outcome.CONTINUE) {
					return this.getOutcome();
				}
			}

			this.finished();

			this.value = 1 - 1.0 / stateSet.size() * generalization;

			return Outcome.SUCCESS;
		}
	}

	private double pnew(final int w, final int n) {
		if (n >= w + 2) {
			return (double) (w * (w + 1)) / (n * (n - 1));
		} else {
			return 1.0;
		}
	}

	@Override
	public void processArgs(String[] args) {
		// Takes no arguments
	}

	@Override
	public MetricLabel getLabel() {
		return EvaluationMetricLabel.GeneralizationStateBased;
	}

	@Override
	public String parametersAsString() {
		return "";
	}

}
