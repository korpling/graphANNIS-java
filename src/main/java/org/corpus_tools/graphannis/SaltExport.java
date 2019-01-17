/*
 * Copyright 2017 Thomas Krause.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.corpus_tools.graphannis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.corpus_tools.graphannis.capi.AnnisEdge;
import org.corpus_tools.graphannis.capi.CAPI;
import org.corpus_tools.graphannis.capi.CAPI.AnnisAnnotation;
import org.corpus_tools.graphannis.capi.CAPI.AnnisComponentConst;
import org.corpus_tools.graphannis.capi.CAPI.AnnisVec_AnnisComponent;
import org.corpus_tools.graphannis.capi.CAPI.AnnisVec_AnnisEdge;
import org.corpus_tools.graphannis.capi.CharPointer;
import org.corpus_tools.graphannis.capi.NodeID;
import org.corpus_tools.graphannis.capi.NodeIDByRef;
import org.corpus_tools.graphannis.model.Component;
import org.corpus_tools.graphannis.model.ComponentType;
import org.corpus_tools.graphannis.model.Edge;
import org.corpus_tools.graphannis.model.Graph;
import org.corpus_tools.graphannis.model.Node;
import org.corpus_tools.graphannis.model.QName;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotationContainer;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SGraph;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.SaltUtil;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.sun.jna.NativeLong;

/**
 * Allows to extract a Salt-Graph from a database subgraph.
 * 
 * @author Thomas Krause <thomaskrause@posteo.de>
 */
public class SaltExport {

	private final Graph orig;
	private final SDocumentGraph docGraph;
	private final BiMap<Integer, SNode> nodesByID;
	private final Map<Integer, Integer> node2timelinePOT;

	protected SaltExport(Graph orig) {
		this.orig = orig;

		this.docGraph = SaltFactory.createSDocumentGraph();
		this.nodesByID = HashBiMap.create();
		this.node2timelinePOT = new HashMap<>();
	}

	private static void mapLabels(SAnnotationContainer n, Map<QName, String> labels, boolean isMeta) {
		for (Map.Entry<QName, String> e : labels.entrySet()) {
			if ("annis".equals(e.getKey().getNs())) {
				n.createFeature(e.getKey().getNs(), e.getKey().getName(), e.getValue());
			} else if (isMeta) {
				n.createMetaAnnotation(e.getKey().getNs(), e.getKey().getName(), e.getValue());
			} else {
				n.createAnnotation(e.getKey().getNs(), e.getKey().getName(), e.getValue());
			}
		}

	}

	private boolean hasDominanceEdge(Node node) {

		List<Edge> outEdges = orig.getOutgoingEdges(node, ComponentType.Dominance);
		return !outEdges.isEmpty();
	}

	private boolean hasCoverageEdge(Node node) {
		List<Edge> outEdges = orig.getOutgoingEdges(node, ComponentType.Coverage);
		return !outEdges.isEmpty();
	}

	private static Map<Pair<String, String>, String> getNodeLabels(CAPI.AnnisGraph g, int nID) {
		Map<Pair<String, String>, String> labels = new LinkedHashMap<>();
		CAPI.AnnisVec_AnnisAnnotation annos = CAPI.annis_graph_annotations_for_node(g, new NodeID(nID));
		for (long i = 0; i < CAPI.annis_vec_annotation_size(annos).longValue(); i++) {
			AnnisAnnotation a = CAPI.annis_vec_annotation_get(annos, new NativeLong(i));

			String ns = CAPI.annis_annotation_ns(a).toString();
			String name = CAPI.annis_annotation_name(a).toString();
			String value = CAPI.annis_annotation_val(a).toString();

			if (name != null && value != null) {
				if (ns == null) {
					labels.put(new ImmutablePair<>("", name), value);
				} else {
					labels.put(new ImmutablePair<>(ns, name), value);
				}
			}
		}
		annos.dispose();

		return labels;
	}

