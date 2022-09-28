package org.corpus_tools.graphannis.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.corpus_tools.graphannis.errors.GraphANNISException;

public class Edge {
	private Node source;
	private Node target;
	
	private final int sourceID;
	private final int targetID;
	
	private final Graph graph;
	
	private final Component component;
	
	private final Map<QName, String> labels;

	public Edge(int sourceID, int targetID, Component component, Map<QName, String> labels, Graph graph) {
		
		if(graph == null) {
			throw new NullPointerException();
		}
		
		this.sourceID = sourceID;
		this.targetID = targetID;
		this.component = component;
		this.labels = labels == null ? new LinkedHashMap<>() : labels;
		this.graph = graph;
	}
	
    public Node getSource() throws GraphANNISException {
		if(source == null) {
			source = graph.getNodeForID(sourceID);
		}
		return source;
	}
	
	public int getSourceID() {
		return sourceID;
	}
	
    public Node getTarget() throws GraphANNISException {
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((component == null) ? 0 : component.hashCode());
		result = prime * result + sourceID;
		result = prime * result + targetID;
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
		Edge other = (Edge) obj;
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component))
			return false;
		if (sourceID != other.sourceID)
			return false;
		if (targetID != other.targetID)
			return false;
		return true;
	}
	
	
}
