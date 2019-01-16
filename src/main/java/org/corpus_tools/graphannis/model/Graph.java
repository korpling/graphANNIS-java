package org.corpus_tools.graphannis.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.corpus_tools.graphannis.capi.AnnisEdge;
import org.corpus_tools.graphannis.capi.CAPI;
import org.corpus_tools.graphannis.capi.CAPI.AnnisAnnotation;
import org.corpus_tools.graphannis.capi.CAPI.AnnisComponentConst;
import org.corpus_tools.graphannis.capi.CAPI.AnnisIterPtr_AnnisNodeID;
import org.corpus_tools.graphannis.capi.CAPI.AnnisVec_AnnisComponent;
import org.corpus_tools.graphannis.capi.CAPI.AnnisVec_AnnisEdge;
import org.corpus_tools.graphannis.capi.CharPointer;
import org.corpus_tools.graphannis.capi.NodeID;
import org.corpus_tools.graphannis.capi.NodeIDByRef;

import com.google.common.base.Preconditions;
import com.sun.jna.NativeLong;

public class Graph {
	
	public static QName NODE_NAME = new QName("annis", "node_name");
	public static QName NODE_TYPE = new QName("annis", "node_type");
	public static QName TOK = new QName("annis", "tok");
	
	
	private final CAPI.AnnisGraph graph;
	
	public Graph(CAPI.AnnisGraph g) {
		Preconditions.checkNotNull(g);
		this.graph = g;
	}
	
	public Iterator<Node> getNodesByType(String type) {
		return new NodeIterator(CAPI.annis_graph_nodes_by_type(this.graph, type));
	}
	
	public List<Edge> getOutgoingEdges(Node node) {
		List<Edge> result = new ArrayList<Edge>();
		
		AnnisVec_AnnisComponent components = CAPI.annis_graph_all_components(graph);
		for (int i = 0; i < CAPI.annis_vec_component_size(components).intValue(); i++) {
            CAPI.AnnisComponentConst cOrig = CAPI.annis_vec_component_get(components, new NativeLong(i));
            Component c = mapComponent(cOrig);

            AnnisVec_AnnisEdge outEdges = CAPI.annis_graph_outgoing_edges(graph, new NodeID(node.getId()),
                    cOrig);
            for (int edgeIdx = 0; edgeIdx < CAPI.annis_vec_edge_size(outEdges).intValue(); edgeIdx++) {
                AnnisEdge edge = CAPI.annis_vec_edge_get(outEdges, new NativeLong(edgeIdx));
                // add edge
                Node source = getNodeForID(edge.source.intValue());
                Node target = getNodeForID(edge.target.intValue());
                Map<QName, String> labels = getEdgeLabels(source, target, cOrig);
                result.add(new Edge(source, target, c, labels));
            }
        }
		
		return result;
	}
	
	public List<Edge> getOutgoingEdges(Node node, int componentType) {
		List<Edge> result = new ArrayList<Edge>();
		
		AnnisVec_AnnisComponent components = CAPI.annis_graph_all_components_by_type(graph, componentType);
		for (int i = 0; i < CAPI.annis_vec_component_size(components).intValue(); i++) {
            CAPI.AnnisComponentConst cOrig = CAPI.annis_vec_component_get(components, new NativeLong(i));
            Component c = mapComponent(cOrig);

            AnnisVec_AnnisEdge outEdges = CAPI.annis_graph_outgoing_edges(graph, new NodeID(node.getId()),
                    cOrig);
            for (int edgeIdx = 0; edgeIdx < CAPI.annis_vec_edge_size(outEdges).intValue(); edgeIdx++) {
                AnnisEdge edge = CAPI.annis_vec_edge_get(outEdges, new NativeLong(edgeIdx));
                // add edge
                Node source = getNodeForID(edge.source.intValue());
                Node target = getNodeForID(edge.target.intValue());
                Map<QName, String> labels = getEdgeLabels(source, target, cOrig);
                result.add(new Edge(source, target, c, labels));
            }
        }
		
		return result;
	}
	
