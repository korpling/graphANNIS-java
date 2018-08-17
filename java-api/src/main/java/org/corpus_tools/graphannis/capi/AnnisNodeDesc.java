package org.corpus_tools.graphannis.capi;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

public class AnnisNodeDesc extends Structure {
    
    public NativeLong component_nr;
    public AnnisString aql_fragment;
    public AnnisString variable;


    @Override
    protected List<String> getFieldOrder()
    {
      return Arrays.asList("component_nr", "aql_fragment", "variable");
    }

    public static class ByReference extends AnnisEdge implements Structure.ByReference
    {
    }

    public static class ByValue extends AnnisEdge implements Structure.ByValue
    {
    }

}
