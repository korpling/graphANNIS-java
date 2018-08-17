package org.corpus_tools.graphannis.errors;

public class GraphANNISException extends Exception {
    
    public GraphANNISException() { 
        super();
    }

    public GraphANNISException(String message) {
        super(message);
    }

    public GraphANNISException(Throwable cause) {
        super(cause);
    }

    public GraphANNISException(String message, Throwable cause) {
        super(message, cause);
    }


}
