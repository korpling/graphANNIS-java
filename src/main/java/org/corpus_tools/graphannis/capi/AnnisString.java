package org.corpus_tools.graphannis.capi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class AnnisString extends Structure {

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList();
    }

}
