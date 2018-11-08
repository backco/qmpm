package org.qmpm.evaluation.enums;

import java.time.Instant;

public enum CrossValidationType {
	None("None"),
	KFold("k-Fold");
	
	private String desc;
	private int k = 1;
	String timeStamp = "";
	
	CrossValidationType(String shortDesc) {
		desc = shortDesc;
	}
	
	public int getK() {
		return k;
	}
	
	public void setK(int k) {
		this.k = k;
	}

	public String shortDescription() {
		return desc;
	}
	
	public String labelType() {
		return this.getClass().getSimpleName();
	}
	
	public String getTimeStamp() {
		
		if (timeStamp.equals("")) timeStamp = Instant.now().toString().replaceAll(":", "-");
		return timeStamp;
	}
}
