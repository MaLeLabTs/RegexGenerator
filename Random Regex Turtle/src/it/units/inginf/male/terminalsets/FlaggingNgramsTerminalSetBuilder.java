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
package it.units.inginf.male.terminalsets;
import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.inputs.DataSet.Example;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Leaf;
import it.units.inginf.male.tree.NodeFactory;
import it.units.inginf.male.utils.Utils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Initialize terminalSet from examples.   
 * @author Fabiano
 */
public class FlaggingNgramsTerminalSetBuilder implements TerminalSetBuilder{
    
    //Parameters defaults
    private int NUMBER_NGRAMS = 10; 
    private boolean PENALIZE_NEGATIVES_NGRAMS = true;
      
    
    /**
     * This setup is additive, this is not going to reset the NodeFactory configuration; 
     * There is ONE configuration per job, and the configurations have separate node factories.
     * When you need to reset the NodeFactory, you have to call configuration.initNodeFactory()
     * method and you are going to obtain a NodeFactory based only to provided constants, ranges
     * and operators.
     * @param configuration
     */
    @Override
    public void setup(Configuration configuration) {
        this.setup(configuration, configuration.getDatasetContainer().getTrainingDataset());
    }
    
    
    public void setup(Configuration configuration, DataSet dataset) {
        Map<String, String> parameters = configuration.getTerminalSetBuilderParameters();
        if(configuration.getTerminalSetBuilderParameters()!=null){
            if(parameters.containsKey("cumulateScoreMultipliers")){
                NUMBER_NGRAMS = Integer.valueOf(parameters.get("numberNgrams"));
            }
            if(parameters.containsKey("penalizeNegativeNgrams")){
                PENALIZE_NEGATIVES_NGRAMS = Boolean.valueOf(parameters.get("penalizeNegativeNgrams"));
            }
        }
        
        //This is used later for Ranges computation
        SortedSet<Character> charset = new TreeSet<>();
       
        //Ngrams
        //Find out the common ngrams into positives examples
        NodeFactory nodeFactory = configuration.getNodeFactory(); 
        Set<Leaf> terminalSet = new HashSet<>(nodeFactory.getTerminalSet());
        
        Map<String, Long> ngrams = new HashMap<>();
        List<Example> examples = dataset.getExamples();
        List<Example> positiveExamples = new ArrayList<>();
        List<Example> negativeExamples = new ArrayList<>();
        for(Example example : examples){
            if(!example.getMatch().isEmpty()){
                positiveExamples.add(example);
            } else {
                negativeExamples.add(example);
            }
        }
        
        /**
         * Uses postive examples n-grams and gives a score 
         * +1 for each positive example which contains it
         */
        for (Example example : positiveExamples) {           
            //find out all used chars in positive examples
            for (char c : example.getString().toCharArray()) {
                    charset.add(c);
            }
            //assign scores to ngrams
            Set<String> subparts = Utils.subparts(example.getString());
            for (String x : subparts) {
                long v = ngrams.containsKey(x) ? ngrams.get(x) : 0;
                v++;
                ngrams.put(x, v);
            }
        }
        
       /**
         * Find out n-grams in negative examples and gives a score 
         * -1 for each negative example which contains it
         * We like to reward ngrams which are in the positives but not in the negatives
         */
        if(PENALIZE_NEGATIVES_NGRAMS){
            for (Example example : negativeExamples) {           
                Set<String> subparts = Utils.subparts(example.getString());
                for (String x : subparts) {
                    long v = ngrams.containsKey(x) ? ngrams.get(x) : 0;
                    v--;
                    ngrams.put(x, v);
                }
            }
        }
        
        ngrams = this.sortByValues(ngrams);
        
        long numberNgrams = 0; //Considering positives only. Is the sum of the ngrams ratios. 
        for (Map.Entry<String, Long> entry : ngrams.entrySet()) {
                String  ngram = entry.getKey();
                Long v = entry.getValue();
                if(v <= 0){
                    //drops ngrams with negative scores
                    continue;
                }
                Leaf leaf = new Constant(Utils.escape(ngram));
                if(terminalSet.add(leaf)){
                    numberNgrams++;              
                }
                if(numberNgrams>=NUMBER_NGRAMS){
                    break;
                }
        }
        
        //Add all the met characters to the Training Set
        //All the caracters are added, there is no filtering 
        //(this is different from ngrams larger than 1) 
        for (char c : charset) {
            terminalSet.add(new Constant(Utils.escape(c)));
        }
        
        terminalSet.addAll(Utils.generateRegexRanges(charset));
       
        nodeFactory.getTerminalSet().clear();
        nodeFactory.getTerminalSet().addAll(terminalSet);        
        
    }
             
     /**
     * Big to little score order. When score equals, bigger strings (length-wise) win. 
     * Always the return value is backed by a TreeMap.
     * @param <K>
     * @param <V>
     * @param map
     * @return Restituisce una TreeMap
     */
    private <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
            @Override
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) {
                    String s1 = (String) k1;
                    String s2 = (String) k2;
                    compare = s2.length() - s1.length();
                    if(compare == 0){
                        compare = s1.compareTo(s2);
                    }
                    return compare;
                } else {
                    return compare;
                }
            }
        };
        Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    @Override
    public void setup(Context context) {
        context.getConfiguration().initNodeFactory();
        setup(context.getConfiguration(), context.getCurrentDataSet());
    }
}