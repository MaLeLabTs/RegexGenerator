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
package it.units.inginf.male.inputs;

import it.units.inginf.male.utils.Range;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * New dataset structure, this is intended to be serialized in Json format using Gson
 * @author MaleLabTs
 */
public class DataSet {

    public DataSet() {
    }

    public DataSet(String name) {
        this.name = name;
    }

    public DataSet(String name, String description, String regexTarget) {
        this.name = name;
        this.description = description;
        this.regexTarget = regexTarget;
    }
    
    public String name;
    public String description;
    public String regexTarget;
    public List<Example> examples = new LinkedList<>();
    
    private transient int numberMatches;
    private transient int numberUnmatches;
    private transient int numberMatchedChars;
    private transient int numberUnmatchedChars;
    private transient int numberUnAnnotatedChars;
    private transient int numberOfChars;
    
    private transient DataSet stripedDataset;
    
    private transient Map<Long, List<DataSet>> separateAndConquerLevels = new ConcurrentHashMap<>();
    //private transient DataSet datasetFocus = null;
    private final static Logger LOG = Logger.getLogger(DataSet.class.getName());  
    
    /**
     * Updates the dataset statistics, numberMatches, numberMatchesChars and so on
     */
    public void updateStats(){
        this.numberMatches = 0;
        this.numberUnmatches = 0;
        this.numberMatchedChars = 0;
        this.numberUnmatchedChars = 0;
        this.numberUnAnnotatedChars = 0;
        this.numberOfChars = 0;
    
        for (Example ex : this.examples) {
            this.numberMatches += ex.match.size();
            this.numberUnmatches += ex.unmatch.size();
            this.numberMatchedChars += ex.getNumberMatchedChars();
            this.numberUnmatchedChars += ex.getNumberUnmatchedChars();
            this.numberOfChars += ex.getNumberOfChars();
        }
        this.numberUnAnnotatedChars = this.numberOfChars - this.numberMatchedChars -this.numberUnmatchedChars;
    }

    public int getNumberMatches() {
        return numberMatches;
    }

    public int getNumberUnmatches() {
        return numberUnmatches;
    }

    public int getNumberMatchedChars() {
        return numberMatchedChars;
    }

    public int getNumberUnmatchedChars() {
        return numberUnmatchedChars;
    }

    public int getNumberUnannotatedChars() {
        return numberUnAnnotatedChars;
    }

    public int getNumberOfChars() {
        return numberOfChars;
    }
    
