package org.corpus_tools.graphannis.model;

import java.util.Collections;
import java.util.Map;

public class Edge {
	private final Node source;
	private final Node target;
	
	private final Component component;
	
	private final Map<QName, String> labels;

	public Edge(Node source, Node target, Component component, Map<QName, String> labels) {
		super();
		this.source = source;
		this.target = target;
		this.component = component;
		this.labels = labels;
	}
	
	public Node getSource() {
		return source;
	}
	
	public Node getTarget() {
		return target;
	}
	
	public Map<QName, String> getLabels() {
		return Collections.unmodifiableMap(this.labels);
	}
	
	public Component getComponent() {
		return component;
	}
}
