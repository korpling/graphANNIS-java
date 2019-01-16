package org.corpus_tools.graphannis.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.corpus_tools.graphannis.capi.CAPI;
import org.corpus_tools.graphannis.capi.CAPI.AnnisAnnotation;
import org.corpus_tools.graphannis.capi.CAPI.AnnisIterPtr_AnnisNodeID;
import org.corpus_tools.graphannis.capi.NodeID;
import org.corpus_tools.graphannis.capi.NodeIDByRef;

import com.google.common.base.Preconditions;
import com.sun.jna.NativeLong;

public class Graph {
	
	public static QName NAME_ATT = new QName("annis", "node_name");
	public static QName TYPE_ATT = new QName("annis", "node_type");
	
	
	private final CAPI.AnnisGraph graph;
	
	public Graph(CAPI.AnnisGraph g) {
		Preconditions.checkNotNull(g);
		this.graph = g;
	}
	
	public Iterator<Node> getNodesByType(String type) {
		return new NodeIterator(CAPI.annis_graph_nodes_by_type(this.graph, type));
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
		
		private Node getNodeForID(NodeIDByRef ref) {
			int id = ref.getValue();
			Map<QName, String> labels = getNodeLabels(graph, id);
			
			String name = labels.remove(NAME_ATT);
			if(name == null) {
				return null;
			}
			String type = labels.remove(TYPE_ATT);
			Node n;
			if(type == null) {
				n = new Node(name);
			} else {
				n = new Node(name, type);
			}
			// add remaining labels
			n.getAttributes().putAll(labels);
			
			return n;
		}

		@Override
		public Node next() {
			if(this.nextID.isPresent()) {
				Node n = getNodeForID(this.nextID.get());
				getNext();
				return n;
			} else {
				return null;
			}
		}
		
	}
}
