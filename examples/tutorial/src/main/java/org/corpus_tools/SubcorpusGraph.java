package org.corpus_tools;

import java.util.Arrays;

import org.corpus_tools.graphannis.CorpusStorageManager;
import org.corpus_tools.graphannis.GraphUpdate;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.graphannis.model.Graph;
import org.corpus_tools.graphannis.model.Node;

public class SubcorpusGraph {
    public static void main(String[] args) throws GraphANNISException {
        CorpusStorageManager cs = new CorpusStorageManager("data");
        GraphUpdate g = new GraphUpdate();
        // create the corpus and document node
        g.addNode("tutorial", "corpus");
        g.addNode("tutorial/doc1", "corpus");
        g.addEdge("tutorial/doc1", "tutorial", "annis", "PartOf", "");
        // add the corpus structure to the existing nodes
        g.addEdge("tutorial/doc1#t1", "tutorial/doc1", "annis", "PartOf", "");
        g.addEdge("tutorial/doc1#t2", "tutorial/doc1", "annis", "PartOf", "");
        g.addEdge("tutorial/doc1#t3", "tutorial/doc1", "annis", "PartOf", "");
        g.addEdge("tutorial/doc1#t4", "tutorial/doc1", "annis", "PartOf", "");
        g.addEdge("tutorial/doc1#t5", "tutorial/doc1", "annis", "PartOf", "");
        g.addEdge("tutorial/doc1#t6", "tutorial/doc1", "annis", "PartOf", "");
        g.addEdge("tutorial/doc1#t7", "tutorial/doc1", "annis", "PartOf", "");
        // apply the changes
        cs.applyUpdate("tutorial", g);
        // get the whole document as graph
        Graph subgraph = cs.subcorpusGraph("tutorial", Arrays.asList("tutorial/doc1"));
        for (Node n : subgraph.getNodesByType("node")) {
            System.out.println(n.getName());
        }
    }
}
