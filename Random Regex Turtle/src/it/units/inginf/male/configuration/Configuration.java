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

import it.units.inginf.male.evaluators.CachedTreeEvaluator;
import it.units.inginf.male.evaluators.TreeEvaluator;
import it.units.inginf.male.generations.InitialPopulationBuilder;
import it.units.inginf.male.generations.TokenizedContextPopulationBuilder;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.objective.PrecisionCharmaskLengthObjective;
import it.units.inginf.male.postprocessing.BasicPostprocessor;
import it.units.inginf.male.postprocessing.Postprocessor;
import it.units.inginf.male.selections.best.BasicLearningBestSelector;
import it.units.inginf.male.selections.best.BestSelector;
import it.units.inginf.male.strategy.ExecutionStrategy;
import it.units.inginf.male.strategy.impl.CombinedMultithreadStrategy;
import it.units.inginf.male.strategy.impl.MultithreadStrategy;
import it.units.inginf.male.terminalsets.TerminalSetBuilder;
import it.units.inginf.male.terminalsets.TokenizedContextTerminalSetBuilder;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Leaf;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.NodeFactory;
import it.units.inginf.male.tree.RegexRange;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MaleLabTs
 */
public class Configuration {

    private static final Logger LOG = Logger.getLogger(Configuration.class.getName());    
  
    /**
     * Initializes with default values, parameters and operators.
     */
    public Configuration() {
        this.evolutionParameters = new EvolutionParameters();
        this.evolutionParameters.setGenerations(1000);
        this.evolutionParameters.setPopulationSize(500);
        
        
        this.initialSeed = 0;
        this.jobId = 0;
        this.jobs = 4;
        this.objective = new PrecisionCharmaskLengthObjective() ;
        
        this.constants = Arrays.asList("\\d",
            "\\w",
            "\\.",":",",",";",
            "_","=","\"","'",
            "\\\\",
            "/",    
            "\\?","\\!",
            "\\}","\\{","\\(","\\)","\\[","\\]","<",">",
            "@","#"," "," ");
        this.ranges = new LinkedList<>();
        this.operators = new ArrayList<>(Arrays.asList("it.units.inginf.male.tree.operator.Group",
            "it.units.inginf.male.tree.operator.NonCapturingGroup",
            "it.units.inginf.male.tree.operator.ListMatch",
            "it.units.inginf.male.tree.operator.ListNotMatch",
            "it.units.inginf.male.tree.operator.MatchOneOrMore",
            "it.units.inginf.male.tree.operator.MatchZeroOrMore",
            "it.units.inginf.male.tree.operator.MatchZeroOrOne",
            "it.units.inginf.male.tree.operator.MatchMinMax"));
        //Add context wise operators (lookaround)
        this.operators.addAll(
                    Arrays.asList("it.units.inginf.male.tree.operator.PositiveLookbehind","it.units.inginf.male.tree.operator.NegativeLookbehind",
                            "it.units.inginf.male.tree.operator.PositiveLookahead", "it.units.inginf.male.tree.operator.NegativeLookahead"));
        
      
        this.initNodeFactory(); //initNodeFactory also instantiate the NodeFactory object, this decouples the terminalset between threads
        List<Leaf> terminalSet = this.nodeFactory.getTerminalSet();
        //Add default ranges
        terminalSet.add(new RegexRange("A-Z"));
        terminalSet.add(new RegexRange("a-z"));
        terminalSet.add(new RegexRange("A-Za-z"));
        
        this.evaluator = new CachedTreeEvaluator();
        this.evaluator.setup(Collections.EMPTY_MAP);
        
        this.outputFolderName = ".";
        
        this.strategyParameters = new HashMap<>();
        this.strategyParameters.put("runStrategy","it.units.inginf.male.strategy.impl.SeparateAndConquerStrategy");
        
        this.strategyParameters.put("runStrategy2","it.units.inginf.male.strategy.impl.DiversityElitarismStrategy");
        
        this.strategyParameters.put("objective2","it.units.inginf.male.objective.CharmaskMatchLengthObjective");
        
        
        this.strategyParameters.put("threads","2");
        this.strategy = new CombinedMultithreadStrategy(); //MultithreadStrategy();
        
        //TerminalSet and population builder setup is performed later
        this.terminalSetBuilderParameters = new HashMap<>();
        this.terminalSetBuilderParameters.put("tokenThreashold","80.0");
        this.terminalSetBuilder = new TokenizedContextTerminalSetBuilder();
        
        this.populationBuilderParameters = new HashMap<>();
        this.populationBuilderParameters.put("tokenThreashold","80.0");     
        this.populationBuilder = new TokenizedContextPopulationBuilder();
        
        this.postprocessorParameters = new HashMap<>();
        this.postprocessor = new BasicPostprocessor();
        this.postprocessor.setup(Collections.EMPTY_MAP);
         
        this.bestSelectorParameters = new HashMap<>();
        this.bestSelector = new BasicLearningBestSelector();
        this.bestSelector.setup(Collections.EMPTY_MAP);      
    }      
    
