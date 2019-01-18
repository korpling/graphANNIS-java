package org.corpus_tools.graphannis.model;

import java.util.Collections;
import java.util.Map;

public class Node {
	private final int id;
	private final String name;
	private final String type;
	
	private final Map<QName, String> labels;
	
	public Node(int id, String name, Map<QName, String> labels) {
		this(id, name, "node", labels);
	}
	
	
	public Node(int id, String name, String type, Map<QName, String> labels) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.labels = labels;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public Map<QName, String> getLabels() {
		return Collections.unmodifiableMap(labels);
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
