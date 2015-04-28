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
package it.units.inginf.male.generations;

import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.inputs.DataSet.Example;
import it.units.inginf.male.terminalsets.TokenizedContextTerminalSetBuilder;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.RegexRange;
import it.units.inginf.male.tree.operator.Concatenator;
import it.units.inginf.male.tree.operator.ListMatch;
import it.units.inginf.male.tree.operator.MatchMinMax;
import it.units.inginf.male.tree.operator.MatchOneOrMore;
import it.units.inginf.male.tree.operator.PositiveLookahead;
import it.units.inginf.male.tree.operator.PositiveLookbehind;
import it.units.inginf.male.utils.BasicTokenizer;
import it.units.inginf.male.utils.Tokenizer;
import it.units.inginf.male.utils.Utils;
import java.util.*;

/**
 * Creates a initial population from the matches and unmatches. 
 * Matches and unmatches are modified in this way:
 * Significant tokens are left unchanged, other words(chars) are changed into the corresponding
 * character class (i.e. \w \d).
 * Sequences of identical character classes or tokens  are compacted using quantifiers.
 * Accepts these configuration population builder parameters:
 * "tokenThreashold","discardWtokens","tokenUnmatchThreashold","addNoContextIndividuals"
 * @author MaleLabTs
 */
public class TokenizedContextPopulationBuilder implements InitialPopulationBuilder {

    private List<Node> population = new LinkedList<>();
    private Map<String,Double> winnerMatchTokens;
    private Map<String,Double> winnerUnMatchTokens;
    
    private Tokenizer tokenizer = new BasicTokenizer();
     
    /**
     * Initialises a population from examples by replacing charcters with "\\w"
     * and digits with "\\d"
     */
    public TokenizedContextPopulationBuilder() {
    }

    @Override
    public List<Node> init() {
        return new ArrayList<>(population);
    }
    
    @Override
    public void setup(Configuration configuration) {
        DataSet trainingDataset = configuration.getDatasetContainer().getTrainingDataset();
        this.population.addAll(this.setup(null, configuration, trainingDataset));
    }
    
    private List<Node> setup(Context context, Configuration configuration, DataSet usedTrainingDataset) {
        
        
        Double TOKEN_THREASHOLD = 80.0; 
        Double TOKEN_UNMATCH_THREASHOLD = 80.0;   
        boolean ADD_NO_CONTEXT_INDIVIDUALS = true;
        boolean DISCARD_W_TOKENS = true; //Discard all tokens who match \w
        
        //Change to striped dataset when striped version is initialized
        //IMPORTANT in case of striped dataset, the striped version is always used. This is coherent with the 
        //population builder behavior
        if (usedTrainingDataset.getStripedDataset()!=null){
            usedTrainingDataset = usedTrainingDataset.getStripedDataset();
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
            if(parameters.containsKey("addNoContextIndividuals")){
                ADD_NO_CONTEXT_INDIVIDUALS = Boolean.valueOf(parameters.get("addNoContextIndividuals"));
            }
        }
         
        List<Node> newPopulation = new LinkedList<>();
        
        //Providing the striped dataset is not an error, a striped dataset has not a striped son :-) usually; so, the provided dataset is used
        this.winnerMatchTokens = TokenizedContextTerminalSetBuilder.calculateWinnerMatchTokens(usedTrainingDataset, TOKEN_THREASHOLD, DISCARD_W_TOKENS);
        this.winnerUnMatchTokens = TokenizedContextTerminalSetBuilder.calculateWinnerUnmatchTokens(usedTrainingDataset, TOKEN_UNMATCH_THREASHOLD, DISCARD_W_TOKENS);
        
        for(Example example : usedTrainingDataset.getExamples()){
            if(example.getMatch().isEmpty()){
                //jump Negative examples
                continue;
            }
            newPopulation.addAll(createIndividualsFromExample(example, true, winnerMatchTokens,winnerUnMatchTokens));
            newPopulation.addAll(createIndividualsFromExample(example, false, winnerMatchTokens,winnerUnMatchTokens));
            
        }
        
        //add individual based on the old tokenization (match based only)
        if(ADD_NO_CONTEXT_INDIVIDUALS){
            TokenizedPopulationBuilder tokenizedPopulationBuilder = new TokenizedPopulationBuilder();
            if(context == null){
                tokenizedPopulationBuilder.setup(configuration);
                newPopulation.addAll(tokenizedPopulationBuilder.init());
            }else {
                newPopulation.addAll(tokenizedPopulationBuilder.init(context));
            }
        }
        
        int popSize = Math.min(configuration.getEvolutionParameters().getPopulationSize()/2, newPopulation.size());
        
        Collections.shuffle(newPopulation,new Random(0));
        newPopulation = new LinkedList<>(newPopulation.subList(0, popSize));
        return newPopulation;
    }
    