    /**
     * Updates dataset and datasetCotainer stats and structures, and initializes terminalSetBuilder and populationBuilder.
     * You should invoke this method when the original Dataset/DatasetContainer is modified.
     */
    public void setup(){ 
        this.datasetContainer.update();
        this.terminalSetBuilder.setup(this);
        this.populationBuilder.setup(this); 
    }

    public Configuration(Configuration cc) {
        this.evolutionParameters = cc.getEvolutionParameters();
        this.initialSeed = cc.getInitialSeed();
        this.jobId = cc.getJobId();
        this.jobs = cc.getJobs();
        this.objective = cc.getObjective();
        this.evaluator = cc.getEvaluator();
        this.outputFolder = cc.getOutputFolder();
        this.outputFolderName = cc.getOutputFolderName();
        this.strategy = cc.getStrategy();
        this.strategyParameters = new LinkedHashMap<>(cc.getStrategyParameters()); //Permits runtime modification of Configuration objects (do not affect other configurations); Used in combined strategy
        this.configName = cc.getConfigName();
        this.populationBuilder = cc.getPopulationBuilder();
        this.terminalSetBuilderParameters = cc.getTerminalSetBuilderParameters();
        this.terminalSetBuilder = cc.getTerminalSetBuilder();
        this.populationBuilderParameters = cc.getPopulationBuilderParameters();
        this.datasetContainer = cc.getDatasetContainer();
        this.postprocessor = cc.getPostProcessor();
        this.postprocessorParameters = cc.getPostprocessorParameters();
        //nodeFactory is not dublicated 
        this.bestSelector = cc.getBestSelector();
        this.bestSelectorParameters = cc.getBestSelectorParameters();
        this.constants = cc.constants;
        this.ranges = cc.ranges;
        this.operators = cc.operators;
        this.isFlagging = cc.isIsFlagging();
        this.initNodeFactory(); //initialized nodeFacory after introducing constants and operators
    }
    
    
    private EvolutionParameters evolutionParameters;
    private long initialSeed;
    private int jobs;
    private int jobId;
    private transient File outputFolder;
    private String outputFolderName;
    private transient Objective objective;
    private transient TreeEvaluator evaluator;
    private transient ExecutionStrategy strategy;    
    private Map<String, String> strategyParameters;  
    private String configName;
    private transient NodeFactory nodeFactory;
    private transient InitialPopulationBuilder populationBuilder;
    private Map<String, String> populationBuilderParameters;
    private Map<String, String> terminalSetBuilderParameters;
    private transient TerminalSetBuilder terminalSetBuilder;
    private DatasetContainer datasetContainer;
    private transient Postprocessor postprocessor;
    private Map<String, String> postprocessorParameters;
    private List<String> constants;
    private List<String> ranges;
    private List<String> operators;
    private transient BestSelector bestSelector;
    private Map<String, String> bestSelectorParameters;
    private boolean  isFlagging = false;

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public void setNodeFactory(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }
    
    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public EvolutionParameters getEvolutionParameters() {
        return evolutionParameters;
    }

    public void setEvolutionParameters(EvolutionParameters evolutionParameters) {
        this.evolutionParameters = evolutionParameters;
    }

    public long getInitialSeed() {
        return initialSeed;
    }

    public void setInitialSeed(long initialSeed) {
        this.initialSeed = initialSeed;
    }

    public Map<String, String> getPostprocessorParameters() {
        return postprocessorParameters;
    }

    public void setPostprocessorParameters(Map<String, String> postprocessorParameters) {
        this.postprocessorParameters = postprocessorParameters;
    }

    public Map<String, String> getTerminalSetBuilderParameters() {
        return terminalSetBuilderParameters;
    }

    public void setTerminalSetBuilderParameters(Map<String, String> terminalSetBuilderParameters) {
        this.terminalSetBuilderParameters = terminalSetBuilderParameters;
    }

    public TerminalSetBuilder getTerminalSetBuilder() {
        return terminalSetBuilder;
    }

    public void setTerminalSetBuilder(TerminalSetBuilder terminalSetBuilder) {
        this.terminalSetBuilder = terminalSetBuilder;
    }

    public List<String> getConstants() {
        return constants;
    }

