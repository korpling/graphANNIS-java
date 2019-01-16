package org.corpus_tools.graphannis.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Node {
	private final String name;
	private final String type;
	
	private final Map<QName, String> attributes;
	
	public Node(String name) {
		this(name, "node");
	}
	
	public Node(String name, String type) {
		this.name = name;
		this.type = type;
		this.attributes = new LinkedHashMap<>();
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public Map<QName, String> getAttributes() {
		return attributes;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Node) {
			Node other = (Node) obj;
			return this.name.equals(other.name);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
}
