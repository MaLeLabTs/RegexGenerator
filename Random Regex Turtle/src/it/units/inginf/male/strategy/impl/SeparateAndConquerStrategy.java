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
package it.units.inginf.male.strategy.impl;

import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.generations.Generation;
import it.units.inginf.male.generations.InitialPopulationBuilder;
import it.units.inginf.male.generations.Ramped;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.objective.performance.PerformacesObjective;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.operator.Or;
import it.units.inginf.male.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Optional accepted parameters: "terminationCriteria", Boolean, then True the
 * termination criteria is always enabled
 * "terminationCriteriaGenerations", Integer, number of generations for the
 * termination criteria.Default value: 200
 * "convertToUnmatch", boolean, when true extracted matches are converted to unmatches
 * "isFlagging", boolean, when true the evolution is a flagging problem; default is false (text extraction) 
 * when dividing the dataset. When false the extracted matches are converted to
 * unannotated ranges when dividing the dataset.
 * @author MaleLabTs
 */
public class SeparateAndConquerStrategy extends DiversityElitarismStrategy{

    private boolean convertToUnmatch = true;
    private boolean isFlagging = false;
    private double dividePrecisionThreashold =1.0;
    
    @Override
    protected void readParameters(Configuration configuration) {
        super.readParameters(configuration); 
        Map<String, String> parameters = configuration.getStrategyParameters();
        if (parameters != null) {
            if (parameters.containsKey("convertToUnmatch")) {
                convertToUnmatch = Boolean.valueOf(parameters.get("convertToUnmatch"));
            }
            if (parameters.containsKey("isFlagging")) {
                isFlagging = Boolean.valueOf(parameters.get("isFlagging"));
            }
            if (parameters.containsKey("dividePrecisionThreashold")) {
                dividePrecisionThreashold = Double.valueOf(parameters.get("dividePrecisionThreashold"));
            }

        }
    }


    private void initialize() {
        int targetPopSize = param.getPopulationSize();
        this.rankings.clear();

        InitialPopulationBuilder populationBuilder = context.getConfiguration().getPopulationBuilder();
        this.population = populationBuilder.init(this.context);
        this.context.getConfiguration().getTerminalSetBuilder().setup(this.context);
        Generation ramped = new Ramped(this.maxDepth, this.context);
        this.population.addAll(ramped.generate(targetPopSize - population.size()));
        List<Ranking> tmp = buildRankings(population, objective);
        while (tmp.size() > 0) {
            List<Ranking> t = Utils.getFirstParetoFront(tmp);
            tmp.removeAll(t);
            sortByFirst(t);
            this.rankings.addAll(t);
        }     
    }