	private SNode mapNode(Node node) {
		SNode newNode;

		// get all annotations for the node into a map, also create the node itself
		Map<QName, String> labels = node.getLabels();

		if (labels.containsKey(Graph.TOK) && !hasCoverageEdge(node)) {
			newNode = SaltFactory.createSToken();
		} else if (hasDominanceEdge(node)) {
			newNode = SaltFactory.createSStructure();
		} else {
			newNode = SaltFactory.createSSpan();
		}

		String nodeName = node.getName();
		if (!nodeName.startsWith("salt:/")) {
			nodeName = "salt:/" + nodeName;
		}
		newNode.setId(nodeName);
		// get the name from the ID
		newNode.setName(newNode.getPath().fragment());

		mapLabels(newNode, labels, false);

		return newNode;
	}

	private void mapAndAddEdge(Edge origEdge) {
		SNode source = nodesByID.get(origEdge.getSource().getId());
		SNode target = nodesByID.get(origEdge.getSource().getId());

		String edgeType = origEdge.getComponent().getName();
		;
		if (source != null && target != null && source != target) {

			SRelation<?, ?> rel = null;
			switch (origEdge.getComponent().getType()) {
			case Dominance:
				if (edgeType == null || edgeType.isEmpty()) {
					// We don't include edges that have no type if there is an edge
					// between the same nodes which has a type.
					List<Edge> domOutEdges = orig.getOutgoingEdges(origEdge.getSource(), ComponentType.Dominance);
					for (Edge outEdge : domOutEdges) {
						if (outEdge.getTargetID() == origEdge.getTargetID()) {
							// exclude this relation
							return;
						}
					}
				} // end mirror check
				rel = docGraph.createRelation(source, target, SALT_TYPE.SDOMINANCE_RELATION, null);

				break;
			case Pointing:
				rel = docGraph.createRelation(source, target, SALT_TYPE.SPOINTING_RELATION, null);
				break;
			case Ordering:
				rel = docGraph.createRelation(source, target, SALT_TYPE.SORDER_RELATION, null);
				break;
			case Coverage:
				// only add coverage edges in salt to spans, not structures
				if (source instanceof SSpan && target instanceof SToken) {
					rel = docGraph.createRelation(source, target, SALT_TYPE.SSPANNING_RELATION, null);
				}
				break;
			}

			if (rel != null) {
				rel.setType(edgeType);

				// map edge labels
				mapLabels(rel, origEdge.getLabels(), false);

				String layerName = origEdge.getComponent().getLayer();
				if (layerName != null && !layerName.isEmpty()) {
					List<SLayer> layer = docGraph.getLayerByName(layerName);
					if (layer == null || layer.isEmpty()) {
						SLayer newLayer = SaltFactory.createSLayer();
						newLayer.setName(layerName);
						docGraph.addLayer(newLayer);
						layer = Arrays.asList(newLayer);
					}
					layer.get(0).addRelation(rel);
				}
			}
		}
	}

	private void addNodeLayers() {
		List<SNode> nodeList = new LinkedList<>(docGraph.getNodes());
		for (SNode n : nodeList) {
			SFeature featLayer = n.getFeature("annis", "layer");
			if (featLayer != null) {
				SLayer layer = docGraph.getLayer(featLayer.getValue_STEXT());
				if (layer == null) {
					layer = SaltFactory.createSLayer();
					layer.setName(featLayer.getValue_STEXT());
					docGraph.addLayer(layer);
				}
				layer.addNode(n);
			}
		}
	}

