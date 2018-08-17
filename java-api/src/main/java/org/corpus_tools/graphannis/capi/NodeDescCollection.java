package org.corpus_tools.graphannis.capi;

import java.util.ArrayList;
import java.util.List;

import org.corpus_tools.graphannis.api.NodeDesc;

import com.sun.jna.NativeLong;
import com.sun.jna.PointerType;

public class NodeDescCollection extends PointerType {
    

    public int getSize() {
        return CAPI.annis_vec_nodedesc_size(this).intValue();
    }

    public List<NodeDesc> getList() {
        final int size = getSize();
        
        List<NodeDesc> result = new ArrayList<>(size);
        for(int i=0; i < size; i++) {
            NodeDesc newNodeDesc = new NodeDesc();
            newNodeDesc.setComponentNr(CAPI.annis_vec_nodedesc_get_component_nr(this, new NativeLong(i)).longValue());
            newNodeDesc.setAqlFragment(CAPI.annis_vec_nodedesc_get_aql_fragment(this, new NativeLong(i)).toString());
            newNodeDesc.setVariable(CAPI.annis_vec_nodedesc_get_variable(this, new NativeLong(i)).toString());
            
        }
        return result;
    }
    

}
