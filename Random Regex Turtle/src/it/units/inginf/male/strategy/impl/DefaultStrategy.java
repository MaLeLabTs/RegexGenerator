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
import it.units.inginf.male.configuration.EvolutionParameters;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.generations.Generation;
import it.units.inginf.male.generations.InitialPopulationBuilder;
import it.units.inginf.male.generations.Ramped;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.selections.Selection;
import it.units.inginf.male.selections.Tournament;
import it.units.inginf.male.strategy.ExecutionListener;
import it.units.inginf.male.strategy.RunStrategy;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.Pair;
import it.units.inginf.male.utils.Utils;
import it.units.inginf.male.variations.Variation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implements the default evolution strategy, termination criteria can be enabled thru parameters.  
 * Optional accepted parameters:
 * "terminationCriteria", Boolean, then True the termination criteria is enabled when false is disabled, Default value: false
 * "terminationCriteriaGenerations", Integer, number of generations for the termination criteria.Default value: 200   
 * @author MaleLabTs
 */
public class DefaultStrategy implements RunStrategy {

    protected Context context;
    protected int maxDepth;
    protected List<Node> population;
    protected List<Ranking> rankings = new LinkedList<>();
    protected Selection selection;
    protected Objective objective;
    protected Variation variation;
    protected EvolutionParameters param;
    protected ExecutionListener listener;
    protected boolean terminationCriteria = false; //Termination criteria enables/disables the premature termination of thread when best regex/individual doens't change for
                                                   //a speciefied amount of generations (terminationCriteriaGenerations)
    protected int terminationCriteriaGenerations = 200;

    
    
    @Override
    public void setup(Configuration configuration, ExecutionListener listener) throws TreeEvaluationException {
        this.param = configuration.getEvolutionParameters();
        
        this.readParameters(configuration);
        
        this.context = new Context(Context.EvaluationPhases.TRAINING, configuration);
        this.maxDepth = param.getCreationMaxDepth();
        //cloning the objective 
        this.objective = configuration.getObjective();
        this.selection = new Tournament(this.context);
        this.variation = new Variation(this.context);
        this.listener = listener;

        this.objective.setup(context);
    }

    protected void readParameters(Configuration configuration){
        Map<String, String> parameters = configuration.getStrategyParameters();
        if (parameters != null) {
            //add parameters if needed
            if (parameters.containsKey("terminationCriteriaGenerations")) {
                terminationCriteriaGenerations = Integer.valueOf(parameters.get("terminationCriteriaGenerations"));
            }
            if (parameters.containsKey("terminationCriteria")) {
                terminationCriteria = Boolean.valueOf(parameters.get("terminationCriteria"));
            }
        }
    }

