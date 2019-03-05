package org.corpus_tools.graphannis;

import java.util.ArrayList;
import java.util.List;

public class Util {
    /** Takes a match identifier (which includes the matched annotation name) and returns the node name. */
    public static List<String> nodeNamesFromMatch(String matchLine) {
        List<String> result = new ArrayList<>();
        if(matchLine != null) {
            String[] matches = matchLine.split("\\s+");
            for (String m : matches) {
                String[] elements = m.split("::", 3);
                result.add(elements[elements.length-1]);
            }
        }
        return result;
    }
}
