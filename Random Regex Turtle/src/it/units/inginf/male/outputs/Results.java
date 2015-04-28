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
package it.units.inginf.male.outputs;

import com.google.gson.Gson;
import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.inputs.DataSet.Example;
import it.units.inginf.male.utils.BasicStats;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author MaleLabTs
 */
public class Results {
    
    public Results(int numberOfJobs) {
        this.jobEvolutionTrace = new ArrayList<>(numberOfJobs);
        for (int i = 0; i < numberOfJobs; i++) {
            this.jobEvolutionTrace.add(new JobEvolutionTrace());
        }
    }
    
    public Results(Configuration configuration) {
        this(configuration.getJobs());
        this.configuration = configuration;
          
        this.datasetName = this.configuration.getDatasetContainer().getDataset().getName();
        this.methodDescription = this.configuration.getConfigName();
    }
    
    private String datasetName;
    private String methodDescription;
    private String comment;
    private Date experimentDate = new Date();
    transient private String machineHardwareSpecifications;
    private FinalSolution bestSolution;
    //extractions of best solution on the learning dataset
    private List<List<DataSet.Bounds>> bestExtractions;
    private List<List<String>> bestExtractionsStrings;
    //original dataset examples, these are optional properties
    private List<Example> examples;
    private List<BasicStats> bestExtractionsStats; //bestExtractionsStats stores for all examples tp and fp stats.
    
    private long overallExecutionTimeMillis;
    //Learning set stats, useful for Knoledge avaiable computation
    //These stats are about the learning dataset (all provided examples)
    private int numberMatches;
    private int numberUnmatches;  
    private int numberMatchedChars = 0;
    private int numberUnmatchedChars = 0;
    private int numberAnnotatedChars;
    //The characters of all Dataset
    private int numberAllChars;
    //training set informations
    private int numberTrainingMatches;
    private int numberTrainingUnmatches;  
    
    private long characterEvaluations = 0;
    transient private Configuration configuration;
    
    //This is set transient only in this developement code in order to not persist Jobs details in serialization.
    transient private List<JobEvolutionTrace> jobEvolutionTrace; 
    
    
    public JobEvolutionTrace getJobTrace(int jobID){
        return this.jobEvolutionTrace.get(jobID);
    }
    
    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getMethodDescription() {
        return methodDescription;
    }

    public void setMethodDescription(String methodDescription) {
        this.methodDescription = methodDescription;
    }

    public Date getExperimentDate() {
        return experimentDate;
    }

    public void setExperimentDate(Date experimentDate) {
        this.experimentDate = experimentDate;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getMachineHardwareSpecifications() {
        return machineHardwareSpecifications;
    }

    public void setMachineHardwareSpecifications(String machineHardwareSpecifications) {
        this.machineHardwareSpecifications = machineHardwareSpecifications;
    }

    public List<JobEvolutionTrace> getJobEvolutionTraces() {
        return jobEvolutionTrace;
    }

    public void setJobEvolutionTraces(List<JobEvolutionTrace> jobEvolutionTraces) {
        this.jobEvolutionTrace = jobEvolutionTraces;
    }

    public FinalSolution getBestSolution() {
        return bestSolution;
    }

    public void setBestSolution(FinalSolution bestSolution) {
        this.bestSolution = bestSolution;
    }

    public long getOverallExecutionTimeMillis() {
        return overallExecutionTimeMillis;
    }

    public void setOverallExecutionTimeMillis(long overallExecutionTimeMillis) {
        this.overallExecutionTimeMillis = overallExecutionTimeMillis;
    }

    public int getNumberMatches() {
        return numberMatches;
    }

    public void setNumberMatches(int numberMatches) {
        this.numberMatches = numberMatches;
    }

    public int getNumberUnmatches() {
        return numberUnmatches;
    }

    public void setNumberUnmatches(int numberUnatches) {
        this.numberUnmatches = numberUnatches;
    }

    public long getCharacterEvaluations() {
        return characterEvaluations;
    }
    
    public void addCharachterEvaluated(long numChars){
        this.characterEvaluations += numChars;
    }   

    public int getNumberMatchedChars() {
        return numberMatchedChars;
    }

    public void setNumberMatchedChars(int numberMatchedChars) {
        this.numberMatchedChars = numberMatchedChars;
        this.numberAnnotatedChars = this.numberMatchedChars + this.numberUnmatchedChars;
    }

    public int getNumberUnmatchedChars() {
        return numberUnmatchedChars;
    }

    public void setNumberUnmatchedChars(int numberUnmatchedChars) {
        this.numberUnmatchedChars = numberUnmatchedChars;
        this.numberAnnotatedChars = this.numberMatchedChars + this.numberUnmatchedChars;
    }

    public int getNumberAllChars() {
        return numberAllChars;
    }

    public void setNumberAllChars(int numberAllChars) {
        this.numberAllChars = numberAllChars;
    }

    public int getNumberAnnotatedChars() {
        return numberAnnotatedChars;
    }

    public int getNumberTrainingMatches() {
        return numberTrainingMatches;
    }

    public void setNumberTrainingMatches(int numberTrainingMatches) {
        this.numberTrainingMatches = numberTrainingMatches;
    }

    public int getNumberTrainingUnmatches() {
        return numberTrainingUnmatches;
    }

    public void setNumberTrainingUnmatches(int numberTrainingUnmatches) {
        this.numberTrainingUnmatches = numberTrainingUnmatches;
    }

    public List<List<DataSet.Bounds>> getBestExtractions() {
        return bestExtractions;
    }

    public void setBestExtractions(List<List<DataSet.Bounds>> bestExtractions) {
        this.bestExtractions = bestExtractions;
    }

    public List<List<String>> getBestExtractionsStrings() {
        return bestExtractionsStrings;
    }

    public void setBestExtractionsStrings(List<List<String>> bestExtractionsStrings) {
        this.bestExtractionsStrings = bestExtractionsStrings;
    }

    public List<Example> getExamples() {
        return examples;
    }

    public void setExamples(List<Example> examples) {
        this.examples = examples;
    }

    public List<BasicStats> getBestExtractionsStats() {
        return bestExtractionsStats;
    }

    public void setBestExtractionsStats(List<BasicStats> bestStats) {
        this.bestExtractionsStats = bestStats;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    
    public static Results load(String fileName) throws IOException{
        FileInputStream fis = new FileInputStream(new File(fileName));
        InputStreamReader isr = new InputStreamReader(fis);
        StringBuilder sb;
        try (BufferedReader bufferedReader = new BufferedReader(isr)) {
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        }
        String json = sb.toString();
        Gson gson = new Gson();
        Results result = gson.fromJson(json, Results.class);
        return result;
    }
    
}
