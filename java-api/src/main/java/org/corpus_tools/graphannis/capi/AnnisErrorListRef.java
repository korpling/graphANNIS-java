package org.corpus_tools.graphannis.capi;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import annis.exceptions.AnnisException;

public class AnnisErrorListRef extends PointerType {

    public AnnisErrorListRef() {
        this(null);
    }
    
    public AnnisErrorListRef(Pointer value) {
        super(new Memory(Pointer.SIZE));
        setValue(value);
    }

    public void checkErrors() throws AnnisException {
        if (getValue() != Pointer.NULL) {
            long num_of_errors = CAPI.annis_error_size(getValue()).longValue();
            if (num_of_errors > 0) {
                // iterate of the list of all causes, rewinding the causes and
                // getting to the first exception that
                // is the main exception
                AnnisException cause = null;
                for (long i = num_of_errors - 1; i >= 0; i--) {
                    String msg = CAPI.annis_error_get_msg(getValue(), new NativeLong(i));

                    // TODO: map known kinds to more specialized exceptions
                    // String kind = CAPI.annis_error_get_kind(getPointer(), new
                    // NativeLong(0));

                    AnnisException ex = new AnnisException(msg, cause);
                    cause = ex;
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
        return getPointer().getPointer(0);
    }

    public synchronized void dispose() {
        Pointer val = getValue();
        if (val != Pointer.NULL) {
            CAPI.annis_free(val);
        }   
    }

    @Override
    protected void finalize() throws Throwable {
        this.dispose();
        super.finalize();
    }

}
