package org.qmpm.evaluation.enums;

import org.qmpm.evaluation.metrics.CollectionSize;
import org.qmpm.evaluation.metrics.Fitness;
import org.qmpm.evaluation.metrics.GeneralizationEventBased;
import org.qmpm.evaluation.metrics.GeneralizationStateBased;
import org.qmpm.evaluation.metrics.MiningTime;
import org.qmpm.evaluation.metrics.ModelSize;
import org.qmpm.evaluation.metrics.Precision;
import org.qmpm.evaluation.metrics.PrecisionNormalized;
import org.qmpm.evaluation.metrics.UniqueTraces;
import org.qmpm.logtrie.enums.MetricLabel;
import org.qmpm.logtrie.metrics.Metric;

public enum EvaluationMetricLabel implements MetricLabel {
	Fitness("Fitness"), Precision("Precision"), PrecisionNormalized("Precision Normalized"),
	GeneralizationStateBased("Generalization (state-based)"), GeneralizationEventBased("Generalization (event-based)"),
	ModelSize("Model Size (constraints/edges)"), MiningTime_ms("Mining Time (milliseconds)"), CollectionSize("Size"),
	UniqueTraces("Unique Traces");

	private String desc;

	EvaluationMetricLabel(String shortDesc) {
		this.desc = shortDesc;
	}

	@Override
	public Metric delegate(String args[]) {
		switch (this) {
		case Fitness:
			return new Fitness();
		case Precision:
			return new Precision();
		case PrecisionNormalized:
			return new PrecisionNormalized();
		case GeneralizationStateBased:
			return new GeneralizationStateBased();
		case GeneralizationEventBased:
			return new GeneralizationEventBased();
		case ModelSize:
			return new ModelSize();
		case MiningTime_ms:
			return new MiningTime();
		case CollectionSize:
			return new CollectionSize();
		case UniqueTraces:
			return new UniqueTraces();
		default:
			return null;
		}
	}

	@Override
	public String shortDescription() {
		return this.desc;
	}

	@Override
	public String labelType() {
		return this.getClass().getSimpleName();
	}
}
