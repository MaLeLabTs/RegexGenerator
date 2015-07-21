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
import it.units.inginf.male.evaluators.CachedTreeEvaluator;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.evaluators.TreeEvaluator;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.objective.performance.PerformacesObjective;
import it.units.inginf.male.objective.performance.PerformancesFactory;
import it.units.inginf.male.outputs.FinalSolution;
import it.units.inginf.male.outputs.JobEvolutionTrace;
import it.units.inginf.male.outputs.Results;
import it.units.inginf.male.strategy.ExecutionListener;
import it.units.inginf.male.strategy.ExecutionListenerFactory;
import it.units.inginf.male.strategy.ExecutionStrategy;
import it.units.inginf.male.strategy.RunStrategy;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.BasicStats;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Execution listener with the same workflow as CoolExecutionListener but there is no console output.
 * The status of evolution is accessible thru the getStatus() method.
 * This listener can automatically call the PostPorcessor in order to complete the elaboration.
 * It provides public methods in order to get more detailed informations about best solution, extractions,
 * and a list of statistical indexes.
 * @author MaleLabTs
 */
public class BasicExecutionListener implements ExecutionListener, ExecutionListenerFactory {

    private final static Logger LOG = Logger.getLogger(DefaultExecutionListener.class.getName());
    private final Map<Integer, Long> jobStartTimes = new ConcurrentHashMap<>();
    private final NavigableSet<Integer> remove = new TreeSet<>();
    private long startTime = System.currentTimeMillis();
    private boolean callPostProcessorAutomatically = false;
    
    private final Results results;
    private final Configuration configuration;
    private BasicExecutionStatus status = new BasicExecutionStatus();
    private final boolean isFlagging;
    
    
    public BasicExecutionListener(Configuration configuration, Results results, boolean callPostProcessorAutomatically) {
        this(configuration, results);
        this.callPostProcessorAutomatically = callPostProcessorAutomatically;
    }
    
    public BasicExecutionListener(Configuration configuration, Results results) {
        this.configuration = configuration;
        this.status.jobTotal = configuration.getJobs();
        this.status.overallGenerations = configuration.getEvolutionParameters().getGenerations() * status.jobTotal;
        this.status.isSearchRunning = true;
        this.status.hasFinalResult = false;
        this.results = results;
        this.isFlagging = configuration.isIsFlagging();
    }

