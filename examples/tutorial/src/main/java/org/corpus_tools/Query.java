package org.corpus_tools;

import java.util.Arrays;
import java.util.Optional;

import org.corpus_tools.graphannis.CorpusStorageManager;
import org.corpus_tools.graphannis.CorpusStorageManager.QueryLanguage;
import org.corpus_tools.graphannis.errors.GraphANNISException;

public class Query {
    public static void main(String[] args) throws GraphANNISException {
        CorpusStorageManager cs = new CorpusStorageManager("data");
        long number_of_matches = cs.count(Arrays.asList("tutorial"), "tok=/.*s.*/", QueryLanguage.AQL);
        System.out.println("Number of matches: " + number_of_matches);
        String[] matches = cs.find(Arrays.asList("tutorial"), "tok=/.*s.*/", QueryLanguage.AQL, 0, Optional.of(100l));
        for (int i = 0; i < matches.length; i++) {
            System.out.println("Match " + i + ": " + matches[i]);
        }
    }
}
