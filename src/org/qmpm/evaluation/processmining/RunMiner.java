package org.qmpm.evaluation.processmining;

import org.deckfour.xes.model.XLog;
import org.qmpm.logtrie.enums.Outcome;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.metrics.Metric;
import org.qmpm.logtrie.trie.Trie;

public abstract class RunMiner extends Metric {
	
	protected String path;
	protected XLog xLog = null;

	protected GenericProcessModel model = null;
	
	protected abstract long getRunTime();
	
	@Override
	public Outcome doComputation(Trie t) throws LabelTypeException, FileLoadException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPath(String p) {
		path = p;
	}

	public void setLog(XLog l) {
		this.xLog = l;
	}
	
	public GenericProcessModel getModel() {
		
		if (model != null)	model.setBuildTime(getRunTime());
		return model;
	}
}