	private static Component mapComponent(AnnisComponentConst cOrig) {
		Component c = new Component();
		int ctype = CAPI.annis_component_type(cOrig);
		c.setType(ComponentType.fromInt(ctype));

		CharPointer cname = CAPI.annis_component_name(cOrig);
		c.setName(cname == null ? "" : cname.toString());

		CharPointer clayer = CAPI.annis_component_layer(cOrig);
		c.setLayer(clayer == null ? "" : clayer.toString());
		
		return c;
	}
	
	private Node getNodeForID(int id) {
		Map<QName, String> labels = getNodeLabels(graph, id);
		
		String name = labels.remove(NODE_NAME);
		if(name == null) {
			return null;
		}
		String type = labels.remove(NODE_TYPE);
		Node n;
		if(type == null) {
			n = new Node(id, name, labels);
		} else {
			n = new Node(id, name, type, labels);
		}
		
		return n;
	}
	
	private static Map<QName, String> getNodeLabels(CAPI.AnnisGraph g, int nID) {
        Map<QName, String> labels = new LinkedHashMap<>();
        CAPI.AnnisVec_AnnisAnnotation annos = CAPI.annis_graph_annotations_for_node(g, new NodeID(nID));
        for (long i = 0; i < CAPI.annis_vec_annotation_size(annos).longValue(); i++) {
            AnnisAnnotation a = CAPI.annis_vec_annotation_get(annos, new NativeLong(i));

            String ns = CAPI.annis_annotation_ns(a).toString();
            String name = CAPI.annis_annotation_name(a).toString();
            String value = CAPI.annis_annotation_val(a).toString();

            if (name != null && value != null) {
                if (ns == null) {
                    labels.put(new QName("", name), value);
                } else {
                    labels.put(new QName(ns, name), value);
                }
            }
        }
        annos.dispose();

        return labels;
    }
	
	private Map<QName, String> getEdgeLabels(Node source, Node target, AnnisComponentConst component) {
		Map<QName, String> labels = new LinkedHashMap<>();
        AnnisEdge.ByValue copyEdge = new AnnisEdge.ByValue();
        copyEdge.source = new NodeID(source.getId());
        copyEdge.target = new NodeID(target.getId());
        CAPI.AnnisVec_AnnisAnnotation annos = CAPI.annis_graph_annotations_for_edge(graph, copyEdge, component);
        for (long i = 0; i < CAPI.annis_vec_annotation_size(annos).longValue(); i++) {
            AnnisAnnotation a = CAPI.annis_vec_annotation_get(annos, new NativeLong(i));

            String ns = CAPI.annis_annotation_ns(a).toString();
            String name = CAPI.annis_annotation_name(a).toString();
            String value = CAPI.annis_annotation_val(a).toString();

            if (name != null && value != null) {
                if (ns == null) {
                    labels.put(new QName("", name), value);
                } else {
                    labels.put(new QName(ns, name), value);
                }
            }
        }
        annos.dispose();

        return labels;
    }
	
	
	private class NodeIterator implements Iterator<Node> {
		
		private final AnnisIterPtr_AnnisNodeID delegate;
		
		private  Optional<NodeIDByRef> nextID = Optional.empty();
		
		public NodeIterator(AnnisIterPtr_AnnisNodeID delegate) {
			this.delegate = delegate;
			getNext();
		}
		
		private void getNext() {
			if(this.delegate != null) {
				this.nextID = Optional.ofNullable(CAPI.annis_iter_nodeid_next(this.delegate));
			}
		}
		
		@Override
		public boolean hasNext() {
			return nextID.isPresent();
		}
		

		@Override
		public Node next() {
			if(this.nextID.isPresent()) {
				Node n = getNodeForID(this.nextID.get().getValue());
				getNext();
				return n;
			} else {
				return null;
			}
		}
		
	}
}
