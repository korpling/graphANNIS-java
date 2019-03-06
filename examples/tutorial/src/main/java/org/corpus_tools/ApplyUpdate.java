package org.corpus_tools;

import org.corpus_tools.graphannis.CorpusStorageManager;
import org.corpus_tools.graphannis.GraphUpdate;
import org.corpus_tools.graphannis.errors.GraphANNISException;

public class ApplyUpdate {
    public static void main(String[] args) throws GraphANNISException {
        CorpusStorageManager cs = new CorpusStorageManager("data");

        GraphUpdate g = new GraphUpdate();

        // First argument is the node name.
        g.addNode("tutorial/doc1#t1");
        // First argument is the node name, 
        // then comes the annotation namespace, name and value.
        g.addNodeLabel("tutorial/doc1#t1", "annis", "tok", "That");

        g.addNode("tutorial/doc1#t2");
        g.addNodeLabel("tutorial/doc1#t2", "annis", "tok", "is");

        g.addNode("tutorial/doc1#t3");
        g.addNodeLabel("tutorial/doc1#t3", "annis", "tok", "a");

        g.addNode("tutorial/doc1#t4");
        g.addNodeLabel("tutorial/doc1#t4", "annis", "tok", "Category");

        g.addNode("tutorial/doc1#t5");
        g.addNodeLabel("tutorial/doc1#t5", "annis", "tok", "3");

        g.addNode("tutorial/doc1#t6");
        g.addNodeLabel("tutorial/doc1#t6", "annis", "tok", "storm");

        g.addNode("tutorial/doc1#t7");
        g.addNodeLabel("tutorial/doc1#t7", "annis", "tok", ".");

        // Add the ordering edges to specify token order.
        // The names of the source and target nodes are given as arguments, 
        // followed by the component layer, type and name.
        g.addEdge("tutorial/doc1#t1", "tutorial/doc1#t2", "annis", "Ordering", "");
        g.addEdge("tutorial/doc1#t2", "tutorial/doc1#t3", "annis", "Ordering", "");
        g.addEdge("tutorial/doc1#t3", "tutorial/doc1#t4", "annis", "Ordering", "");
        g.addEdge("tutorial/doc1#t4", "tutorial/doc1#t5", "annis", "Ordering", "");
        g.addEdge("tutorial/doc1#t5", "tutorial/doc1#t6", "annis", "Ordering", "");
        g.addEdge("tutorial/doc1#t6", "tutorial/doc1#t7", "annis", "Ordering", "");

        cs.applyUpdate("tutorial", g);
        String[] corpora = cs.list();
        if(corpora.length > 0) {
            System.out.println(corpora[0]);
        } else {
            System.out.println("No corpus found");
        }
        
    }
}
