package org.corpus_tools.graphannis.model;

/**
 * FrequencyDefEntry
 */
public class FrequencyDefEntry {

    private String ns;
    private String name;
    private String nodeRef;

    public FrequencyDefEntry() {

    }

    /**
     * @return the nodeRef
     */
    public String getNodeRef() {
        return nodeRef;
    }

    /**
     * @param nodeRef the nodeRef to set
     */
    public void setNodeRef(String nodeRef) {
        this.nodeRef = nodeRef;
    }

    /**
     * @return the ns
     */
    public String getNs() {
        return ns;
    }

    /**
     * @param ns the ns to set
     */
    public void setNs(String ns) {
        this.ns = ns;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

}