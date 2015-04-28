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
package it.units.inginf.male.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.inputs.DataSet.Example;
import it.units.inginf.male.utils.Range;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author MaleLabTs
 */
public final class DatasetContainer {

    private String path;
    private transient DataSet dataset;
    private List<Range> training = new ArrayList<>();
    private List<Range> validation = new ArrayList<>();
    private boolean dataSetStriped = false;
    private double datasetStripeMarginSize = Integer.MAX_VALUE; //no slices are done
    private int normalProposedDatasetInterval = 0;
    private transient DataSet trainingDataset;
    private transient DataSet validationDataset;
    private transient DataSet learningDataset;

    public DatasetContainer() {    
    }
    
    public DatasetContainer(DatasetContainer datasetContainer) {
        this.path = datasetContainer.getPath();
        this.dataset = datasetContainer.getDataset();
        this.training = new LinkedList<>(datasetContainer.getTraining());
        this.validation = new LinkedList<>(datasetContainer.getValidation());
        this.dataSetStriped = datasetContainer.isDataSetStriped();
        this.datasetStripeMarginSize = datasetContainer.getDatasetStripeMarginSize();
        this.normalProposedDatasetInterval = datasetContainer.getProposedNormalDatasetInterval();
        this.trainingDataset = datasetContainer.getTrainingDataset();
        this.validationDataset = datasetContainer.getValidationDataset();
        this.learningDataset = datasetContainer.getLearningDataset();
    }
    
    /**
     * Initialize the dataset container for the provided dataset.
     * Ranges and sub-dataset are not initialized, you have to set the ranges and
     * call the <code>update</code> instance method in order to generate them.
     * Random generator is initialized to a default value.
     * @param dataset
     */
    public DatasetContainer(DataSet dataset){ 
        this(dataset,false, 0);
    }
    
    /**
     * Initialize the dataset container for the provided dataset.
     * When defaultRanges is true, it initializes automatically training and 
     * validation ranges; not less than 50% of matches have to stay in training, 50% in validation.
     * Random generator is initialized to a default value.
     * @param dataset
     * @param defaultRanges
     */
    public DatasetContainer(DataSet dataset, boolean defaultRanges){
        this(dataset,defaultRanges, 0);
    }
    
    /**
     * Initialize the dataset container for the provided dataset.
     * When defaultRanges is true, it initializes automatically training and 
     * validation ranges; 50% matches have to stay in training, 50% in validation.
     * @param dataset
     * @param defaultRanges when true, default ranges are computed and sub-dataset are generated
     * @param defaultRangesSeed The seed of the random generator
     */
    public DatasetContainer(DataSet dataset, boolean defaultRanges, int defaultRangesSeed){
        this();
        this.setDataset(dataset);
        this.dataset.populateAnnotatedStrings();
        this.dataset.updateStats();
        //generate Ranges
        if(defaultRanges){
            this.createDefaultRanges(defaultRangesSeed);
            this.updateSubDataset();
        }
    }
    
    /**
     * Generates the default ranges for the given dataset.
     * @param randomSeed The random seed for the internal random generator.
     */
    public void createDefaultRanges(int randomSeed){
        this.training.clear();
        this.validation.clear();
        
        Random random = new Random(randomSeed);
        int overallNumberMatchesInTraining = (int) Math.ceil(this.dataset.getNumberMatches() / 2.0);
        overallNumberMatchesInTraining = (overallNumberMatchesInTraining == 0) ? 1 : overallNumberMatchesInTraining; //not less than one example is allowed
        int matchesInTrainingCountdown = overallNumberMatchesInTraining;
        List<Integer> examplePositiveIndexes = new ArrayList<>();
        List<Integer> exampleNegativeIndexes = new ArrayList<>();
        
        for (int i = 0; i < this.dataset.getNumberExamples(); i++) {
            Example example = this.dataset.getExamples().get(i);
            if(example.getNumberMatches()>0){
                examplePositiveIndexes.add(i);
            } else {
                exampleNegativeIndexes.add(i);
            }
        }
        Collections.shuffle(examplePositiveIndexes,random);
        Collections.shuffle(exampleNegativeIndexes,random);
        
        for(Integer exampleIndex : examplePositiveIndexes){
            Example example = this.dataset.getExamples().get(exampleIndex);
            //When learning set has one element, the example goes in training; if validation is empty and the example has less matches than overallNumberMatchesInTraining, it goes in validation;
            //In case the number of matches in training are less than overallNumberMatchesInTraining and the previous conditions are false the example goes into training set.
            if(examplePositiveIndexes.size() == 1 ||  ((matchesInTrainingCountdown > 0) && !(validation.isEmpty() && example.getNumberMatches()<overallNumberMatchesInTraining))){
                //example is in training
                this.training.add(new Range(exampleIndex, exampleIndex));
                matchesInTrainingCountdown-=example.getNumberMatches();
            } else {
                //example is in validation
                this.validation.add(new Range(exampleIndex, exampleIndex));             
            }
        }
        
        for(int i = 0; i < exampleNegativeIndexes.size(); i++) {
            if(i<Math.ceil(exampleNegativeIndexes.size()/2.0)){
                this.training.add(new Range(exampleNegativeIndexes.get(i), exampleNegativeIndexes.get(i)));
            } else {
                this.validation.add(new Range(exampleNegativeIndexes.get(i), exampleNegativeIndexes.get(i)));
            }
        }
    }

