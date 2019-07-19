package org.corpus_tools.graphannis.capi;

import org.corpus_tools.graphannis.errors.AQLSemanticError;
import org.corpus_tools.graphannis.errors.AQLSyntaxError;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.corpus_tools.graphannis.errors.NoSuchCorpus;
import org.corpus_tools.graphannis.errors.SetLoggerError;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;


public class AnnisErrorListRef extends PointerType {
    
    private final Memory memPtr = new Memory(Native.POINTER_SIZE);
    
    public AnnisErrorListRef() {
        this(null);
    }
    
    public AnnisErrorListRef(Pointer value) {
        setPointer(memPtr);
        setValue(value);
    }

    public void checkErrors() throws GraphANNISException {
        if (getValue() != Pointer.NULL) {
            long num_of_errors = CAPI.annis_error_size(getValue()).longValue();
            if (num_of_errors > 0) {
                // iterate of the list of all causes, rewinding the causes and
                // getting to the first exception that
                // is the main exception
                GraphANNISException cause = null;
                for (long i = num_of_errors - 1; i >= 0; i--) {
                    String msg = CAPI.annis_error_get_msg(getValue(), new NativeLong(i));

                    // TODO: map known kinds to more specialized exceptions
                    String kind = CAPI.annis_error_get_kind(getValue(), new NativeLong(i));
                    
                    if("SetLoggerError".equals(kind)) {
                        cause = new SetLoggerError(msg, cause);
                    } else if("AQLSemanticError".equals(kind)) {
                        cause = new AQLSemanticError(msg, cause);
                    } else if("AQLSyntaxError".equals(kind)) {
                        cause = new AQLSyntaxError(msg, cause);
                    } else if("NoSuchCorpus".equals(kind)) {
                        cause = new NoSuchCorpus(msg, cause);
                    } else {
                        cause = new GraphANNISException(msg, cause);
                    }

                }
                if (cause != null) {
                    throw (cause);
                }
            }
        }
    }
    
    public void setValue(Pointer value) {
        getPointer().setPointer(0, value);
    }
    
    public Pointer getValue() {
        if(memPtr.valid()) {
            return memPtr.getPointer(0);
        }
        return null;
    }

    public synchronized void dispose() {
        Pointer val = getValue();
        if (val != Pointer.NULL) {
            CAPI.annis_free(val);
            setValue(null);
        }   
    }

    @Override
    protected void finalize() throws Throwable {
        this.dispose();
        super.finalize();
    }

}
