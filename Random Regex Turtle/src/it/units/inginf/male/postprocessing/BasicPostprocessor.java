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
package it.units.inginf.male.postprocessing;

import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.evaluators.TreeEvaluator;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.inputs.DataSet.Bounds;
import it.units.inginf.male.inputs.DataSet.Example;
import it.units.inginf.male.outputs.Results;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.BasicStats;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a simplified postprocessor which implements only the basic workflow, i.e. solution selection
 * This class is not intended to provide any output (BasicPostprocessor is not going to write to console 
 * or saving JSON files)
 * @author MaleLabTs
 */
public class BasicPostprocessor implements Postprocessor {

    private static Logger LOG = Logger.getLogger(BasicPostprocessor.class.getName());
    private boolean populateOptionalFields = true;
    static public final String PARAMETER_NAME_POPULATE_OPTIONAL_FIELDS = "populateOptionalFields";
   
    @Override
    public void setup(Map<String, String> parameters) {
    }

    @Override
    public void elaborate(Configuration config, Results results, long timeTaken) {
        //The PostProcessor gets the parameters in real time
        Map<String, String> parameters = config.getPostprocessorParameters();
        if(parameters!=null){
            if(parameters.containsKey(PARAMETER_NAME_POPULATE_OPTIONAL_FIELDS)){
                this.populateOptionalFields = Boolean.valueOf(parameters.get(PARAMETER_NAME_POPULATE_OPTIONAL_FIELDS));
            }
        }
          
        //"Start evaluating results..."
        
        //crunches the results file and find out the best individual
        config.getBestSelector().elaborate(results);
        results.setOverallExecutionTimeMillis(timeTaken);
        //Populate optional fields
        if(populateOptionalFields){
            results.setExamples(config.getDatasetContainer().getDataset().getExamples());
        }
        try {
            //Populate extractions data and stats
            results.setBestExtractions(this.getEvaluations(results.getBestSolution().getSolution(), config, Context.EvaluationPhases.LEARNING));
            results.setBestExtractionsStrings(this.getEvaluationsStrings(results.getBestExtractions(),config.getDatasetContainer().getLearningDataset()));
            results.setBestExtractionsStats(this.getEvaluationStats(results.getBestExtractions(), config));
        } catch (TreeEvaluationException ex) {
            Logger.getLogger(BasicPostprocessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //Populate knowledge avaiable
        DataSet training = config.getDatasetContainer().getTrainingDataset();
        DataSet learning = config.getDatasetContainer().getLearningDataset();
        //training stats
        int numberTrainingMatches = training.getNumberMatches();
        int numberTrainingUnmatches = training.getNumberUnmatches();
        results.setNumberTrainingMatches(numberTrainingMatches);
        results.setNumberTrainingUnmatches(numberTrainingUnmatches);
        //Here is the KA from definitions
        int numberMatches = learning.getNumberMatches();
        int numberUnmatches = learning.getNumberUnmatches();
        int numberMatchedChars = learning.getNumberMatchedChars();
        int numberUnmatchedChars = learning.getNumberUnmatchedChars();
        int numberAllChars = config.getDatasetContainer().getDataset().getNumberOfChars();
        results.setNumberMatches(numberMatches);
        results.setNumberUnmatches(numberUnmatches);
        results.setNumberMatchedChars(numberMatchedChars);
        results.setNumberUnmatchedChars(numberUnmatchedChars);
        results.setNumberAllChars(numberAllChars);
        
    }
    
    private List<List<DataSet.Bounds>> getEvaluations(String solution, Configuration configuration, Context.EvaluationPhases phase) throws TreeEvaluationException{
        TreeEvaluator treeEvaluator = configuration.getEvaluator();
        Node bestIndividualReplica = new Constant(solution);
        return treeEvaluator.evaluate(bestIndividualReplica, new Context(phase, configuration));
    }
    
    private List<List<String>> getEvaluationsStrings(List<List<Bounds>> extractions, DataSet dataset){
        List<List<String>> evaluationsStrings = new LinkedList<>();
        Iterator<Example> it = dataset.getExamples().iterator();
        for (List<Bounds> extractionsOfExample : extractions) {
            Example example = it.next();
            List<String> extractionsOfExampleStrings = new LinkedList<>();
            for (Bounds bounds : extractionsOfExample) {
                extractionsOfExampleStrings.add(example.getString().substring(bounds.start,bounds.end));
            }
            evaluationsStrings.add(extractionsOfExampleStrings);
        }
        return evaluationsStrings;
    }

    
    //errors per example, on learning; 
    private List<BasicStats> getEvaluationStats(List<List<DataSet.Bounds>> evaluation, Configuration config) throws TreeEvaluationException{
        DataSet dataset = config.getDatasetContainer().getLearningDataset();
        List<BasicStats> statsPerExample = new LinkedList<>();
        for (int index = 0; index < dataset.getExamples().size(); index++) {
            List<DataSet.Bounds> extractionsList = evaluation.get(index);
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
}
