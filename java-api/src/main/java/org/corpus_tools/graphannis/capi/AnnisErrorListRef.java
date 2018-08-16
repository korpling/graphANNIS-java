package org.corpus_tools.graphannis.capi;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByReference;

import annis.exceptions.AnnisException;

public class AnnisErrorListRef extends ByReference {

    public AnnisErrorListRef() {
        super(Pointer.SIZE);
        setPointer(Pointer.NULL);
    }
    
    public void checkErrors() throws AnnisException {
        if(getPointer() != Pointer.NULL) {
            int num_of_errors = CAPI.annis_error_size(getPointer()).intValue();
            if(num_of_errors > 0) {
                // iterate of the list of all causes, rewinding the causes and getting to the first exception that
                // is the main exception
                AnnisException cause = null;
                for(int i=num_of_errors-1; i >= 0; i++) {
                    String msg = CAPI.annis_error_get_msg(getPointer(), new NativeLong(0));

                    // TODO: map known kinds to more specialized exceptions
                    //String kind = CAPI.annis_error_get_kind(getPointer(), new NativeLong(0));
                    
                    AnnisException ex = new AnnisException(msg, cause);
                    cause = ex;
                }
                if(cause != null) {
                    throw(cause);
                }
            }
        }
    }
    
    public synchronized void dispose()
    {
      try {
        if (this.getPointer() != Pointer.NULL && this.getPointer().getPointer(0) != Pointer.NULL)
        {
          CAPI.annis_free(this.getPointer().getPointer(0));
        }
      } finally {
        this.setPointer(Pointer.NULL);
      }
    }

    @Override
    protected void finalize() throws Throwable
    {
      this.dispose();
      super.finalize();
    }

}
