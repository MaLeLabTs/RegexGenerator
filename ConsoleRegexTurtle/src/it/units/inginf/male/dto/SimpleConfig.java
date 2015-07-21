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
package it.units.inginf.male.dto;

import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.configuration.DatasetContainer;
import it.units.inginf.male.generations.EmptyPopulationBuilder;
import it.units.inginf.male.generations.FlaggingNaivePopulationBuilder;
import it.units.inginf.male.generations.TokenizedPopulationBuilder;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.objective.FlaggingAccuracyPrecisionLengthObjective;
import it.units.inginf.male.selections.best.BasicFlaggingLearningBestSelector;
import it.units.inginf.male.strategy.impl.MultithreadStrategy;
import it.units.inginf.male.terminalsets.FlaggingNgramsTerminalSetBuilder;
import it.units.inginf.male.terminalsets.TokenizedTerminalSetBuilder;
import java.util.Arrays;
import java.util.logging.Logger;


/**
 *
 * @author MaleLabTs
 */
public class SimpleConfig {
    //Maximum unmatch_chars/match_chars ratio
    //and sets the maximum unmatch_chars/match_chars ratio; this value defines the margin size around the matches 
    transient private final double STRIPING_DEFAULT_MARGIN_SIZE = 10;
    public int numberThreads;
    public int numberOfJobs;
    public int generations;
    public int populationSize;
    public DataSet dataset;
    public boolean populateOptionalFields;
    public boolean isStriped = false;
    public boolean isFlagging = false;
    
    transient public String datasetName;
    transient public String outputFolder;

    /**
     * Percentange [0,100] of the number of the generations used for the Spared termination
     * criteria. 
     */
    public double termination = 20.0;
    public String comment;
    
    public Configuration buildConfiguration(){
        assert !(isFlagging&&isStriped);
        
        //
        Configuration configuration = new Configuration();
        configuration.setConfigName("Console config");
        configuration.getEvolutionParameters().setGenerations(generations);
        configuration.getEvolutionParameters().setPopulationSize(populationSize);
        configuration.setJobs(numberOfJobs);
        configuration.getStrategyParameters().put(MultithreadStrategy.THREADS_KEY, String.valueOf(numberThreads));
        
        int terminationGenerations = (int)(termination * configuration.getEvolutionParameters().getGenerations() / 100.0);
        if(termination==100.0){
            configuration.getStrategyParameters().put("terminationCriteria","false");  
        } else {
            configuration.getStrategyParameters().put("terminationCriteria","true");
        }
        configuration.getStrategyParameters().put("terminationCriteriaGenerations", String.valueOf(terminationGenerations));
        //Added terminationCriteria for the second strategy
        configuration.getStrategyParameters().put("terminationCriteria2","false");
        
        if(dataset == null){
            throw new IllegalArgumentException("You must define a dataset");
        }
        dataset.populateUnmatchesFromMatches();
        DatasetContainer datasetContainer = new DatasetContainer(dataset);
        datasetContainer.createDefaultRanges((int) configuration.getInitialSeed());
        //checks if striping is needed
        dataset.updateStats();
        if(isStriped){
            Logger.getLogger(this.getClass().getName()).info("Enabled striping.");
            datasetContainer.setDataSetsStriped(true);
            datasetContainer.setDatasetStripeMarginSize(STRIPING_DEFAULT_MARGIN_SIZE);
            datasetContainer.setProposedNormalDatasetInterval(100);//terminationGenerations+50);
        }
        configuration.setDatasetContainer(datasetContainer); //remind that after setting the DataSetContainer.. we need to update configuration in order to invoke datacontainer update methods
        
        //FLagging configuration
        //is an alternative configuration, experimental, that requires changes into the configuration defaults (extractor configuration)
        //Changes: bestSelector, fitness, terminalset builder configuration mod, population builders(?)
        configuration.setIsFlagging(isFlagging);
        if(this.isFlagging){
            configuration.setStrategy(new MultithreadStrategy());
            configuration.setBestSelector(new BasicFlaggingLearningBestSelector());
            configuration.setObjective(new FlaggingAccuracyPrecisionLengthObjective());
            configuration.setPopulationBuilder(new FlaggingNaivePopulationBuilder()); //disable context generation
            configuration.setTerminalSetBuilder(new FlaggingNgramsTerminalSetBuilder()); //disable context generation 
            //TODO change terminalSet to a more naive version?
            configuration.getTerminalSetBuilderParameters().put("discardWtokens", "false");//Takes significant chars too
            configuration.getStrategyParameters().put("isFlagging", "true"); //Enable strategy flagging
            //Remove lookarounds
            configuration.getOperators().removeAll(
                    Arrays.asList("it.units.inginf.male.tree.operator.PositiveLookbehind","it.units.inginf.male.tree.operator.NegativeLookbehind",
                            "it.units.inginf.male.tree.operator.PositiveLookahead", "it.units.inginf.male.tree.operator.NegativeLookahead"));
        }
        
        
        
        configuration.setup(); //initializes datasetcontainer, populationbuilder and terminalsetbuilder
        
        return configuration;
    }
}