	private void recreateText(final String name, List<SNode> rootNodes) {
        final StringBuilder text = new StringBuilder();
        final STextualDS ds = docGraph.createTextualDS("");

        ds.setName(name);

        Map<SToken, Range<Integer>> token2Range = new HashMap<>();

        // traverse the token chain using the order relations
        docGraph.traverse(rootNodes, SGraph.GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "ORDERING_" + name,
                new GraphTraverseHandler() {
                    @Override
                    public void nodeReached(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
                            SNode currNode, SRelation<SNode, SNode> relation, SNode fromNode, long order) {
                        if (fromNode != null) {
                            text.append(" ");
                        }

                        SFeature featTok = currNode.getFeature("annis::tok");
                        if (featTok != null && currNode instanceof SToken) {
                            int idxStart = text.length();
                            text.append(featTok.getValue_STEXT());
                            token2Range.put((SToken) currNode, Range.closed(idxStart, text.length()));
                        }
                    }

                    @Override
                    public void nodeLeft(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
                            SRelation<SNode, SNode> relation, SNode fromNode, long order) {
                    }

                    @SuppressWarnings("rawtypes")
                    @Override
                    public boolean checkConstraint(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
                            SRelation relation, SNode currNode, long order) {
                        if (relation == null) {
                            // TODO: check if this is ever true
                            return true;
                        } else if (relation instanceof SOrderRelation && Objects.equal(name, relation.getType())) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

        // update the actual text
        ds.setText(text.toString());

        // add all relations
        token2Range.forEach((t, r) -> {
            STextualRelation rel = SaltFactory.createSTextualRelation();
            rel.setSource(t);
            rel.setTarget(ds);
            rel.setStart(r.lowerEndpoint());
            rel.setEnd(r.upperEndpoint());
            docGraph.addRelation(rel);
        });
        
        if (docGraph.getTimeline() != null) {
        	
            // create the relations to the timeline for the tokens of this text by getting
            // the original node IDs of the coverage edges and their mapping to a point of time (POT)
            for (SToken tok : token2Range.keySet()) {
                Integer tokID = nodesByID.inverse().get(tok);
                if (tokID != null) {
                    Integer tokPOT = this.node2timelinePOT.get(tokID);
                    if(tokPOT != null)  {
                        // directly map the relation of the token to its POT
                        STimelineRelation rel = SaltFactory.createSTimelineRelation();
                        rel.setSource(tok);
                        rel.setTarget(docGraph.getTimeline());
                        rel.setStart(tokPOT);
                        rel.setEnd(tokPOT);
                        docGraph.addRelation(rel);
                    } else {
                        // find the coverage edges from this node to a token which has a POT
                    	Component covCompoment = new Component(ComponentType.Coverage, "annis", "");
                    	List<Edge> edges = orig.getOutgoingEdges(orig.getNodeForID(tokID), covCompoment);
                        for(Edge e : edges) {                     
                            Integer pot = this.node2timelinePOT.get(e.getTargetID());
                            if(pot != null) {
                                STimelineRelation rel = SaltFactory.createSTimelineRelation();
                                rel.setSource(tok);
                                rel.setTarget(docGraph.getTimeline());
                                rel.setStart(pot);
                                rel.setEnd(pot);
                                docGraph.addRelation(rel);
                            }
                    	
                        }
                    }
                }
            }
        
        }
    }

	private void addTextToSegmentation(final String name, List<SNode> rootNodes) {

		// traverse the token chain using the order relations
		docGraph.traverse(rootNodes, SGraph.GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST, "ORDERING_" + name,
				new GraphTraverseHandler() {
					@Override
					public void nodeReached(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
							SNode currNode, SRelation<SNode, SNode> relation, SNode fromNode, long order) {

						SFeature featTok = currNode.getFeature("annis::tok");
						if (featTok != null && currNode instanceof SSpan) {
							currNode.createAnnotation(null, name, featTok.getValue().toString());
						}
					}

					@Override
					public void nodeLeft(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
							SRelation<SNode, SNode> relation, SNode fromNode, long order) {
					}

					@Override
					public boolean checkConstraint(SGraph.GRAPH_TRAVERSE_TYPE traversalType, String traversalId,
							SRelation relation, SNode currNode, long order) {
						if (relation == null) {
							// TODO: check if this is ever true
							return true;
						} else if (relation instanceof SOrderRelation && Objects.equal(name, relation.getType())) {
							return true;
						} else {
							return false;
						}
					}
				});
	}

	public static SDocumentGraph map(Graph orig) {
		if (orig == null) {
			return null;
		}
		SaltExport export = new SaltExport(orig);

		export.mapDocGraph();
		return export.docGraph;
	}

	private void mapDocGraph() {

		// create all new nodes
		Iterator<Node> itNodes = orig.getNodesByType("node");
		List<Edge> edges = new LinkedList<>();
		while (itNodes.hasNext()) {
			Node node = itNodes.next();

			SNode n = mapNode(node);
			nodesByID.put(node.getId(), n);
			edges.addAll(orig.getOutgoingEdges(node));
		}

		// add nodes to the graph
		nodesByID.values().stream().forEach(n -> docGraph.addNode(n));

		// create and add all edges
		for (Edge e : edges) {
			mapAndAddEdge(e);
		}

		// find all chains of SOrderRelations and reconstruct the texts belonging to
		// them
		Multimap<String, SNode> orderRoots = docGraph.getRootsByRelationType(SALT_TYPE.SORDER_RELATION);
		orderRoots.keySet().forEach((name) -> {
			ArrayList<SNode> roots = new ArrayList<>(orderRoots.get(name));
			if (SaltUtil.SALT_NULL_VALUE.equals(name)) {
				name = null;
			}
			if (name == null || "".equals(name)) {
				// only re-create text if this is the default (possible virtual) tokenization
				recreateText(name, roots);
			} else {
				// add the text as label to the spans
				addTextToSegmentation(name, roots);
			}
		});

		addNodeLayers();
	}

	private static SCorpus addCorpusAndParents(SCorpusGraph cg, int id, Map<Integer, Integer> parentOfNode,
			Map<Integer, SCorpus> id2corpus, Map<Integer, Map<QName, String>> node2labels) {

		if (id2corpus.containsKey(id)) {
			return id2corpus.get(id);
		}

		Map<QName, String> labels = node2labels.get(id);
		if (labels == null) {
			return null;
		}

		// create parents first
		Integer parentID = parentOfNode.get(id);
		SCorpus parent = null;
		if (parentID != null) {
			parent = addCorpusAndParents(cg, parentID, parentOfNode, id2corpus, node2labels);
		}

		String corpusName = labels.getOrDefault(new ImmutablePair<>("annis", "node_name"), "corpus");
		List<String> corpusNameSplitted = Splitter.on('/').trimResults().splitToList(corpusName);
		// use last part of the path as name
		SCorpus newCorpus = cg.createCorpus(parent, corpusNameSplitted.get(corpusNameSplitted.size() - 1));
		id2corpus.put(id, newCorpus);

		return newCorpus;

	}

	public static SCorpusGraph mapCorpusGraph(Graph orig) {
		if (orig == null) {
			return null;
		}
		SCorpusGraph cg = SaltFactory.createSCorpusGraph();


		Map<Integer, Map<QName, String>> node2labels = new LinkedHashMap<>();
		Map<Integer, Integer> parentOfNode = new LinkedHashMap<>();

		// iterate over all nodes and get their outgoing edges
		Iterator<Node> itNodes = orig.getNodesByType("corpus");
		while (itNodes.hasNext()) {
			Node n = itNodes.next();
			Map<QName, String> nodeLabels = n.getLabels();
			node2labels.put(n.getId(), nodeLabels);

			List<Edge> outEdges = orig.getOutgoingEdges(n, ComponentType.PartOfSubcorpus);
			for (Edge edge: outEdges) {
				parentOfNode.put(edge.getSourceID(), edge.getTargetID());
			}
		}

		Map<Integer, SCorpus> id2corpus = new HashMap<>();
		// add all non-documents first
		for (Integer id : parentOfNode.values()) {
			addCorpusAndParents(cg, id, parentOfNode, id2corpus, node2labels);
		}
		for (Map.Entry<Integer, SCorpus> e : id2corpus.entrySet()) {
			Map<QName, String> labels = node2labels.get(e.getKey());
			if (labels != null) {
				mapLabels(e.getValue(), labels, true);
			}
		}

		// add all documents next
		for (Map.Entry<Integer, Integer> edge : parentOfNode.entrySet()) {
			long childID = edge.getKey();
			long parentID = edge.getValue();
			if (!id2corpus.containsKey(childID)) {
				Map<QName, String> labels = node2labels.get(childID);
				if (labels != null) {
					String docName = labels.getOrDefault(new ImmutablePair<>("annis", "doc"), "document");
					SCorpus parent = id2corpus.get(parentID);
					SDocument doc = cg.createDocument(parent, docName);

					mapLabels(doc, labels, true);
				}
			}
		}

		return cg;
	}
}
