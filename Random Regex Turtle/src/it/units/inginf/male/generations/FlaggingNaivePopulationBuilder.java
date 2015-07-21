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
import it.units.inginf.male.utils.Utils;
import java.util.*;

/**
 * Create a population, 3 individuals for each positive example.
 * @author andrea + fabiano
 */
public class FlaggingNaivePopulationBuilder implements InitialPopulationBuilder {

    private List<Node> population = new LinkedList<>();
    private final boolean useDottification;
    private final boolean useWordClasses;

    /**
     * Initialises a population from examples by replacing charcters with "\\w"
     * and digits with "\\d"
     */
    public FlaggingNaivePopulationBuilder() {
        this.useDottification = true;
        this.useWordClasses = true;
    }

    /**
     * When dottification is true and useWordClasses is true, instances are
     * initialized using character classes "[A-Za-z]" "\\d" When dottification is
     * true and useWordClasses is false, instances are initialized by replacing
     * characters and digits with "."
     *
     * @param useDottification
     * @param useWordClasses
     */
    public FlaggingNaivePopulationBuilder(boolean useDottification, boolean useWordClasses) {
        this.useDottification = useDottification;
        this.useWordClasses = useWordClasses;
    }

    @Override
    public List<Node> init() {
        return new ArrayList<>(population);
    }

    @Override
    public void setup(Configuration configuration) {
        DataSet trainingDataset = configuration.getDatasetContainer().getTrainingDataset();
        this.population.addAll(this.setup(configuration, trainingDataset));
    }
    
    private List<Node> setup(Configuration configuration, DataSet usedTrainingDataset) {
        Set<String> phrases = new HashSet<>();
        List<Node> newPopulation = new LinkedList<>();
        DataSet dataSet = usedTrainingDataset;

        
        for (Example example : dataSet.getExamples()) {
            if(!example.match.isEmpty()){
                //is positive example
                phrases.add(example.getString());
            }
        }

        int examples = Math.min(configuration.getEvolutionParameters().getPopulationSize() / 3, phrases.size());

        List<String> uniquePhrases = new ArrayList<>(phrases);

        int counter = 0;
        for (String node : uniquePhrases) {
            if (this.useDottification) {
                newPopulation.add(createByExample(node, true, false));
                newPopulation.add(createByExample(node, true, true));
            }
            newPopulation.add(createByExample(node, false, false));
            counter++;
            if (counter >= examples) {
                break;
            }
        }
        return newPopulation;
    }

    private Node createByExample(String example, boolean replace, boolean compact) {
        Deque<Node> nodes = new LinkedList<>();
        Deque<Node> tmp = new LinkedList<>();

        //String w = this.useWordClasses ? "\\w" : ".";
        String d = this.useWordClasses ? "\\d" : ".";
        Node letters;
        if(useWordClasses){
            letters = new ListMatch();
            letters.getChildrens().add(new RegexRange("A-Za-z"));
        } else {
            letters = new Constant(".");
        }
        
        for (char c : example.toCharArray()) {
            if (replace) {
                if (Character.isLetter(c)) {
                    nodes.add(letters.cloneTree());
                } else if (Character.isDigit(c)) {
                    nodes.add(new Constant(d));
                } else {
                    nodes.add(new Constant(Utils.escape(c)));
                }
            } else {
                nodes.add(new Constant(Utils.escape(c)));
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
