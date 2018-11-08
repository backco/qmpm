package org.qmpm.evaluation.metrics;

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

public class PrecisionNormalized extends Metric {
	
	String modelName = "";

	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException {
		
		modelName = ((ModelTrie) t).getModel().getName();
		
		double P = 0.0;
		double P_upper = 1.0;
		double P_lower = 0.0;
		
		Precision prec = new Precision();  
		prec.compute(t);
		
		PrecisionFlower precFlower = new PrecisionFlower();  
		precFlower.compute(t);
		
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
		
		double precision = 0;
		Set<ElementLabel> en_L;
		Set<String> en_M;
		Set<Node> eventsAsNodes = t.getNodeSet(false);
		//eventsAsNodes.remove(t.getRoot());
		
		int progress = 0;
		int total = eventsAsNodes.size();
		
		/*
		for (Node n : eventsAsNodes) {
			ModelNode m = (ModelNode) n;

			if (!m.getIsRoot()) {
				ModelNode parent = (ModelNode) m.getParent();
				
				ModelState parState = ((ModelTrie) t).getModel().getState(parent.getStateAbbrev());
				en_M = parState.getValidMoves();
				en_L = parent.getChildEdgeLabels();
				int count = m.getVisits();
				precision += ((double) en_L.size() / en_M.size()) * count;
			}
			
			// Update progress of associated Metric object, check for timeout or error
			updateProgress((double) ++progress / total);
			if (getOutcome() != Outcome.CONTINUE) {
				return getOutcome();
			}
		}
		*/

		//int numOfEvents = t.getTotalVisits(false);
 		
		finished();
		
		value = ((double) P - P_lower) / (P_upper - P_lower);
 		return Outcome.SUCCESS;
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