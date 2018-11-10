package org.corpus_tools.graphannis.model;

import java.util.List;

/**
 * FrequencyTableEntry
 */
public class FrequencyTableEntry<T> {

    private long size;
    private T[] tuple;

    public FrequencyTableEntry() {

    }

    public FrequencyTableEntry(T[] tuple, long size) {
        this.size = size;
        this.tuple = tuple;
    }

    /**
     * @return the tuple
     */
    public T[] getTuple() {
        return tuple;
    }

    /**
     * @param tuple the tuple to set
     */
    public void setTuple(T[] tuple) {
        this.tuple = tuple;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

}