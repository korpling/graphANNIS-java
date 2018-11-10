package org.corpus_tools.graphannis.model;

import java.util.List;

/**
 * FrequencyTableEntry
 */
public class FrequencyTableEntry<T> {

    private long count;
    private T[] tuple;

    public FrequencyTableEntry() {

    }

    /**
     * @return the count
     */
    public long getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(long count) {
        this.count = count;
    }

    public FrequencyTableEntry(T[] tuple, long count) {
        this.setCount(count);
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


}