    public int getNumberExamples(){
        return this.examples.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegexTarget() {
        return regexTarget;
    }

    public void setRegexTarget(String regexTarget) {
        this.regexTarget = regexTarget;
    }
    
    
    
    public String getStatsString(){
        StringBuilder stats = new StringBuilder();
        stats.append("DataSet ").append(this.name).append(" stats:\n")
        .append("number examples: ").append(this.getNumberExamples())
        .append("\noverall chars in dataset: ").append(this.getNumberOfChars())
        .append("\nnumber matches: ").append(this.getNumberMatches())
        .append("\ncharacters in matches: ").append(this.getNumberMatchedChars())
        .append("\nnumber unmatches: ").append(this.getNumberUnmatches())
        .append("\ncharacters in unmatches: ").append(this.getNumberUnmatchedChars())
        .append("\nunannotated chars: ").append(this.getNumberUnannotatedChars());
        return stats.toString();
    }
    
    public void randomize() {
        Random random = new Random();
        for (int i = 0; i < examples.size(); i++) {
            if((((i*100)/examples.size())%5)==0){
                System.out.format("randomize %d%%\n",((i*100)/examples.size()));
            }
            Example ex1;
            Example ex2;
            int destIndex = random.nextInt(examples.size());
            ex1 = examples.get(i);
            ex2 = examples.get(destIndex);
            examples.set(i, ex2);
            examples.set(destIndex, ex1);
        }
    }
    
    public void populateUnmatchesFromMatches(){
            for (Example ex : this.examples) {
                ex.populateUnmatchesFromMatches();
            }
        
    }
    
    /**
     * Populate examples with the temporary list of matched and unmatched strings (using current match and unmatch bounds)
     */
    public void populateAnnotatedStrings(){
            for(Example example : this.examples){
            example.populateAnnotatedStrings();
            }
    }

    public List<Example> getExamples() {
        return this.examples;
    }
    
    /**
     * Create a dataset which is a "view" of the current dataset.A subset of the dataset defined by ranges.
     * @param name
     * @param ranges
     * @return
     */
    public DataSet subDataset(String name, List<Range> ranges){
            // ranges are inclusive
            DataSet subDataset = new DataSet(name);
            for(Range range : ranges){
                for(int index = range.getStartIndex(); index <= range.getEndIndex(); index++){
                    subDataset.getExamples().add(this.getExamples().get(index));
                }           
            }
            return subDataset;
    }
    
    /**
     * 
     * @param marginSize is the how much the margin is bigger than match size. marginSize = 3 means the number of characters in margins is three times the match characters.
     * @return
     */
    public DataSet initStripedDatasetView(double marginSize){
        this.stripedDataset = new DataSet(this.name, this.description, this.regexTarget);
        for(Example example : this.examples){
            this.stripedDataset.getExamples().addAll(this.stripeExample(example, marginSize));
        }        
        return this.stripedDataset;
    }
    
    
    
    /**
     * creates a list of examples created from examples by splitting example in littler pieces.
     * The pieces are created from a window surrounding the example matches. 
     * When two or more windows overlaps, the windows merge together and a single multi-match example is created.
     * @param example
     * @param marginSize
     * @return
    */
        protected static List<Example> stripeExample(Example example, double marginSize){     
        List<Example> slicesExampleList = new LinkedList<>();    
        //create ranges covering the saved portions 
        List<Bounds> savedBounds = new LinkedList<>();
        for(Bounds match : example.getMatch()){
            //double -> int cast works like Math.floor(); Gives the same results of the older integer version
            int charMargin = (int) Math.max(((match.size() * marginSize) / 2.0),1.0);
            Bounds grownMatch = new Bounds(match.start - charMargin, match.end + charMargin);
            grownMatch.start = (grownMatch.start >= 0) ? grownMatch.start:0;
            grownMatch.end = (grownMatch.end <= example.getNumberOfChars()) ? grownMatch.end : example.getNumberOfChars();
            savedBounds.add(grownMatch);
        }
        //compact bounds, create a compact representation of saved portions
        //This bounds represents the slices we are going to cut the example to 
        savedBounds = Bounds.mergeBounds(savedBounds);
        
        //Create examples from slices
        for(Bounds slice : savedBounds){
            Example sliceExample = new Example();
            sliceExample.setString(example.getString().substring(slice.start, slice.end));
            
            //find owned matches
            for(Bounds match : example.getMatch()){
                if(match.start >= slice.end){
                    break; //only the internal for
                } else {
                    Bounds slicedMatch = match.windowView(slice);
                    if(slicedMatch != null){
                        sliceExample.getMatch().add(slicedMatch);
                    }
                }
            }
            //find owned unmatches
            for(Bounds unmatch : example.getUnmatch()){
                if(unmatch.start >= slice.end){
                    break; //only the internal for
                } else {
                    Bounds slicedUnmatch = unmatch.windowView(slice);
                    if(slicedUnmatch != null){
                        sliceExample.getUnmatch().add(slicedUnmatch);
            }
                }
            }
            
            sliceExample.populateAnnotatedStrings();
            slicesExampleList.add(sliceExample);
        }
        return slicesExampleList;
    }
    
    /**
     * Returns the last initialized striped version of this DataSet. In order to change the window size you have to call initStripedDatasetView() again.
     * @return
     */
    public DataSet getStripedDataset(){
//          if(this.stripedDataset == null){
//              Logger.getLogger(this.getClass().getName()).info("getStripedDataset returns, null dataset cause uninitialized striped dataset.");
//          }
          return this.stripedDataset;
    }
    
    /**
     * Returns the "Separate and Conquer" sub-dataset of the requested level.
     * Level 0 is the original dataset, level 1 is the dataset obtained from
     * the first reduction, level 2 is the dataset from the second reduction
     * and so on.
     * 
     * @param divideEtImperaLevel
     * @param jobId
     * @return
     */
    public DataSet getSeparateAndConquerDataSet(int divideEtImperaLevel, int jobId) {
         if(divideEtImperaLevel == 0){
             return this;
         }
         return getSeparateAndConquerLevels(jobId).get(divideEtImperaLevel - 1);
    }
    
    /**
     * Divide dataset, defaults to converting match to unmatches and text extraction problem.
     * @param individualRegex
     * @param jobId
     * @return
     */
    public boolean addSeparateAndConquerLevel(String individualRegex, int jobId){
        return this.addSeparateAndConquerLevel(individualRegex, jobId, true, false);
    }
    
    /**
     * From the last generated "Separate and conquer" sub-dataset, creates a new sub-dataset.
     * We evaluate the individualRegex on the dataset examples, matches that are 
     * correctly extracted are removed. A removed match is converted to unmatch or unannotated depending
     * on the convertToUnmatch value: True==unmatch
     * In striping mode, the base dataset and striped dataset are reduced separately.
     * There is no direct connection from --i.e the third level of the original dataset
     * and the third level of the striped dataset. The reduced dataset depends only 
     * by its parent.
     * NOTE: The levels are the sub-dataset, level 0 is the original dataset, level 1 is
     * the sub-dataset reduced from the original dataset, level 2 is a sub-dataset reduced
     * from the level 1 dataset and so on.
     * @param individualRegex
     * @param jobId
     * @param convertToUnmatch when true, the eliminated matches are converted into unmatches, otherwise unannotated area.
     * @param isFlagging
     * @return true, when the dataset has been modified by reduction
     */
    public boolean addSeparateAndConquerLevel(String individualRegex, int jobId, boolean convertToUnmatch, boolean isFlagging){
        boolean modified = false;
        DataSet oldDataset = this.getLastSeparateAndConquerDataSet(jobId);
        DataSet dataset = oldDataset.reduceSeparateAndConquerDataset(individualRegex, convertToUnmatch, isFlagging);
        dataset.updateStats();
        modified = (dataset.getNumberMatches() != oldDataset.getNumberMatches());
        this.getSeparateAndConquerLevels(jobId).add(dataset);
        if(this.getStripedDataset()!=null){
            modified = this.getStripedDataset().addSeparateAndConquerLevel(individualRegex,jobId, convertToUnmatch, isFlagging) || modified;       
        }
        return modified;
    }
    
   /**
     * Creates a DataSet (sub-dataset for "Separate and conquer") from this dataset instance.
     * We evaluate the individualRegex on the dataset examples, matches that are 
     * correctly extracted are removed. A removed match is converted to unmatch or unannotated depending
     * on the convertToUnmatch value: True==unmatch
     */
    private DataSet reduceSeparateAndConquerDataset(String individualRegex, boolean convertToUnmatch, boolean isFlagging ){
        //initialize pattern matcher
        Pattern pattern = Pattern.compile(individualRegex);
        Matcher individualRegexMatcher = pattern.matcher("");
    
        DataSet reducedDataset = new DataSet(this.name, "Reduction: "+individualRegex, this.regexTarget);
        for(Example example : this.examples){
            if(!isFlagging){
                reducedDataset.getExamples().add(this.reduceSeparateAndConquerExample(example, individualRegexMatcher, convertToUnmatch));
            } else {
                reducedDataset.getExamples().add(this.reduceSeparateAndConquerFlaggingExample(example, individualRegexMatcher));
            }
        }        
        return reducedDataset;
    }
    
    private boolean isTruePositiveFlaggingExample(Example example, Matcher individualRegexMatcher){
        try {
            Matcher m = individualRegexMatcher.reset(example.getString());
            return (m.find() && !example.match.isEmpty());
        } catch (StringIndexOutOfBoundsException ex) {
            return false;
            /**
             * Workaround: riferimento BUG: 6984178
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6984178
             * con i quantificatori greedy restituisce una eccezzione
             * invece che restituire un "false".
             */
        }
    }
    
    
    private Example reduceSeparateAndConquerFlaggingExample(Example example, Matcher individualRegexMatcher){
        //Negative or unannotated are left unchanged
        if(!isTruePositiveFlaggingExample(example, individualRegexMatcher)){
            return new Example(example);
        }
        Example unannotatedExample = new Example();
        unannotatedExample.setString(example.getString());
        return unannotatedExample;
    }
    
    private Example reduceSeparateAndConquerExample(Example example, Matcher individualRegexMatcher, boolean convertToUnmatch){
        return this.manipulateSeparateAndConquerExample(example, individualRegexMatcher, convertToUnmatch);
    }
    
    //creates a reduced ("Separate and conquer") Example instance from an example and a given Regex Instance 
    //ELIMINATED Feature: When doFocus is true, the method perform focus action instead of examples reduction (Focus action creates the complementary of the reduction in order to focus evolution).
    
    //When convertToUnmatch is true extracted matches are converted into unannotated. 
    private Example manipulateSeparateAndConquerExample(Example example, Matcher individualRegexMatcher, boolean convertToUnmatch){
        Example exampleClone = new Example(example);
        List<Bounds> extractions = new LinkedList<>();
        try {
            Matcher m = individualRegexMatcher.reset(example.getString());
            while (m.find()) {
                Bounds bounds = new Bounds(m.start(0), m.end(0));
                extractions.add(bounds);
            }
        } catch (StringIndexOutOfBoundsException ex) {
            /**
             * Workaround: riferimento BUG: 6984178
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6984178
             * con i quantificatori greedy restituisce una eccezzione
             * invece che restituire un "false".
             */
        }
        
        //remove extracted matches
        for (Iterator<Bounds> it = exampleClone.getMatch().iterator(); it.hasNext();) {
            Bounds match = it.next();
            for(Bounds extraction : extractions){
                //when doFocus is true, match is remove when equals
                if(match.equals(extraction)){
                    it.remove();
                    if(convertToUnmatch){
                        exampleClone.getUnmatch().add(match);
                    }
                    break;
                }
                //Optimization
                //extractions are in ascending order.. because find is going to extract in this order
                if(extraction.start > match.end){
                    break;
                }
            }
        }
        
        exampleClone.mergeUnmatchesBounds();
        exampleClone.populateAnnotatedStrings();
        return exampleClone;
    }
    
    public DataSet getLastSeparateAndConquerDataSet(int jobId){
        List<DataSet> datasetsList =  this.getSeparateAndConquerLevels(jobId);
        if(datasetsList.isEmpty()){
            return this;
        }
        return datasetsList.get(datasetsList.size() -1);
    }
    
    public int getNumberOfSeparateAndConquerLevels(int jobId){
        return this.getSeparateAndConquerLevels(jobId).size();
    }
    
    
    /**
     * Returns all the "Separate and conquer" sub-datasets lists, for all the current threads.
     * The map resolves a ThreadID to the thread sub-datasets (levels) list. 
     * @return
     */
    public Map<Long, List<DataSet>> getAllSeparateAndConquerLevels(){
        return this.separateAndConquerLevels;
    }
    
    /**
     * Returns the list of the sub-datasets created by "Separate and conquer", for the current thread.
     * The threads (aka active Jobs) have their own sub-datasets. This works right as
     * far as there is a *:1 relation between Jobs and Threads and we clean the generated
     * levels when a new Job is stated.
     * The levels are the sub-dataset, level 0 is the original dataset, level 1 is
     * the sub-dataset reduced from the original dataset, level 2 is a sub-dataset reduced
     * from the level 1 dataset and so on.
     * @param jobId
     * @return The list of sub datasets
     */
    public List<DataSet> getSeparateAndConquerLevels(long jobId){
        if (this.separateAndConquerLevels.containsKey(jobId)){
            return this.separateAndConquerLevels.get(jobId);
        } else {
            List<DataSet> newDatasetList = new LinkedList<>();
            this.separateAndConquerLevels.put(jobId, newDatasetList);
            return newDatasetList;
        }
    }
    
    /**
     * Resets the "Separate and conquer" for the current thread only.
     * Deletes all the generated (reduced) sub-datasets for the current thread.
     */
    public void resetSeparateAndConquer(long jobId){
        this.getSeparateAndConquerLevels(jobId).clear();
        if(this.getStripedDataset()!=null){
            this.getStripedDataset().getSeparateAndConquerLevels(jobId).clear();
        }
    }
    
    
    /**
     * Resets the "Separate and conquer", delete reduced sub-datasets, for all threads.
     */
    public void resetAllSeparateAndConquer(){
        this.getAllSeparateAndConquerLevels().clear();
    }
    
    
    public void removeSeparateAndConquerLevel(int jobID){
        List<DataSet> separateAndConquerLevelsForJob = this.getSeparateAndConquerLevels(jobID);
        if(!separateAndConquerLevelsForJob.isEmpty()){
            separateAndConquerLevelsForJob.remove(separateAndConquerLevelsForJob.size()-1);
        }
    }

   
    
    public static class Example {

        public Example() {
        }
        
        public Example(Example example) {
            this.string = example.string;
            this.match= new LinkedList<>(example.match);
            this.unmatch = new LinkedList<>(example.unmatch);
            if(example.matchedStrings!=null){
                this.matchedStrings = new LinkedList<>(example.matchedStrings);
            }
            if(example.unmatchedStrings!=null){
                this.unmatchedStrings = new LinkedList<>(example.unmatchedStrings);
        }
        }
        
        public String string;
        public List<Bounds> match = new LinkedList<>();
        public List<Bounds> unmatch = new LinkedList<>();
        transient protected List<String> matchedStrings = new LinkedList<>();
        transient protected List<String> unmatchedStrings = new LinkedList<>();

        public void addMatchBounds(int bs, int bf) {
            Bounds boundaries = new Bounds(bs, bf);
            match.add(boundaries);
        }
        
        public void addUnmatchBounds(int bs, int bf) {
            Bounds boundaries = new Bounds(bs, bf);
            unmatch.add(boundaries);
        }
        
        public int getNumberMatchedChars(){
            return getNumberCharsInsideIntervals(match);
        }
        
        public int getNumberUnmatchedChars(){
            return getNumberCharsInsideIntervals(unmatch);
        }
        
        public int getNumberOfChars(){
            return string.length();
        }
        
        private int getNumberCharsInsideIntervals(List<Bounds> textIntervals){
            int countChars = 0;
            for(Bounds interval : textIntervals){
                countChars += (interval.end - interval.start);
            }
            return countChars;
        }
        
        public void populateAnnotatedStrings(){
            this.matchedStrings.clear();
            for(Bounds bounds : this.match){
                this.matchedStrings.add(this.string.substring(bounds.start,bounds.end));
            }
            this.unmatchedStrings.clear();
            for(Bounds bounds : this.unmatch){
                this.unmatchedStrings.add(this.string.substring(bounds.start,bounds.end));
        }
        }

        
        public List<String> getMatchedStrings() {
            return matchedStrings;
        }
      
        public List<String> getUnmatchedStrings() {
            return unmatchedStrings;
        }
        
        public String getString() {
            return string;
        }

        public List<Bounds> getMatch() {
            return match;
        }

        public List<Bounds> getUnmatch() {
            return unmatch;
        }

        public void setString(String string) {
            this.string = string;
        }
       
        
        /**
         * Creates fully annotated examples based on the provided matches (bounds)
         * All chars are going to be annotated. 
         */
        public void populateUnmatchesFromMatches(){
            this.unmatch.clear();
            //generate unmatches
                int previousMatchFinalIndex = 0;
                for(DataSet.Bounds oneMatch : this.match){
                    if(oneMatch.start > previousMatchFinalIndex){
                        this.addUnmatchBounds(previousMatchFinalIndex, oneMatch.start);
                    }
                    previousMatchFinalIndex = oneMatch.end;
                }
                if(previousMatchFinalIndex < string.length()){
                    /*
                    the right value of the interval can be equal than the string.lenght
                    because the substrings are left-inclusive and right-exclusive
                    */
                    this.addUnmatchBounds(previousMatchFinalIndex, string.length());
                }
        }

        public int getNumberMatches() {
            return this.match.size();
        }
        
        /**
         * Returns the annotated strings, matches and unmatches
         * @return a list of all annotated strings
         */
        public List<String> getAnnotatedStrings(){
            List<String> annotatedStrings = new LinkedList<>();
            for(Bounds bounds : this.getMatch()){
                annotatedStrings.add(this.getString().substring(bounds.start, bounds.end));
            }
            for(Bounds bounds : this.getUnmatch()){
                annotatedStrings.add(this.getString().substring(bounds.start, bounds.end));
            }
            return annotatedStrings;
        }
        
        /**
         * This method generates all the annotated strings; the strings are in the same order
         * they appear into the example. This method has been created in order to mantain the
         * same behavior for the getAnnotatedStrings method (different order).
         * @return
         */
        public List<String> getOrderedAnnotatedStrings(){
            List<Bounds> boundsList = new LinkedList<>(this.getMatch());
            boundsList.addAll(this.getUnmatch());
            Collections.sort(boundsList);  
            List<String> annotatedStrings = new LinkedList<>();
            for(Bounds bounds : boundsList){
                annotatedStrings.add(this.getString().substring(bounds.start, bounds.end));
    }
            return annotatedStrings;
        }
    
         
        public void mergeUnmatchesBounds(){
            this.unmatch = Bounds.mergeBounds(this.unmatch);
        }
        
    }
    
    static public class Bounds implements Comparable<Bounds>{
        public Bounds(int start, int end) {
            this.start = start;
            this.end = end;
        }
        public int start;
        public int end;
        
        public int size(){
            return ( this.end - this.start );
        };

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + this.start;
            hash = 97 * hash + this.end;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Bounds other = (Bounds) obj;
            return ((this.end == other.end)&&(this.start == other.start));
        }
        
        /**
         * Convert a list of bounds in a more compact representation; this works with overlapping intervals too.
         * Remind that bounds define ranges with start inclusive and end inclusive index.
         * @param boundsList
         * @return
         */
        static public List<Bounds> mergeBounds(List<Bounds> boundsList){
            List<Bounds> newBoundsList = new LinkedList<>();
            if(boundsList.isEmpty()){
                return newBoundsList;
            }
            Collections.sort(boundsList);
            Bounds prevBounds = new Bounds(boundsList.get(0).start, boundsList.get(0).end);
            for (int i = 1; i < boundsList.size(); i++) {
                Bounds currentBounds = boundsList.get(i);
                if(currentBounds.start <= prevBounds.end){
                    //merge
                    prevBounds.end = Math.max(currentBounds.end,prevBounds.end);
                } else {
                    newBoundsList.add(prevBounds);
                    prevBounds = new Bounds(currentBounds.start, currentBounds.end);
                }
            }
            newBoundsList.add(prevBounds);
            return newBoundsList;
        }
        
        /**
         * Creates a new Bounds object representing this interval relative a rangeBounds
         * interval. --i.e: this [4,9] ; this.windowView([2,7]) == [2,7] 
         * When this Bounds object is out of the window, windowView returns null. 
         * @param rangeBounds
         * @return
         */
        public Bounds windowView(Bounds rangeBounds){
            Bounds newBounds = new Bounds(this.start-rangeBounds.start, this.end-rangeBounds.start);
            if((newBounds.start >= rangeBounds.size())||(newBounds.end<=0)){
                return null; //Out of window
            }
            newBounds.start = Math.max(newBounds.start,0);
            newBounds.end = Math.min(newBounds.end,rangeBounds.size());
            return newBounds;
        }
        
        @Override
        public int compareTo(Bounds o) {
           return (this.start - o.start);
        }
        
        public boolean isSubsetOf(Bounds bounds){
            return (this.start >= bounds.start) && (this.end <= bounds.end);
        }
        
        /**
         * When this Bounds objects overlaps the bounds argument (range intersection is not empty) returns true; otherwise returns false.
         * Zero length bounds are considered overlapping when they adhere each other --i.e: [3,5] and [5,5] are considered overlapping
         * [3,3] and [3,8] are overlapping ranges.
         * @param bounds The range to compare, for intersections, with the calling instance 
         * @return true when the ranges overlap
         */
        public boolean overlaps(Bounds bounds){
            return this.start == bounds.start || this.end == bounds.end || ((this.start < bounds.end) && (this.end > bounds.start));
        }
    
        
        //extracted ranges are always ordered collection. In case you have to sort the collection first.
        //AnnotatedRanges are sorted internally by the method
        /**
         * Counts the number of checkedRanges that overlaps with the zoneRanges. A Bounds object in checkedRanges who doesn't overlap
         * with zoneRanges are not counted.
         * @param ranges
         * @param zoneRanges
         * @return
         */
        static public int countRangesThatCollideZone(List<Bounds> ranges, List<Bounds> zoneRanges) {
            int overallEOAA = 0;
            Collections.sort(zoneRanges);  
             
            //This approach relies on the fact that both annotatedRanges and extracted ranges are ordered
            for (Bounds extractedBounds : ranges) {
                for (Bounds expectedBounds : zoneRanges) {
                    if(expectedBounds.start >= extractedBounds.end){
                        break;
                    } 
                    if(extractedBounds.overlaps(expectedBounds)){
                        overallEOAA++;
                        break;
                    }
                }
            }
            return overallEOAA;
        }

    }
    
    public Example getExample(int index){
        return examples.get(index);
    }
}