    @Override
    public Void call() throws TreeEvaluationException {
        try {
            int generation;
            listener.evolutionStarted(this);
            initialize();
            List<Node> bests = new LinkedList<>();
            //Variables for termination criteria
            String oldGenerationBestValue = null;
            int terminationCriteriaGenerationsCounter = 0;
            context.setSeparateAndConquerEnabled(true);
            
            for (generation = 0; generation < param.getGenerations(); generation++) {
                context.setStripedPhase(context.getDataSetContainer().isDataSetStriped() && ((generation % context.getDataSetContainer().getProposedNormalDatasetInterval()) != 0));

                evolve();
                Ranking best = rankings.get(0);

                //computes joined solution and fitenss on ALL training
                List<Node> tmpBests = new LinkedList<>(bests);
                 
                
                tmpBests.add(best.getTree());
                
                Node joinedBest = joinSolutions(tmpBests);
                context.setSeparateAndConquerEnabled(false);
                double[] fitnessOfJoined = objective.fitness(joinedBest);
                context.setSeparateAndConquerEnabled(true);
                
                
                if (listener != null) {
                    //note: the rankings contains the individuals of the current sub-evolution (on divided training)
                    //logGeneration usually takes into account best and fitness fields for stats and persistence,
                    //rankings is used for size and other minor stats.
                    listener.logGeneration(this, generation + 1, joinedBest, fitnessOfJoined, this.rankings);
                }
                boolean allPerfect = true;
                for (double fitness : this.rankings.get(0).getFitness()) {
                    if (Math.round(fitness * 10000) != 0) {
                        allPerfect = false;
                        break;
                    }
                }
                if (allPerfect) {
                    break;
                }

                Objective trainingObjective = new PerformacesObjective();
                trainingObjective.setup(context);
                double[] trainingPerformace = trainingObjective.fitness(best.getTree());
                Map<String, Double> performancesMap = new HashMap<>();
                PerformacesObjective.populatePerformancesMap(trainingPerformace, performancesMap, isFlagging);

                double pr = !isFlagging ? performancesMap.get("match precision") : performancesMap.get("flag precision");
                
                String newBestValue = best.getDescription();
                if (newBestValue.equals(oldGenerationBestValue)) {
                    terminationCriteriaGenerationsCounter++;
                } else {
                    terminationCriteriaGenerationsCounter = 0;
                }
                oldGenerationBestValue = newBestValue;

                if (terminationCriteriaGenerationsCounter >= terminationCriteriaGenerations && pr >= dividePrecisionThreashold && generation < (param.getGenerations() - 1)) {
                    terminationCriteriaGenerationsCounter = 0;
                    bests.add(rankings.get(0).getTree());
                    // remove matched matches
                    StringBuilder builder = new StringBuilder();
                    rankings.get(0).getTree().describe(builder);
                    context.getTrainingDataset().addSeparateAndConquerLevel(builder.toString(), (int) context.getSeed(), convertToUnmatch, isFlagging);

                    // check if matches still exists, when matches are zero, the new level is removed and the evolution exits.
                    if (context.getCurrentDataSet().getNumberMatches() == 0) {
                        context.getTrainingDataset().removeSeparateAndConquerLevel((int) context.getSeed());
                        break;
                    }
                    // re-initialize population
                    initialize();
                    // continue evolvution
                }

                if (Thread.interrupted()) {
                    break;
                }

            }

            if (!bests.contains(rankings.get(0).getTree())) {
                bests.add(rankings.get(0).getTree());
            }
             
             
            //THe bests list insertion code should be refined.
            if (listener != null) {
                List<Node> dividedPopulation = new ArrayList<>(population.size());
                List<Node> tmpBests = new LinkedList<>(bests);
                for (Ranking r : rankings) {
                    tmpBests.set(tmpBests.size() - 1, r.getTree());
                    dividedPopulation.add(joinSolutions(tmpBests));
                }

                //We have to evaluate the new solutions on the testing dataset
                context.setSeparateAndConquerEnabled(false);
                List<Ranking> tmp = buildRankings(dividedPopulation, objective);
                

                listener.evolutionComplete(this, generation - 1, tmp);
            }
            return null;
        } catch (Throwable x) {
            throw new TreeEvaluationException("Error during evaluation of a tree", x, this);
        }
    }

    /**
     * Overrides base sortByFirst and implements a lexicographic order, for fitnesses.
     * @param front
     */
    @Override
    protected void sortByFirst(List<Ranking> front) {
        Collections.sort(front, new Comparator<Ranking>() {
            @Override
            public int compare(Ranking o1, Ranking o2) {
                double[] fitness1 = o1.getFitness();
                double[] fitness2 = o2.getFitness();
                int compare = 0;
                for (int i = 0; i < fitness1.length; i++) {
                    compare = Double.compare(fitness1[i], fitness2[i]);
                    if (compare != 0) {
                        return compare;
                    }
                }
                return -o1.getDescription().compareTo(o2.getDescription());
            }
        });
    }

    private Node joinSolutions(List<Node> bests) {
        Deque<Node> nodes = new LinkedList<>(bests);
        Deque<Node> tmp = new LinkedList<>();
        while (nodes.size() > 1) {

            while (nodes.size() > 0) {
                Node first = nodes.pollFirst();
                Node second = nodes.pollFirst();

                if (second != null) {
                    Node or = new Or();
                    or.getChildrens().add(first);
                    or.getChildrens().add(second);
                    first.setParent(or);
                    second.setParent(or);
                    tmp.addLast(or);
                } else {
                    tmp.addLast(first);
                }
            }

            nodes = tmp;
            tmp = new LinkedList<>();

        }
        return nodes.getFirst();
    }
}