    public String getPath() {
        return path;
    }

    /**
     * Set new dataset (with the given JSON file path) and automatically loads it.
     * The ranges and other DatasetContainer properties are not changed.
     *
     * @param path
     * @throws IOException
     */
    public void setPath(String path) throws IOException {
        this.path = path;
        this.loadDataset();
    }

    public List<Range> getTraining() {
        return training;
    }

    public void setTraining(List<Range> training) {
        this.training = training;
    }

    public List<Range> getValidation() {
        return validation;
    }

    public void setValidation(List<Range> validation) {
        this.validation = validation;
    }


    /**
     * When true, the training dataset includes its reduced version (striped).
     * Update and load operations also affects the linked striped views.
     *
     * @return
     */
    public boolean isDataSetStriped() {
        return dataSetStriped;
    }

    public void setDataSetsStriped(boolean datasetStriped) {
        this.dataSetStriped = datasetStriped;
    }

    /**
     * <code>normalDataInterval</code> is a proposed value for the number of
     * iterations between the usage of the normal training dataset view; this i
     * meaningful when hasStripedDataSets is true. This value does not affect
     * the DatasetContatiner behavior; The strategy is supposed to read this
     * value and change its behavior accordingly. The strategy has to inform the
     * context when the striped dataset version is needed and ALWAYS use the
     * Context.getCurrentDataset() method in order to access the right dataset
     * view.
     *
     * @return
     */
    public int getProposedNormalDatasetInterval() {
        return normalProposedDatasetInterval;
    }

    public void setProposedNormalDatasetInterval(int unstripedDatasetInterval) {
        this.normalProposedDatasetInterval = unstripedDatasetInterval;
    }

    public DataSet getDataset() {
        return dataset;
    }

    /**
     * Set the managed dataset.
     * The dataset instance is not cloned.
     * The DatasetContatiner is going to manage the provided instance.
     * @param dataset
     */
    public final void setDataset(DataSet dataset) {
        this.dataset = dataset;
    }
    
    public DataSet getTrainingDataset() {
        return trainingDataset;
    }

    public void setTrainingDataset(DataSet trainingDataset) {
        this.trainingDataset = trainingDataset;
    }

    public DataSet getValidationDataset() {
        return validationDataset;
    }

    public void setValidationDataset(DataSet validationDataset) {
        this.validationDataset = validationDataset;
    }

    public DataSet getLearningDataset() {
        return learningDataset;
    }

    public double getDatasetStripeMarginSize() {
        return datasetStripeMarginSize;
    }

    public void setDatasetStripeMarginSize(double datasetStripeMarginSize) {
        this.datasetStripeMarginSize = datasetStripeMarginSize;
    }

    /**
     * Forces reloading of the dataset from file; the dataset path URL is the
     * DatasetContainer <code>path</code> property
     *
     * @throws IOException
     */
    public void loadDataset() throws IOException {
        FileInputStream fis = new FileInputStream(new File(this.path));
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
        this.loadDatasetJson(json);
    }
    
    /**
     * Forces reloading of the dataset from JSON, updates matches strings in 
     * dataset, updates stats in dataset and sub-datasets (learning, training, validation).
     * If no ranges are defined inside the container, the sub-datasets will be empty.
     * <code>path</code> property
     *
     * @param jsonDataset
     */
    public void loadDatasetJson(String jsonDataset) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();;
        this.dataset = gson.fromJson(jsonDataset, DataSet.class);
        this.update();
    }

    public final void updateSubDataset() {
        this.trainingDataset = this.dataset.subDataset("training", training);
        this.validationDataset = this.dataset.subDataset("validation", validation);
        this.trainingDataset.updateStats();
        this.validationDataset.updateStats();
       
        this.learningDataset = new DataSet("learning");
        //the Learning set is all the dataset, there is no testing phase in this code.
        //We change the code in a way that learning set equals all dataset. 
        //This has been done in order to minimize changes and mantain the dataset examples 
        //order into the Learning collection. 
        
        this.learningDataset.getExamples().addAll(dataset.getExamples());
        this.learningDataset.updateStats();

        if (this.dataSetStriped) {
            this.trainingDataset.initStripedDatasetView(this.datasetStripeMarginSize);
            this.trainingDataset.getStripedDataset().updateStats();
        }
    }
    
    /**
     * Updates the dataset and the defined subDataset (learning,training,validation).
     * Populates the matching strings in dataset and statistical info both in dataset and
     * subDataset.
     */
    public void update(){
        this.dataset.populateAnnotatedStrings();
        this.dataset.updateStats();
        this.updateSubDataset();
    }
}
