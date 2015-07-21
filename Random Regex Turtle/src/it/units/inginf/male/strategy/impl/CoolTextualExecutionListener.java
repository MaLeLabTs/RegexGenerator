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
import it.units.inginf.male.evaluators.CachedEvaluator;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.objective.performance.PerformacesObjective;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.objective.performance.PerformancesFactory;
import it.units.inginf.male.outputs.FinalSolution;
import it.units.inginf.male.outputs.JobEvolutionTrace;
import it.units.inginf.male.outputs.Results;
import it.units.inginf.male.outputs.Solution;
import it.units.inginf.male.strategy.ExecutionListener;
import it.units.inginf.male.strategy.ExecutionListenerFactory;
import it.units.inginf.male.strategy.ExecutionStrategy;
import it.units.inginf.male.strategy.RunStrategy;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.Utils;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A textual interface that works only on Unix systems. Uses the ANSI escape
 * sequence 0x1B+"[2J" to clear the screen. Handy for experiments that take a
 * long time.
 *
 * @author MaleLabTs
 */
public class CoolTextualExecutionListener implements ExecutionListener, ExecutionListenerFactory {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private final static Logger LOG = Logger.getLogger(DefaultExecutionListener.class.getName());
    private final Map<Integer, String> screen = new TreeMap<>();
    private final Map<Integer, Long> jobStartTimes = new ConcurrentHashMap<>();
    private final NavigableSet<Integer> remove = new TreeSet<>();
    private final String header;
    private int jobDone = 0;
    private int jobTotal = 0;
    private int overallDone = 0;
    private int overallTotal = 0;
    private final long startTime = System.currentTimeMillis();
    private String eta;
    private FinalSolution best = null;
    private final Results results;
    private boolean isEvaluatorCached = false;
    private final boolean isFlagging;

    public CoolTextualExecutionListener(String message, Configuration configuration, Results results) {
        this.header = ((message!=null)? message.concat("\n") : "") +"Output folder: " + configuration.getOutputFolder().getName();
        this.jobTotal = configuration.getJobs();
        this.overallTotal = configuration.getEvolutionParameters().getGenerations() * jobTotal;
        this.results = results;
        if (configuration.getEvaluator() instanceof CachedEvaluator) {
            this.isEvaluatorCached = true;
        }
        this.isFlagging = configuration.isIsFlagging();
    }

    private synchronized void print() {
        char esc = 27;
        String clear = esc + "[2J";
        System.out.print(clear);

        int doneAll = 20 * overallDone / overallTotal;
        double percAll = Math.round(1000 * overallDone / (double) overallTotal) / 10.0;

        System.out.println(header);
        if (isEvaluatorCached) {
            CachedEvaluator evaluator = (CachedEvaluator) this.results.getConfiguration().getEvaluator();
            System.out.printf("[%s] %.2f%%  | %d/%d | ETA: %s | CR: %.2f\n", progress(doneAll), percAll, jobDone, jobTotal, eta, evaluator.getRatio());
        } else {
            System.out.printf("[%s] %.2f%%  | %d/%d | ETA: %s\n", progress(doneAll), percAll, jobDone, jobTotal, eta);
        }
        for (Integer jobId : screen.keySet()) {
            String color = "";
            if (remove.contains(jobId)) {
                color = ANSI_GREEN;
            }
            System.out.println(color + screen.get(jobId) + ANSI_RESET);
        }

        System.out.println("Best: " + ANSI_GREEN + printRegex(best.getSolution()) + ANSI_RESET);

    }

    @Override
    public void evolutionStarted(RunStrategy strategy) {
        int jobId = strategy.getConfiguration().getJobId();
        String print = "[                     ] 0% Gen --> 0 job: " + jobId;

        synchronized (screen) {
            screen.put(jobId, print);
        }

        this.jobStartTimes.put(jobId, System.currentTimeMillis());
    }

