package org.corpus_tools.graphannis.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Edge {
	private Node source;
	private Node target;
	
	private final int sourceID;
	private final int targetID;
	
	private final Graph graph;
	
	private final Component component;
	
	private final Map<QName, String> labels;

	public Edge(int sourceID, int targetID, Component component, Map<QName, String> labels, Graph graph) {
		
		Preconditions.checkNotNull(graph);
		
		this.sourceID = sourceID;
		this.targetID = targetID;
		this.component = component;
		this.labels = labels == null ? new LinkedHashMap<>() : labels;
		this.graph = graph;
	}
	
	public Node getSource() {
		if(source == null) {
			source = graph.getNodeForID(sourceID);
		}
		return source;
	}
	
	public int getSourceID() {
		return sourceID;
	}
	
	public Node getTarget() {
		if(target == null) {
			target = graph.getNodeForID(targetID);
		}
		return target;
	}
	
	public int getTargetID() {
		return targetID;
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
			return Objects.equal(this.sourceID, other.sourceID) && Objects.equal(this.targetID, other.targetID)
					&& Objects.equal(this.component, other.component);
		} else {
			return false;
		}
	}
}
