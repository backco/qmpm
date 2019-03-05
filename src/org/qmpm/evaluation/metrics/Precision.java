package org.qmpm.evaluation.metrics;

import java.util.Set;

import org.qmpm.evaluation.enums.EvaluationMetricLabel;
import org.qmpm.evaluation.trie.ModelNode;
import org.qmpm.evaluation.trie.ModelTrie;
import org.qmpm.logtrie.elementlabel.ElementLabel;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.trie.Trie;
import org.qmpm.logtrie.trie.Trie.Node;

public class Precision extends Metric {

	String modelName = "";

	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException {

		if (!(t instanceof ModelTrie)) {
			return Outcome.ERROR;
		} else {

			this.modelName = ((ModelTrie) t).getModel().getName();

			// System.err.println("Precision.doComputation()");

			double precision = 0;
			Set<ElementLabel> en_L;
			// Set<String> en_M;
			int en_M_Size;
			Set<Node> eventsAsNodes = t.getNodeSet(false);
			// eventsAsNodes.remove(t.getRoot());

			int progress = 0;
			int total = eventsAsNodes.size();

			for (Node n : eventsAsNodes) {
				ModelNode m = (ModelNode) n;

				if (!m.getIsRoot()) {

					ModelNode parent = (ModelNode) m.getParent();

					en_M_Size = parent.getNumValidMoves();
					en_L = parent.getChildEdgeLabels();

					int count = m.getVisits();

					precision += (double) en_L.size() / en_M_Size * count;
				}

				// Update progress of associated Metric object, check for timeout or error
				this.updateProgress((double) ++progress / total);
				if (this.getOutcome() != Outcome.CONTINUE) {
					return this.getOutcome();
				}
			}

			int numOfEvents = t.getTotalVisits(false);

			this.finished();

			this.value = 1.0 / numOfEvents * precision;
			return Outcome.SUCCESS;
		}
	}

	@Override
	public void processArgs(String[] args) {
		// Takes no arguments
	}

	@Override
	public MetricLabel getLabel() {
		return EvaluationMetricLabel.Precision;
	}

	@Override
	public String parametersAsString() {
		return "";
	}

}
