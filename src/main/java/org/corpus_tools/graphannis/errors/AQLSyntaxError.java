package org.corpus_tools.graphannis.errors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AQLSyntaxError extends GraphANNISException {

    
    /**
     * 
     */
    private static final long serialVersionUID = 5765081068662324088L;

    public AQLSyntaxError(String msg, GraphANNISException cause) {
        super(msg, cause);
    }

}
