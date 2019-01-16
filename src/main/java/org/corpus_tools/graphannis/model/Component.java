package org.corpus_tools.graphannis.model;

public class Component {

	private ComponentType ctype;
	private String name;
	private String layer;

	public ComponentType getType() {
		return ctype;
	}

	public void setType(ComponentType ctype) {
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
