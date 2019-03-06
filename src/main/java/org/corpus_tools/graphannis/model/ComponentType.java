package org.corpus_tools.graphannis.model;

public enum ComponentType {
	
	
	Coverage(0), Dominance(2), Pointing(3), Ordering(4), LeftToken(5), RightToken(6), PartOf(7);
	

	int rawVal;
	ComponentType(int rawVal) {
		this.rawVal = rawVal;
	}
	
	public static ComponentType fromInt(int ctype) {
		for(ComponentType t : ComponentType.values()) {
			if(t.rawVal == ctype) {
				return t;
			}
		}
		throw new IllegalArgumentException("Invalid component type " + ctype);
	}
	
	public int toInt() {
		return this.rawVal;
	}
}