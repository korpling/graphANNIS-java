package org.corpus_tools.graphannis.errors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SetLoggerError extends GraphANNISException {

    /**
     * 
     */
    private static final long serialVersionUID = 6141581619602233717L;
    
    public SetLoggerError(String msg, GraphANNISException cause) {
        super(msg, cause);
    }

}
