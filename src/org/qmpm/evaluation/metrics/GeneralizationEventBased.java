package org.qmpm.evaluation.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.processmining.GenericProcessModel.ModelState;
import org.qmpm.evaluation.trie.ModelNode;
import org.qmpm.evaluation.trie.ModelTrie;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.exceptions.NodeTypeException;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.trie.Trie;
import org.qmpm.logtrie.trie.Trie.Node;

public class GeneralizationEventBased extends Metric {

	// private String modelName = "";

	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException, NodeTypeException {

		if (!(t instanceof ModelTrie)) {
			return Outcome.ERROR;
		} else {

			// modelName = ((ModelTrie) t).getModel().getName();

			Map<ModelState, Integer> simSizeMap = new HashMap<>();
			Set<ElementLabel> diff;
			Set<ModelNode> sim;
			int simSize = 0;
			double generalization = 0;

			Set<Node> prefixNodeSet = t.getNodeSet(false);
			// prefixNodeSet.remove(t.getRoot());

			int progress = 0;
			int total = prefixNodeSet.size();

			for (Node n : prefixNodeSet) {

				if (!(n instanceof ModelNode)) {
					throw new NodeTypeException(
							"Can only calculate model-log metrics on tries consisting of ModelNodes. Encountered node of type: "
									+ n.getClass().getSimpleName());
				}

				simSize = 0;
				if (!n.getIsRoot()) {
					ModelNode p = (ModelNode) n.getParent();
					ModelState state = ((ModelTrie) t).getModel().getState(p.getStateAbbrev());

					if (simSizeMap.containsKey(state)) {
						simSize = simSizeMap.get(state);
					} else {
						sim = state.getNodes();
						for (ModelNode visitingNode : sim) {
							simSize += visitingNode.getVisits();
						}
						simSizeMap.put(state, simSize);
					}

					diff = state.getOutgoingActs();
					generalization += this.pnew(diff.size(), simSize) * n.getVisits();

					// Update progress of associated Metric object, check for timeout or error
					this.updateProgress((double) ++progress / total);
					if (this.getOutcome() != Outcome.CONTINUE) {
						return this.getOutcome();
					}
				}
			}

			int numOfEvents = t.getTotalVisits(false);

			this.finished();

			this.value = 1 - 1.0 / numOfEvents * generalization;

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
		return EvaluationMetricLabel.GeneralizationEventBased;
	}

	@Override
	public String parametersAsString() {
		return "";
	}

}
