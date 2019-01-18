package org.corpus_tools.graphannis.model;

public class Component {

	private ComponentType ctype;
	private String name;
	private String layer;
	
	
	public Component() {
		super();
	}

	public Component(ComponentType ctype, String layer, String name) {
		super();
		this.ctype = ctype;
		this.name = name;
		this.layer = layer;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ctype == null) ? 0 : ctype.hashCode());
		result = prime * result + ((layer == null) ? 0 : layer.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Component other = (Component) obj;
		if (ctype != other.ctype)
			return false;
		if (layer == null) {
			if (other.layer != null)
				return false;
		} else if (!layer.equals(other.layer))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	

}
