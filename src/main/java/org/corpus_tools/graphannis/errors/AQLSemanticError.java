package org.corpus_tools.graphannis.errors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AQLSemanticError extends GraphANNISException {

   

    /**
     * 
     */
    private static final long serialVersionUID = 3876163026720755330L;

    public AQLSemanticError(String msg, GraphANNISException cause) {
        super(msg, cause);
    }

}