    @Override
    public Void call() throws TreeEvaluationException {
        try {
            int generation;
            listener.evolutionStarted(this);
            InitialPopulationBuilder populationBuilder = context.getConfiguration().getPopulationBuilder();
            this.population = populationBuilder.init();
            Generation ramped = new Ramped(this.maxDepth, this.context);
            this.population.addAll(ramped.generate(param.getPopulationSize() - population.size()));
            List<Ranking> tmp = buildRankings(population, objective);
            while (tmp.size() > 0) {
                List<Ranking> t = Utils.getFirstParetoFront(tmp);
                tmp.removeAll(t);
                sortByFirst(t);
                this.rankings.addAll(t);
            }
            //Variables for termination criteria
            String oldGenerationBestValue = null;
            int terminationCriteriaGenerationsCounter = 0;
            int doneGenerations = 0;
            for (generation = 0; generation < param.getGenerations(); generation++) {
                context.setStripedPhase(context.getDataSetContainer().isDataSetStriped() && ((generation % context.getDataSetContainer().getProposedNormalDatasetInterval()) != 0));

                evolve();
                Ranking best = rankings.get(0);
                doneGenerations = generation + 1;
                if (listener != null) {
                    listener.logGeneration(this, doneGenerations, best.getTree(), best.getFitness(), this.rankings);
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
                
                if(terminationCriteria){
                    String newBestValue = best.getDescription();
                    if(newBestValue.equals(oldGenerationBestValue)){
                        terminationCriteriaGenerationsCounter++;
                    } else {
                        terminationCriteriaGenerationsCounter = 0;
                    }
                    if(terminationCriteriaGenerationsCounter >= this.terminationCriteriaGenerations) {
                        break;
                    }
                    oldGenerationBestValue = newBestValue;
                }
                
                if (Thread.interrupted()) {
                    break;
                }

            }

            //now generation value is already last generation + 1, no reason to add +1
            if (listener != null) {
                listener.evolutionComplete(this, doneGenerations, this.rankings);
            }
            return null;
        } catch (Throwable x) {
            throw new TreeEvaluationException("Error during evaluation of a tree", x, this);
        }
    }

    protected void evolve() {

        List<Node> newPopulation = new ArrayList<>(population.size());
        int popSize = population.size();
        int oldPopSize = (int) (popSize * 0.9);

        boolean allPerfect = true;
        for (double fitness : rankings.get(0).getFitness()) {
            if (Math.round(fitness * 10000) != 0) {
                allPerfect = false;
                break;
            }
        }
        if (allPerfect) {
            return;
        }

        for (int index = 0; index < param.getElitarism(); index++) {
            Node elite = rankings.remove(0).getTree();
            newPopulation.add(elite);
        }

        while (newPopulation.size() < oldPopSize) {

            double random = context.getRandom().nextDouble();

            if (random <= param.getCrossoverProbability() && oldPopSize - newPopulation.size() >= 2) {
                Node selectedA = selection.select(rankings);
                Node selectedB = selection.select(rankings);

                Pair<Node, Node> newIndividuals = variation.crossover(selectedA, selectedB);
                if (newIndividuals != null) {
                    newPopulation.add(newIndividuals.getFirst());
                    newPopulation.add(newIndividuals.getSecond());
                }
            } else if (random <= param.getCrossoverProbability() + param.getMutationPobability()) {
                Node mutant = selection.select(this.rankings);
                mutant = variation.mutate(mutant);
                newPopulation.add(mutant);

            } else {
                Node duplicated = selection.select(rankings);
                newPopulation.add(duplicated);
            }
        }

        Generation ramped = new Ramped(maxDepth, context);
        List<Node> generated = ramped.generate(popSize - oldPopSize);
        newPopulation.addAll(generated);

        population = newPopulation;
        List<Ranking> tmp = buildRankings(population, objective);
        rankings.clear();
        while (tmp.size() > 0) {
            List<Ranking> t = Utils.getFirstParetoFront(tmp);
            tmp.removeAll(t);
            sortByFirst(t);
            rankings.addAll(t);
        }
    }

    protected List<Ranking> buildRankings(List<Node> population, Objective objective) {
        List<Ranking> result = new ArrayList<>(population.size());
        for (Node tree : population) {
            double fitness[] = objective.fitness(tree);
            result.add(new Ranking(tree, fitness));
        }
        return result;
    }

    @Override
    public Configuration getConfiguration() {
        return context.getConfiguration();
    }

    @Override
    public ExecutionListener getExecutionListener() {
        return listener;
    }

    protected void sortByFirst(List<Ranking> front) {
        Collections.sort(front, new Comparator<Ranking>() {
            @Override
            public int compare(Ranking o1, Ranking o2) {
                Double fitness1 = o1.getFitness()[0];
                Double fitness2 = o2.getFitness()[0];
                int result = Double.compare(fitness1, fitness2);
                if (result == 0) {
                    return o1.getDescription().compareTo(o2.getDescription());
            }
                return result;
            }
        });
    }

    @Override
    public Context getContext() {
        return this.context;
    }
}
