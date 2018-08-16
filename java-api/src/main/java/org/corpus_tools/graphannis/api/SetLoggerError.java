package org.corpus_tools.graphannis.api;

import annis.exceptions.AnnisException;

public class SetLoggerError extends AnnisException {

    /**
     * 
     */
    private static final long serialVersionUID = 6141581619602233717L;
    
    public SetLoggerError(String msg, AnnisException cause) {
        super(msg, cause);
    }

}