    private boolean firstEvolution = true;
    @Override
    public void evolutionStarted(RunStrategy strategy) {
        int jobId = strategy.getConfiguration().getJobId();
        this.jobStartTimes.put(jobId, System.currentTimeMillis());
        if(firstEvolution){
            this.startTime = System.currentTimeMillis();
            firstEvolution = false;
        }    
    }

    
    @Override
    public void logGeneration(RunStrategy strategy, int generation, Node best, double[] fitness, List<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();
      
        this.status.overallGenerationsDone++;

        double timeTakenPerGen = (double)(System.currentTimeMillis() - startTime) / this.status.overallGenerationsDone; //changed to double in case is less than one
        long elapsedMillis = (long)((this.status.overallGenerations - this.status.overallGenerationsDone) * timeTakenPerGen);

        this.status.evolutionEta = String.format("%d h, %d m, %d s",
                TimeUnit.MILLISECONDS.toHours(elapsedMillis),
                TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMillis)),
                TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)));
        //let's store the current generatin best(fitness) individual performances on validation. remind performances indexes != fintesses 
        Ranking bestRanking = new Ranking(best, fitness);
        FinalSolution generationBestSolution = new FinalSolution(bestRanking);
      
        //The learning performance is needed by the checkBestCandidate, commented out from production code
        //Objective learningObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.LEARNING, strategy.getConfiguration());
        //double[] learningPerformance = learningObjective.fitness(population.get(0).getTree());
        //PerformacesObjective.populatePerformancesMap(learningPerformance, generationBestSolution.getLearningPerformances());
        
        Objective trainingObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.TRAINING, strategy.getConfiguration());
        double[] trainingPerformace = trainingObjective.fitness(best);
        PerformacesObjective.populatePerformancesMap(trainingPerformace, generationBestSolution.getTrainingPerformances(), isFlagging);
                       
        status.updateBest(generationBestSolution);
        
        //This code is used for history selection experiments, commented out from production code
        //results.getJobTrace(jobId).checkBestCandidateSolution(generationBestSolution); 

        results.addCharachterEvaluated(strategy.getContext().getCurrentDataSet().getNumberOfChars() * population.size());
    }

    @Override
    public void evolutionComplete(RunStrategy strategy, int generation, List<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();
        long executionTime = System.currentTimeMillis() - this.jobStartTimes.remove(jobId);
        
        //Strategies can stop first then the maximum number of generations; we consider jumped generations like succesfully executed (useful for ETA stats):
        int jumpedGenerations = strategy.getConfiguration().getEvolutionParameters().getGenerations() - generation;
        this.status.overallGenerationsDone+=jumpedGenerations;
             
        synchronized (remove) {
            remove.add(jobId);
        }

        this.status.jobDone++;

        JobEvolutionTrace jobTrace = this.results.getJobTrace(jobId);
        jobTrace.setExecutionTime(executionTime);
        
        /*
         Populate Job final population with FinalSolution(s). The final population has the same order as fitness ranking but contains fitness and performance info
         The performance are propulated here:
         */
        Objective trainingObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.TRAINING, strategy.getConfiguration());
        Objective validationObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.VALIDATION, strategy.getConfiguration());
        Objective learningObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.LEARNING, strategy.getConfiguration());
        for (int i = 0; i < population.size(); i++) {
            Ranking individual = population.get(i);
            FinalSolution finalSolution = new FinalSolution(individual);
            //This condition is used in production release only.. performance is calculated only for first individual
            if(i==0){
                double[] trainingPerformace = trainingObjective.fitness(individual.getTree());
                double[] validationPerformance = validationObjective.fitness(individual.getTree());
                double[] learningPerformance = learningObjective.fitness(individual.getTree());
                PerformacesObjective.populatePerformancesMap(trainingPerformace, finalSolution.getTrainingPerformances(),isFlagging);
                PerformacesObjective.populatePerformancesMap(validationPerformance, finalSolution.getValidationPerformances(),isFlagging);
                PerformacesObjective.populatePerformancesMap(learningPerformance, finalSolution.getLearningPerformances(),isFlagging);
            }
            jobTrace.getFinalGeneration().add(finalSolution);
        }
        
        //When jobsDone >= number of Jobs we can call the PostProcessor to elaborate all the results
        if (this.status.jobDone >= strategy.getConfiguration().getJobs()) {
            
        
            //All the jobs has finished, let's manage the postprocessing stuff
            //PostProcessor calls the BestSelector which selects the best job
            if(callPostProcessorAutomatically) {
                callPostProcessor();
            }
        }
    }

    @Override
    public void evolutionFailed(RunStrategy strategy, TreeEvaluationException cause) {
        int jobId = strategy.getConfiguration().getJobId();
        synchronized (remove) {
            remove.add(jobId);
        }
        this.status.jobDone++;
        this.status.jobFailed++;
        if (this.status.jobDone >= strategy.getConfiguration().getJobs()) {
            //All the jobs has finished, let's manage the postprocessing stuff
            if(callPostProcessorAutomatically) {
                callPostProcessor();
            }
        }
            
        LOG.log(Level.SEVERE, "Job "+jobId+" failed", cause);
        
    }

    private void callPostProcessor(){
        long elaborationTime;
        if (configuration.getPostProcessor() != null) {
            elaborationTime = System.currentTimeMillis() - startTime;
            configuration.getPostProcessor().elaborate(configuration, results, elaborationTime);
        }
        this.status.isSearchRunning = false;
        this.status.best = this.results.getBestSolution();
        this.status.hasFinalResult = true;
    }

    @Override
    public void register(ExecutionStrategy strategy) {
        //NO OP
    }

    @Override
    public ExecutionListener getNewListener() {
        return this;
    }

    public BasicExecutionStatus getStatus() {
        return status;
    }

    public Results getResults() {
        return results;
    }

    public List<List<DataSet.Bounds>> getBestEvaluations() throws TreeEvaluationException{
        TreeEvaluator treeEvaluator = this.configuration.getEvaluator();
        Node bestIndividualReplica = new Constant(this.status.best.getSolution());
        return treeEvaluator.evaluate(bestIndividualReplica, new Context(Context.EvaluationPhases.LEARNING, this.configuration));
    }
 
    //errors per example, on learning
    public List<BasicStats> getBestEvaluationStats(int startIndex, int endIndex) throws TreeEvaluationException{
        List<List<DataSet.Bounds>> bestevaluations = getBestEvaluations();//.subList(startIndex, endIndex+1);
        DataSet dataset = this.configuration.getDatasetContainer().getLearningDataset();
        List<BasicStats> statsPerExample = new LinkedList<>();
        for (int index = startIndex; index <= endIndex; index++) {
            List<DataSet.Bounds> extractionsList = bestevaluations.get(index);
            Set<DataSet.Bounds> extractionsSet = new HashSet<>(extractionsList);
            DataSet.Example example = dataset.getExample(index);
            extractionsSet.removeAll(example.getMatch()); //left only false extractions
            BasicStats exampleStats = new BasicStats();
            exampleStats.fn = -1; //unset, not interesting at the moment
            exampleStats.fp = extractionsSet.size();
            exampleStats.tp = extractionsList.size() - exampleStats.fp;
            exampleStats.tn = -1; //unset, not interesting at the moment
            statsPerExample.add(exampleStats);
        }
        return statsPerExample;
    }
    
    @Override
    public void evolutionStopped() {
        this.status.isSearchRunning = false;
    }
    
}
