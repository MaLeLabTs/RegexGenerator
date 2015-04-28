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
package it.units.inginf.male.inputs;

import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.configuration.DatasetContainer;
import java.util.Random;

/**
 *
 * @author MaleLabTs
 */
public class Context {

    public enum EvaluationPhases {

        TRAINING, VALIDATION, LEARNING;
    }

    //This property is not useful in production, commented out
    //private DataSet dataSet;

    private EvaluationPhases phase;
    private boolean stripedPhase = false;
    private boolean separateAndConquerEnabled = false; 
    private Random random;
    private Configuration configuration;
    private long seed;

    public Context(EvaluationPhases phase, final Configuration configuration) {
        this.phase = phase;
        this.configuration = configuration;
        this.seed = configuration.getInitialSeed();
        this.random = new Random(this.seed);
        
    }

    /**
     * true when we are in a striped phase. During a striped phase the Context has to return
     * the striped (reduced) version of the original datasets. The reduced versions includes the
     * unmatched characters "near" the matches (in a window).  
     * @return
     */
    public boolean isStripedPhase() {
        return this.stripedPhase;
    }

    public void setStripedPhase(boolean stripedPhase) {
        this.stripedPhase = stripedPhase;
    }
    

    public Random getRandom() {
        return random;
    }

    public EvaluationPhases getPhase() {
        return phase;
    }

    public void setPhase(EvaluationPhases phase) {
        this.phase = phase;
    }
    
    public int getCurrentDataSetLength() {
        return this.getCurrentDataSet().getNumberExamples();
    }

    public long getSeed() {
        return seed;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public DatasetContainer getDataSetContainer() {
        return this.configuration.getDatasetContainer();
    }
    
    /**
     * Returns the dataset view for the current phase; the phases are TRAINING,VALIDATION
     * The returned dataset depends also by the isStripedPhase property.When true, the training set is
     * returned in a more compact version. Let's see the DataSet class for more info.
     * @return
     */
    public DataSet getCurrentDataSet(){
        switch(this.phase){
            //NOTE: only training dataset admits a striped dataset version
            case TRAINING: 
                    DataSet trainingDataset;
                    if(!this.isStripedPhase()){
                       trainingDataset = this.getDataSetContainer().getTrainingDataset();
                    }  else {
                       trainingDataset = this.getDataSetContainer().getTrainingDataset().getStripedDataset();
                    }
                    if(separateAndConquerEnabled){
                        //getSeed returns a number that in current implementation IS the JOBID
                        return trainingDataset.getLastSeparateAndConquerDataSet((int) this.getSeed());
                    } else {
                        return trainingDataset;
                    }
            case VALIDATION: return this.getDataSetContainer().getValidationDataset();
            case LEARNING: return this.getDataSetContainer().getLearningDataset();
            default : throw new UnsupportedOperationException("unhandled phase in getDataSet");
        }
        
    }

    @Override
    public String toString() {
        return this.phase.toString();
    }
    
    public boolean isSeparateAndConquerEnabled() {
        return separateAndConquerEnabled;
    }
    
    /**
     * Returns the full training dataset.(this is the original training, not the striped or other sub-datasets)
     * @return
     */
    public DataSet getTrainingDataset(){
        return configuration.getDatasetContainer().getTrainingDataset();
}
    
    /**
     * Affects only the training phase.
     * When enabled, true, the Current dataset returns the last generated "Separate and conquer" for the training.
     * When disabled, false, the normal (full) training dataset is returned. 
     * @param separateAndConquerEnabled true, enables the "Separate and conquer"
     */
    public void setSeparateAndConquerEnabled(boolean separateAndConquerEnabled) {
        this.separateAndConquerEnabled = separateAndConquerEnabled;
    }
     
}