    @Override
    public void logGeneration(RunStrategy strategy, int generation, Node best, double[] fitness, List<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();
        int done = 20 * generation / strategy.getConfiguration().getEvolutionParameters().getGenerations();
        double perc = Math.round(1000 * generation / (double) strategy.getConfiguration().getEvolutionParameters().getGenerations()) / 10f;

        overallDone++;

        long timeTakenPerGen = (System.currentTimeMillis() - startTime) / overallDone;
        long elapsedMillis = (overallTotal - overallDone) * timeTakenPerGen;

        eta = String.format("%d h, %d m, %d s",
                TimeUnit.MILLISECONDS.toHours(elapsedMillis),
                TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMillis)),
                TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)));


        //let's store the current generatin best(fitness) individual performances on validation. remind performances indexes != fintesses 
        Ranking bestRanking = new Ranking(best, fitness);
        FinalSolution generationBestSolution = new FinalSolution(bestRanking);
         
 
        //Only  the learning performance i needed by the checkBestCandidate
        Objective learningObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.LEARNING, strategy.getConfiguration());
        double[] learningPerformance = learningObjective.fitness(population.get(0).getTree());
        PerformacesObjective.populatePerformancesMap(learningPerformance, generationBestSolution.getLearningPerformances(), isFlagging);
        //update best for visualization sake; uses the same algorithm as in BasicExecutionListener
        this.updateBest(generationBestSolution);
        
        

        String print = String.format("[%s] %.2f%% g: %d j: %d f: %s d: %.2f%% ", progress(done), perc, generation, jobId, printArray(fitness), Utils.diversity(population));
        synchronized (screen) {
            screen.put(jobId, print);
            print();
        }
        
        
        results.addCharachterEvaluated(strategy.getContext().getCurrentDataSet().getNumberOfChars() * population.size());
    }

    @Override
    public void evolutionComplete(RunStrategy strategy, int generation, List<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();
        long executionTime = System.currentTimeMillis() - this.jobStartTimes.remove(jobId);

        //Strategies can stop first than the maximum number of generations; we consider jumped generations like succesfully executed(useful for ETA):
        int jumpedGenerations = strategy.getConfiguration().getEvolutionParameters().getGenerations() - generation;
        overallDone+=jumpedGenerations;
        
        synchronized (screen) {
            remove.add(jobId);

            if (screen.size() > 10) {
                screen.remove(remove.pollFirst());
            }
        }

        jobDone++;

        if (jobDone >= strategy.getConfiguration().getJobs()) {
            print();
        }
        JobEvolutionTrace jobTrace = this.results.getJobTrace(jobId);
        jobTrace.setExecutionTime(executionTime);
        /*
         Populate Job final population with FinalSolution(s). The final population has the same order as fitness ranking but can contain fitness and performance info
         The performance are propulated here:
         */
        Objective trainingObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.TRAINING, strategy.getConfiguration());
        Objective validationObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.VALIDATION, strategy.getConfiguration());
        Objective learningObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.LEARNING, strategy.getConfiguration());
        for (int i = 0; i < population.size(); i++) {
            Ranking individual = population.get(i);
            FinalSolution finalSolution = new FinalSolution(individual);
            //performance is calculated only for first individual
            if(i==0){
                double[] trainingPerformace = trainingObjective.fitness(individual.getTree());
                double[] validationPerformance = validationObjective.fitness(individual.getTree());
                double[] learningPerformance = learningObjective.fitness(individual.getTree());
                PerformacesObjective.populatePerformancesMap(trainingPerformace, finalSolution.getTrainingPerformances(), isFlagging);
                PerformacesObjective.populatePerformancesMap(validationPerformance, finalSolution.getValidationPerformances(), isFlagging);
                PerformacesObjective.populatePerformancesMap(learningPerformance, finalSolution.getLearningPerformances(), isFlagging);
            }
            jobTrace.getFinalGeneration().add(finalSolution);
        }
    }

    @Override
    public void evolutionFailed(RunStrategy strategy, TreeEvaluationException cause) {
        int jobId = strategy.getConfiguration().getJobId();
        try {
            
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private String progress(int done) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < done; i++) {
            builder.append("=");
        }

        if (done < 20) {
            builder.append(">");
            for (int i = 19; i > done; i--) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    @Override
    public void register(ExecutionStrategy strategy) {
        //NO OP
    }

    @Override
    public ExecutionListener getNewListener() {
        return this;
    }

    private String printRegex(String regex) {
        if (regex.length() > 65) {
            return regex.substring(0, 64) + " [..]";
        }
        return regex;
    }

    private String printArray(double[] fitness) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(Math.round(fitness[0] * 100) / 100f);
        for (int i = 1; i < fitness.length; i++) {
            sb.append(",");
            sb.append(Math.round(fitness[i] * 100) / 100f);
        }
        sb.append("]");
        return sb.toString();
    }

    
     /**
     * Checks the candidate parameter to be better than previous recorded best.
     * When the candidate is better than the best, candidate becames the new best.
     * The fitness arrays are compared in this way:
     * integer i=0
     * when f(i) > g(i) fitness g is better than fitness f
     * when f(i) < g(i) fitness f is better than fitness g
     * when f(i) = g(i) we have to raise the i; i=i+1 and check again
     * @param candidate
     */
    synchronized public void updateBest(FinalSolution candidate){
        if(this.best == null){
            this.best = candidate;
            return;
        }
        int index = 0;
        for(double value : this.best.getFitness()){
            if(value > candidate.getFitness()[index]){
                this.best = candidate;
                return;
            }
            if(value < candidate.getFitness()[index]){
                return;
            }
            //values are equal, go ahead with the next one
            index++;
        }
    }
   
    @Override
    public void evolutionStopped() {
        //let's do nothing, there is no status here
    }
}