    private List<Node> createIndividualsFromExample(Example example,boolean compact, Map<String,Double> winnerMatchTokens, Map<String,Double> winnerUnMatchTokens){
        if(example.getNumberMatches()==0){
            return Collections.EMPTY_LIST;
        }
        List<Node> individualsForExample = new LinkedList<>();
        Set<String> matchSet = new HashSet<>(example.getMatchedStrings());
        Set<String> unmatchSet = new HashSet<>(example.getUnmatchedStrings());
        List<String> orderedAnnotatedStrings = example.getOrderedAnnotatedStrings();
         
        //Context is for now ONLY unmatched portions
        String pre=null,match=null,post=null;
        for (int i = 0; i < orderedAnnotatedStrings.size(); i++) {
            if(matchSet.contains(orderedAnnotatedStrings.get(i))){
                match =  orderedAnnotatedStrings.get(i);
                if(i>0){
                    if(unmatchSet.contains(orderedAnnotatedStrings.get(i-1))){
                        pre = orderedAnnotatedStrings.get(i-1);
                    }
                }
                if((i+1)<orderedAnnotatedStrings.size()){
                    if(unmatchSet.contains(orderedAnnotatedStrings.get(i+1))){
                        post = orderedAnnotatedStrings.get(i+1);
                    }
                }
                individualsForExample.add(createIndividualFromStrings(pre, match, post, compact, winnerMatchTokens, winnerUnMatchTokens));
            }            
        }
        return individualsForExample;
    }

    
    
    //Create individual with lookbehind and ahead, and match.
    private Node createIndividualFromStrings(String preUnmatchString, String matchString, String postUnmatchString, boolean compact, 
                                                                    Map<String,Double> winnerMatchTokens, Map<String,Double> winnerUnmatchTokens) {
        
        Node preUnmatchNode = null, matchNode = null, postUnmatchNode = null;
        
        if(preUnmatchString != null){
            List<String> preUnmatchStringTokens = tokenizer.tokenize(preUnmatchString);
            preUnmatchNode = createIndividualFromTokenizedString(preUnmatchStringTokens, winnerUnmatchTokens, compact, true);
        }
        if(matchString != null){
            List<String> matchStringTokens = tokenizer.tokenize(matchString);
            matchNode = createIndividualFromTokenizedString(matchStringTokens, winnerMatchTokens, compact, false);
        }
        if(postUnmatchString != null){
            List<String> postUnmatchStringTokens = tokenizer.tokenize(postUnmatchString);        //winner tokens are added with no modifications(only escaped), other parts are converted to classes or escaped
            postUnmatchNode = createIndividualFromTokenizedString(postUnmatchStringTokens, winnerUnmatchTokens, compact, false);
        }
        
        Node finalIndividual = matchNode;
        if(postUnmatchNode!=null){
            Node finalIndividualTemp = new Concatenator();
            finalIndividualTemp.getChildrens().add(matchNode);
            Node positiveLookAhead = new PositiveLookahead();
            positiveLookAhead.getChildrens().add(postUnmatchNode);
            finalIndividualTemp.getChildrens().add(positiveLookAhead);
            finalIndividual = finalIndividualTemp;
        }
        if(preUnmatchNode!=null){
            Node finalIndividualTemp = new Concatenator();
            Node positiveLookBehind = new PositiveLookbehind();
            positiveLookBehind.getChildrens().add(preUnmatchNode);
            finalIndividualTemp.getChildrens().add(positiveLookBehind);
            finalIndividualTemp.getChildrens().add(finalIndividual);
            finalIndividual = finalIndividualTemp;
        }
        return finalIndividual;
    }
    
