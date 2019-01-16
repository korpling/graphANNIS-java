package org.corpus_tools.graphannis.model;

import org.corpus_tools.graphannis.capi.AnnisComponentType;

public class Component {

	public enum Type {
		Coverage, Dominance, Pointing, Ordering, LeftToken, RightToken, PartOfSubcorpus;
		
		public static Type fromInt(int ctype) {
			switch (ctype) {
			case AnnisComponentType.Coverage:
				return Type.Coverage;
			case AnnisComponentType.Dominance:
				return Type.Dominance;
			case AnnisComponentType.Pointing:
				return Type.Pointing;
			case AnnisComponentType.Ordering:
				return Type.Ordering;
			case AnnisComponentType.LeftToken:
				return Type.LeftToken;
			case AnnisComponentType.RightToken:
				return Type.RightToken;
			case AnnisComponentType.PartOfSubcorpus:
				return Type.PartOfSubcorpus;
			}
			throw new IllegalArgumentException("Invalid component type " + ctype);
		}
	}

	private Type ctype;
	private String name;
	private String layer;

	public Type getType() {
		return ctype;
	}

	public void setType(Type ctype) {
		this.ctype = ctype;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLayer() {
		return layer;
	}

	public void setLayer(String layer) {
		this.layer = layer;
	}

}
