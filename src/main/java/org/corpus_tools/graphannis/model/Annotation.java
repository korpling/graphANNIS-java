package org.corpus_tools.graphannis.model;

/**
 *
 */
public class Annotation {

    private AnnoKey key;
    private String value;

    public Annotation() {

    }

    /**
     * @return the key
     */
    public AnnoKey getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(AnnoKey key) {
        this.key = key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
}