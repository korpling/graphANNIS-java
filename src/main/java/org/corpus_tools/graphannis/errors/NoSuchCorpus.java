package org.corpus_tools.graphannis.errors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NoSuchCorpus extends GraphANNISException {


    /**
     * 
     */
    private static final long serialVersionUID = -2629892226819554466L;

    public NoSuchCorpus(String msg, GraphANNISException cause) {
        super(msg, cause);
    }

}
