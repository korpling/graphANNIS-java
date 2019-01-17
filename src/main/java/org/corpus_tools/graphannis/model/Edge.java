package org.corpus_tools.graphannis.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Edge {
	private Node source;
	private Node target;
	
	private final int source_ref;
	private final int target_ref;
	
	private final Graph graph;
	
	private final Component component;
	
	private final Map<QName, String> labels;

	public Edge(int source_ref, int target_ref, Component component, Map<QName, String> labels, Graph graph) {
		
		Preconditions.checkNotNull(graph);
		
		this.source_ref = source_ref;
		this.target_ref = target_ref;
		this.component = component;
		this.labels = labels == null ? new LinkedHashMap<>() : labels;
		this.graph = graph;
	}
	
	public Node getSource() {
		if(source == null) {
			source = graph.getNodeForID(source_ref);
		}
		return source;
	}
	
	public Node getTarget() {
		if(target == null) {
			target = graph.getNodeForID(target_ref);
		}
		return target;
	}
	
	public Map<QName, String> getLabels() {
		return Collections.unmodifiableMap(this.labels);
	}
	
	public Component getComponent() {
		return component;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		
		if(obj instanceof Edge) {
			Edge other = (Edge) obj;
			return Objects.equal(this.source_ref, other.source_ref) && Objects.equal(this.target_ref, other.target_ref)
					&& Objects.equal(this.component, other.component);
		} else {
			return false;
		}
	}
}
