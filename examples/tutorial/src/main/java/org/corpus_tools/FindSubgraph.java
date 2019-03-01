package org.corpus_tools;

import java.util.Arrays;
import java.util.List;

import org.corpus_tools.graphannis.CorpusStorageManager;
import org.corpus_tools.graphannis.Util;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.graphannis.model.Graph;
import org.corpus_tools.graphannis.model.Node;

public class FindSubgraph {
    public static void main(String[] args) throws GraphANNISException {
        CorpusStorageManager cs = new CorpusStorageManager("data");
        String[] matches = cs.find(Arrays.asList("tutorial"), "tok . tok", 0, 100);
        for (String m : matches) {
            System.out.println(m);
            // convert the match string to a list of node IDs
            List<String> node_names = Util.nodeNamesFromMatch(m);
            Graph g = cs.subgraph("tutorial", node_names, 2, 2);
            // iterate over all nodes of type "node" and output the name
            int numberOfNodes = 0;
            for (Node n : g.getNodesByType("node")) {
                numberOfNodes++;
            }
            System.out.println("Number of nodes in subgraph: " + numberOfNodes);
        }
    }
}
