/*
 * Copyright (C) 2015 Machine Learning Lab - University of Trieste, 
 * Italy (http://machinelearning.inginf.units.it/)  
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.inginf.male.utils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This object represents a range of contigous row indexes
 * @author MaleLabTs
 */
public class Range implements Comparable<Range>{
    private int startIndex;
    private int endIndex;

    public Range(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    /**
     * The index of the first row (inclusive) of the range
     * @return the index
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * The index of the last row (inclusive) of the range
     * @return the index
     */
    public int getStartIndex() {
        return startIndex;
    }


    public int getLength(){
        return endIndex-startIndex+1;
    }
    
    /**
     * Merges ranges in order to obtain a more compact representation. This is intended to work with no-overlapping Ranges.
     * Behavior with overlapping ranges is undefined.
     * @param ranges
     * @return
     */
    static public List<Range> compactRanges(List<Range> ranges){
        List<Range> newRanges = new LinkedList<>();
        if(ranges.isEmpty()){
            return newRanges;
        }
        
        Collections.sort(ranges);
        Range prevRange = new Range(ranges.get(0).startIndex, ranges.get(0).endIndex);
        for (int i = 1; i < ranges.size(); i++) {
            Range currentRange = ranges.get(i);
            if(currentRange.startIndex == prevRange.endIndex+1){
                //merge
                prevRange.endIndex = currentRange.endIndex;
            } else {
                newRanges.add(prevRange);
                prevRange = new Range(currentRange.startIndex, currentRange.endIndex);
            }
        }
        newRanges.add(prevRange);
        return newRanges;
    }

    @Override
    public int compareTo(Range o) {
       return (this.startIndex - o.startIndex);
    }
    
    
}