    public void setConstants(List<String> constants) {
        this.constants = constants;
    }

    public List<String> getRanges() {
        return ranges;
    }

    public void setRanges(List<String> ranges) {
        this.ranges = ranges;
    }

    public List<String> getOperators() {
        return operators;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    /**
     * Returns a clone of the current objective, the strategies should get the objective once and cache the instance.
     * There should be and instance per strategy (and one instance per job).
     * Calling the objective a lot of times is going to instantiate a lot of instances. 
     * @return
     */
    public Objective getObjective() {
        return objective.cloneObjective();
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    public TreeEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(TreeEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public int getJobs() {
        return jobs;
    }

    public void setJobs(int jobs) {
        this.jobs = jobs;
    }

    public ExecutionStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ExecutionStrategy strategy) {
        this.strategy = strategy;
    }

    public Map<String, String> getStrategyParameters() {
        return strategyParameters;
    }

    public void setStrategyParameters(Map<String, String> strategyParameters) {
        this.strategyParameters = strategyParameters;
    }

    public InitialPopulationBuilder getPopulationBuilder() {
        return populationBuilder;
    }

    public void setPopulationBuilder(InitialPopulationBuilder populationBuilder) {
        this.populationBuilder = populationBuilder;
    }

    public Postprocessor getPostProcessor() {
        return postprocessor;
    }

    public void setPostProcessor(Postprocessor postprocessor) {
        this.postprocessor = postprocessor;
    }   
    
    public BestSelector getBestSelector() {
        return bestSelector;
    }

    public void setBestSelector(BestSelector bestSelector) {
        this.bestSelector = bestSelector;
        this.bestSelector.setup(Collections.EMPTY_MAP);
    }
    
    public Map<String, String> getBestSelectorParameters() {
        return bestSelectorParameters;
    }

    public boolean isIsFlagging() {
        return isFlagging;
    }

    public void setIsFlagging(boolean isFlagging) {
        this.isFlagging = isFlagging;
    }

    public void setBestSelectorParameters(Map<String, String> bestSelectorParameters) {
        this.bestSelectorParameters = bestSelectorParameters;
    }

    public String getOutputFolderName() {
        return outputFolderName;
    }

    public void setOutputFolderName(String outputFolderName) {
        this.outputFolderName = outputFolderName;
        this.outputFolder = new File(this.outputFolderName);
        checkOutputFolder(this.outputFolder);
    }
   
    private void checkOutputFolder(File outputFolder) throws ConfigurationException {
        if (outputFolder == null) {
            throw new IllegalArgumentException("The output folder must be set");
        }
        if (!outputFolder.isDirectory()) {
            if (!outputFolder.mkdirs()) {
                throw new ConfigurationException("Unable to create output folder \""+outputFolder+"\"");
            }
        }
    }   
    
    public DatasetContainer getDatasetContainer() {
        return datasetContainer;
    }

    /**
     * Sets the new datasetContainer.
     * you need to call the the setup command, in order to initialize the datasetContainer and 
     * in order to generate terminalSets and initial populations,
     * @param datasetContainer
     */
    public void setDatasetContainer(DatasetContainer datasetContainer) {
        this.datasetContainer = datasetContainer;
    }
    
    public Map<String, String> getPopulationBuilderParameters() {
        return populationBuilderParameters;
    }    
    
    public final void initNodeFactory() {
        NodeFactory factory = new NodeFactory();
        List<Leaf> terminals = factory.getTerminalSet();

        for (String c : constants) {            
            terminals.add(new Constant(c));
        }

        for (String s : ranges) {
            terminals.add(new RegexRange(s));
        }

        List<Node> functions = factory.getFunctionSet();
        for (String o : operators) {
            try {
                functions.add(buildOperatorInstance(o));
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                LOG.log(Level.SEVERE, "Unable to create required operator: " + o, ex);
                System.exit(1);
            }
        }
        this.nodeFactory = factory;
    }
    
    private Node buildOperatorInstance(String o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<? extends Node> operatorClass = Class.forName(o).asSubclass(Node.class);
        Node operator = operatorClass.newInstance();
        return operator;
    }
    
    /**
     * Changes the configured fitness with the java class objectiveClass
     * @param objectiveClass
     * @return
     */
    public void updateObjective(String objectiveClass) {
        try {
            Class<? extends Objective> operatorClass = Class.forName(objectiveClass).asSubclass(Objective.class);
            Objective operator = operatorClass.newInstance();
            this.objective = operator;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, "Unable to create required objective: " + objectiveClass, ex);
            System.exit(1);
        }
        //NO OP  
    }
}
