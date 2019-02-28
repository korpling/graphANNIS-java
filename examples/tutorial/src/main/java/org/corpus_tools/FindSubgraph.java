package org.corpus_tools;

import java.util.Arrays;

import org.corpus_tools.graphannis.CorpusStorageManager;
import org.corpus_tools.graphannis.errors.GraphANNISException;

public class FindSubgraph {
    public static void main(String[] args) throws GraphANNISException {
        CorpusStorageManager cs = new CorpusStorageManager("data");
        String[] matches = cs.find(Arrays.asList("tutorial"), "tok . tok", 0, 100);
        // for(String m : matches) {
        //     cs.subgraph("tutorial", node_ids, ctx_left, ctx_right)
        // }
        // for m in matches:
        //     print(m)
        //     G = cs.subgraph("tutorial", node_name_from_match(m), ctx_left=2, ctx_right=2)
        //     print("Number of nodes in subgraph: " + str(len(G.nodes)))
    }
}
