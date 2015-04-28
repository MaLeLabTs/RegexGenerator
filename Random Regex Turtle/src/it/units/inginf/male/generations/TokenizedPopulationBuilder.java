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
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.RegexRange;
import it.units.inginf.male.tree.operator.Concatenator;
import it.units.inginf.male.tree.operator.ListMatch;
import it.units.inginf.male.tree.operator.MatchOneOrMore;
import it.units.inginf.male.utils.BasicTokenizer;
import it.units.inginf.male.utils.Tokenizer;
import it.units.inginf.male.utils.Utils;
import java.util.*;

/**
 * Creates a initial population from the matches. Matches are modified in this way:
 * Significant tokens are left unchanged, other words are changed into the corresponding
 * character class (i.e. \w \d).
 * Sequences of identical classes are compacted using quantifiers.
 * @author MaleLabTs
 */
public class TokenizedPopulationBuilder implements InitialPopulationBuilder {

    private List<Node> population = new LinkedList<>();
    private Map<String,Double> winnerTokens = new HashMap<>();
    
    private Tokenizer tokenizer = new BasicTokenizer();
     
    /**
     * Initialises a population from examples by replacing charcters with "\\w"
     * and digits with "\\d"
     */
    public TokenizedPopulationBuilder() {
    }

    @Override
    public List<Node> init() {
        return new ArrayList<>(population);
    }

 //It is true when string matches \w (.i.e. its length is one and it is alphabetic or decimal number)
    private boolean matchW(String string){
        return (string.length()==1 && matchW(string.charAt(0)));
    }
    
    private boolean matchW(char character){
        return Character.isAlphabetic(character) || Character.isDigit(character) || character == '_';
    }
    
    @Override
    public void setup(Configuration configuration) {
        DataSet trainingDataset = configuration.getDatasetContainer().getTrainingDataset();
        this.population.addAll(this.setup(configuration, trainingDataset));
    }
        
    private List<Node> setup(Configuration configuration, DataSet usedTrainingDataset) {
        Map<String, Double> tokensCounter = new HashMap<>();

        List<List<String>> matchTokens = new LinkedList<>();
        List<Node> newPopulation = new LinkedList<>();
        DataSet dataSet = usedTrainingDataset;
        
        Double TOKEN_THREASHOLD = 80.0; 
        boolean DISCARD_W_TOKENS = true; //Discard all tokens who match \w
        Map<String, String> parameters = configuration.getPopulationBuilderParameters();
        if(parameters!=null){
            //add parameters if needed
            if(parameters.containsKey("tokenThreashold")){
                TOKEN_THREASHOLD = Double.valueOf(parameters.get("tokenThreashold"));
            }
            if(parameters.containsKey("discardWtokens")){
                DISCARD_W_TOKENS = Boolean.valueOf(parameters.get("discardWtokens"));
            }
        }
               
        for (Example example : dataSet.getExamples()) {
            for (String match : example.getMatchedStrings()) {
                List<String> tokens = tokenizer.tokenize(match);
                matchTokens.add(tokens);
                Set<String> tokensSet = new HashSet<>(tokens);//unicity of tokens
                for(String token : tokensSet){
                    if(matchW(token) && DISCARD_W_TOKENS){
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
             if(doublePercentange >= TOKEN_THREASHOLD){
                winnerTokens.put(key,doublePercentange);
            }
        }
        
        int popSize = configuration.getEvolutionParameters().getPopulationSize();
   
        int counter = 0;
        for (List<String> tokenizedMatch : matchTokens){
            newPopulation.add(createIndividualFromString(tokenizedMatch, true, winnerTokens));
            newPopulation.add(createIndividualFromString(tokenizedMatch, false, winnerTokens));
            counter+=2;
            if (counter >= popSize) {
                break;
            }
        }
        return newPopulation;
    }

    
    private Node createIndividualFromString(List<String> tokenizedString, boolean compact, Map<String,Double> winnerTokens) {
        Deque<Node> nodes = new LinkedList<>();
        Deque<Node> tmp = new LinkedList<>();

        String w = "\\w";
        String d = "\\d";
        Node letters = new ListMatch();
        letters.getChildrens().add(new RegexRange("A-Za-z"));
        //winner tokens are added with no modifications(only escaped), other parts are converted to classes or escaped
         
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
        
        //when two adiacent nodes are equal symbols/tokens, a quantifier is used to compact.
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
                while (nodes.size()>0){
                    Node next = nodes.peek();
                    nextValue = next.toString();
                     
                    if(nodeValue.equals(nextValue)){
                        isRepeat = true;
                        //Consume and drop the repetition
                        nodes.pollFirst();
                    } else {
                        //They are different, get out
                        break;
                    } 
                }    
                if(isRepeat){
                    Node finalNode = new MatchOneOrMore();
                    finalNode.getChildrens().add(node);
                    node = finalNode;
                }
                newNodes.add(node);                
            }
            nodes = newNodes;
        }

        //Build the concatenation of given nodes
        //This code is the same as NaivePopulationBulder and has been "cloned" from older code
       
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
        return setup(context.getConfiguration(), context.getCurrentDataSet());
    }
}