    //Create a simple individual from a tokenized (splitted) version (list of strings). Only winnerTokens are preserved.
    private Node createIndividualFromTokenizedString(List<String> tokenizedString, Map<String,Double> winnerTokens, boolean compact, boolean useMinMaxQuantifier){
         
        Deque<Node> nodes = new LinkedList<>();
        Deque<Node> tmp = new LinkedList<>();

        String w = "\\w";
        String d = "\\d";
        Node letters = new ListMatch();
        letters.getChildrens().add(new RegexRange("A-Za-z"));
        
        for(String token : tokenizedString){
            if(winnerTokens.containsKey(token)){
                nodes.add(new Constant(Utils.escape(token)));
            } else {
                for (char c : token.toCharArray()) {
                    if (Character.isLetter(c)) {
                        nodes.add(letters.cloneTree());
                    } else if (Character.isDigit(c)) {
                        nodes.add(new Constant(d));
                    } else {
                        nodes.add(new Constant(Utils.escape(c)));
                    }
                }
            }
        }
        
        //when two adiacent nodes are equal, symbols or tokens, a quantifier is used to compact.
        // /w/w/w is converted to /w++
        if(compact){
            Deque<Node> newNodes = new LinkedList<>();
            String nodeValue;
            String nextValue;
            //do compact
            
            while (nodes.size()>0) {
                Node node = nodes.pollFirst();
                nodeValue = node.toString();
                boolean isRepeat = false;
                int repetitions = 1;
                while (nodes.size()>0){
                    Node next = nodes.peek();
                    nextValue = next.toString();
                     
                    if(nodeValue.equals(nextValue)){
                        repetitions++;
                        isRepeat = true;
                        //Consume and drop the repetition
                        nodes.pollFirst();
                    } else {
                        //They are different, get out
                        break;
                    } 
                }    
                if(isRepeat){
                    Node finalNode = null;
                    if(useMinMaxQuantifier){
                        finalNode = new MatchMinMax();
                        finalNode.getChildrens().add(node);
                        finalNode.getChildrens().add(new Constant("1"));
                        finalNode.getChildrens().add(new Constant(String.valueOf(repetitions)));
                    } else {
                        finalNode = new MatchOneOrMore();
                        finalNode.getChildrens().add(node);
                        
                    }
                    node = finalNode;
                }
                newNodes.add(node);                
            }
            nodes = newNodes;
        }

        //Build the concatenation of given nodes
        //This code is the same as in NaivePopulationBulder and has been "cloned" from older code
       
        while (nodes.size() > 1) {

            while (nodes.size() > 0) {
                Node first = nodes.pollFirst();
                Node second = nodes.pollFirst();

                if (second != null) {
                    Node conc = new Concatenator();
                    conc.getChildrens().add(first);
                    conc.getChildrens().add(second);
                    first.setParent(conc);
                    second.setParent(conc);
                    tmp.addLast(conc);
                } else {
                    tmp.addLast(first);
                }
            }

            nodes = tmp;
            tmp = new LinkedList<>();

        }

        return nodes.getFirst();
    }
    

    @Override
    public List<Node> init(Context context) {
        return setup(context, context.getConfiguration(), context.getCurrentDataSet());
    }
}
