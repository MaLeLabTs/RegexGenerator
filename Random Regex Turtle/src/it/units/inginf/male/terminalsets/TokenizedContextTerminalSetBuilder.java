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
import it.units.inginf.male.utils.BasicTokenizer;
import it.units.inginf.male.utils.Tokenizer;
import it.units.inginf.male.utils.Utils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Initialize terminal set from examples (tokens, ranges) and add significant tokens to the terminal set.
 * It uses two separate algorithms for tokens on matches and umathces.
 * This terminalset builder always adds character classes \d \w.
 * As usual, it adds terminal sets to the predefined set in configuration file. 
 * The configuration file should contain a list of constant with predefined separators.
 * Accepts these configuration population builder parameters:
 * "tokenThreashold","discardWtokens","tokenUnmatchThreashold"
 * @author MaleLabTs
 */
public class TokenizedContextTerminalSetBuilder implements TerminalSetBuilder{
    
    static private final Tokenizer tokenizer = new BasicTokenizer();
    
    //It is true when string matches \w (.i.e. its length is one and it is alphabetic or decimal number)
    static private boolean matchW(String string){
        return (string.length()==1 && matchW(string.charAt(0)));
    }
    
    static private boolean matchW(char character){
        return Character.isAlphabetic(character) || Character.isDigit(character) || character == '_';
    }
    
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
    
    /**
     * This setup is additive, this is not going to reset the NodeFactory configuration; 
     * There is ONE configuration per job, and the configurations have separate node factories.
     * When you need to reset the NodeFactory, you have to call configuration.initNodeFactory()
     * method and you are going to obtain a NodeFactory based only to provided constants, ranges
     * and operators.
     * @param configuration
     * @param trainingDataset
     */
    public void setup(Configuration configuration, DataSet trainingDataset) {
        
        Double TOKEN_THREASHOLD = 80.0; 
        Double TOKEN_UNMATCH_THREASHOLD = 80.0;
        boolean DISCARD_W_TOKENS = true; //Discard all tokens who match \w
        
        //Change to striped dataset when striped version is initialized
        //IMPORTANT in case of striped dataset, the striped version is always used. This is coherent with the 
        //population builder behavior
        if (trainingDataset.getStripedDataset()!=null){
            trainingDataset = trainingDataset.getStripedDataset();
        }
        
        Map<String, String> parameters = configuration.getPopulationBuilderParameters();
        if(parameters!=null){
            //add parameters if needed
            if(parameters.containsKey("tokenThreashold")){
                TOKEN_THREASHOLD = Double.valueOf(parameters.get("tokenThreashold"));
            }
            if(parameters.containsKey("discardWtokens")){
                DISCARD_W_TOKENS = Boolean.valueOf(parameters.get("discardWtokens"));
            }
            if(parameters.containsKey("tokenUnmatchThreashold")){
                TOKEN_UNMATCH_THREASHOLD = Double.valueOf(parameters.get("tokenUnmatchThreashold"));
            }
        }
        
        //This is used later for Ranges computation and to define the used character set
        SortedSet<Character> charset = new TreeSet<>();
        
        
        NodeFactory nodeFactory = configuration.getNodeFactory();
        
        
        Set<Leaf> terminalSet = new HashSet<>(nodeFactory.getTerminalSet());
        
        
                
        for (Example example : trainingDataset.getExamples()) {
            for (String match : example.getMatchedStrings()) {
                
                //Create the used charset set.
                for(char c : match.toCharArray()){
                    charset.add(c);
                }
            }
        }
       
        
        Map<String, Double> winnerTokens = TokenizedContextTerminalSetBuilder.calculateWinnerMatchTokens(trainingDataset, TOKEN_THREASHOLD, DISCARD_W_TOKENS);
        Map<String, Double> winnerUnMatchTokens = TokenizedContextTerminalSetBuilder.calculateWinnerUnmatchTokens(trainingDataset, TOKEN_UNMATCH_THREASHOLD,DISCARD_W_TOKENS);
        winnerTokens.putAll(winnerUnMatchTokens);
        
        //adds winnertokens (match and unmatch) to the terminal set
        for (Map.Entry<String, Double> entry : winnerTokens.entrySet()) {
                String  token = entry.getKey();
                double v = entry.getValue();
                Leaf leaf = new Constant(Utils.escape(token));
                terminalSet.add(leaf);

        }
        
        
        
        //Generate ranges from characters
        terminalSet.addAll(Utils.generateRegexRanges(charset));
        
        //Add classes
        terminalSet.add(new Constant("\\d"));
        terminalSet.add(new Constant("\\w"));
        //terminalSet.add(new Constant("\\s"));
        
        nodeFactory.getTerminalSet().clear();
        nodeFactory.getTerminalSet().addAll(terminalSet);        
        
    }

    
    static public Map<String,Double> calculateWinnerMatchTokens(DataSet dataSet, double threashold, boolean discardWtokens){
        Map<String,Double> tokensCounter = new HashMap<>();
        Map<String,Double> winnerMatchTokensLocal = new HashMap<>();
        for (Example example : dataSet.getExamples()) {
            for (String match : example.getMatchedStrings()) {
                List<String> tokens = tokenizer.tokenize(match);
                Set<String> tokensSet = new HashSet<>(tokens);//unicity of tokens
                for(String token : tokensSet){
                    if(matchW(token) && discardWtokens){
                        continue;
                    }
                    if(tokensCounter.containsKey(token)){
                        Double value = tokensCounter.get(token);
                        value++;
                        tokensCounter.put(token, value);
                    } else {
                        tokensCounter.put(token, 1.0);
                    }
                }
            }
        }
        
        int numberOfMatches = dataSet.getNumberMatches();
        for (Map.Entry<String, Double> entry : tokensCounter.entrySet()) {
            String key = entry.getKey();
            Double double1 = entry.getValue();
            Double doublePercentange = (double1 * 100.0) / numberOfMatches;
            entry.setValue(doublePercentange); //update the original collection too
             if(doublePercentange >= threashold){
                winnerMatchTokensLocal.put(key,doublePercentange);
            }
        }
        return winnerMatchTokensLocal;
    }
    
