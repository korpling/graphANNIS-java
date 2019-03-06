package org.corpus_tools;

import org.corpus_tools.graphannis.CorpusStorageManager;
import org.corpus_tools.graphannis.errors.GraphANNISException;

public class ListCorpora {
    public static void main(String[] args) throws GraphANNISException {
        CorpusStorageManager cs = new CorpusStorageManager("data");
        String[] corpora = cs.list();
        System.out.println(corpora.length);
    }
}