    static public Map<String,Double> calculateWinnerUnmatchTokens(DataSet dataSet, double threashold, boolean discardWtokens){
        Map<String,Double> tokensCounter = new HashMap<>();
        Map<String,Double> winnerUnmatchTokensLocal = new HashMap<>();
        int numberOfPositiveExamples = 0;
        for (Example example : dataSet.getExamples()) {
            if(example.getMatch().isEmpty()){
                //only positive example are interesting
                continue;
            }
            numberOfPositiveExamples++;
            Set<String> exampleTokenSet = new HashSet<>();//unicity of tokens
                
            for (String unmatch : example.getUnmatchedStrings()) {
                List<String> tokens = tokenizer.tokenize(unmatch);
                exampleTokenSet.addAll(tokens);
            }
            
            //add one point per example
            for(String token : exampleTokenSet){
                if(matchW(token) && discardWtokens){
                    continue;
                }
                if(tokensCounter.containsKey(token)){
                    Double value = tokensCounter.get(token);
                    value++;
                    tokensCounter.put(token, value);
                } else {
                    tokensCounter.put(token, 1.0);
                }
            }    
        }
        
        for (Map.Entry<String, Double> entry : tokensCounter.entrySet()) {
            String key = entry.getKey();
            Double double1 = entry.getValue();
            Double doublePercentange = (double1 * 100.0) / numberOfPositiveExamples;
            entry.setValue(doublePercentange); //update the original collection too
             if(doublePercentange >= threashold){
                
                winnerUnmatchTokensLocal.put(key,doublePercentange);
            }
        }
        return winnerUnmatchTokensLocal;
    }
    
    /**
     * This setup also resets the NodeFactory configuration inside current configuration; 
     * There is ONE configuration per job, and the configurations have separate node factories.
     * Calling this method is useful when you have to change the NodeFactory configuration
     * (--i.e. SeparateAndConquer strategies)
     * @param context
     */
    @Override
    public void setup(Context context) {
        //creates a Job/thread local instance of NodeFactory (this decuples the terminalSet builders between Jobs)
        //IMPORTANT in case of striped dataset, the striped version is always used. This is coherent with the 
        //population builder behavior. The called method accesses the dataset.getStripedDataset(), when defined.
        
        context.getConfiguration().initNodeFactory();
        setup(context.getConfiguration(), context.getCurrentDataSet());
    }
    
}
