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
package it.units.inginf.male.console;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.units.inginf.male.Main;
import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.dto.SimpleConfig;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.inputs.DataSet.Example;
import it.units.inginf.male.outputs.FinalSolution;
import it.units.inginf.male.outputs.Results;
import it.units.inginf.male.postprocessing.BasicPostprocessor;
import it.units.inginf.male.postprocessing.JsonPostProcessor;
import it.units.inginf.male.strategy.ExecutionStrategy;
import it.units.inginf.male.strategy.impl.CoolTextualExecutionListener;
import it.units.inginf.male.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a commandline tool for the GP Engine, RandomRegexTurtle.
 *
 * @author MaleLabTs
 */
public class ConsoleRegexTurtle {

    private static String WARNING_MESSAGE = "\nWARNING\n"
            + "The quality of the solution depends on a number of factors, including size and syntactical properties of the learning information.\n"
            + "The algorithms embedded in this experimental prototype have always been tested with at least 25 matches over at least 2 examples.\n"
            + "It is very unlikely that a smaller number of matches allows obtaining a useful solution.\n";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SimpleConfig simpleConfiguration = new SimpleConfig();

        //Set defaults for commandline parameters
        simpleConfiguration.datasetName = "./dataset.json"; // -d
        simpleConfiguration.outputFolder = "."; // -o
        //load simpleconfig defaults
        simpleConfiguration.numberOfJobs = 32; // -j
        simpleConfiguration.generations = 1000; // -g
        simpleConfiguration.numberThreads = 4; // -t
        simpleConfiguration.populationSize = 500; //-p
        simpleConfiguration.termination = 20; //-e
        simpleConfiguration.populateOptionalFields = false;
        simpleConfiguration.isStriped = false;

        parseArgs(args, simpleConfiguration);

        try {
            simpleConfiguration.dataset = loadDataset(simpleConfiguration.datasetName);
        } catch (IOException ex) {
            System.out.println("Problem opening the dataset file " + simpleConfiguration.datasetName + "\n");
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        //Output warning about learning size
        String message = null;
        int numberPositiveExamples = 0;
        for (Example example : simpleConfiguration.dataset.getExamples()) {
            if (example.getNumberMatches() > 0) {
                numberPositiveExamples++;
            }
        }
        if (simpleConfiguration.dataset.getNumberMatches() < 25 || numberPositiveExamples < 2) {
            message = WARNING_MESSAGE;
        }
        Configuration config = simpleConfiguration.buildConfiguration();
        //change defaults for console usage
        config.setPostProcessor(new JsonPostProcessor());
        config.getPostprocessorParameters().put(BasicPostprocessor.PARAMETER_NAME_POPULATE_OPTIONAL_FIELDS, Boolean.toString(simpleConfiguration.populateOptionalFields));
        config.setOutputFolderName(simpleConfiguration.outputFolder);

        Results results = new Results(config);
        results.setComment(simpleConfiguration.comment);
        try {
            //This is an optional information
            results.setMachineHardwareSpecifications(Utils.cpuInfo());
        } catch (IOException ex) {
            Logger.getLogger(ConsoleRegexTurtle.class.getName()).log(Level.SEVERE, null, ex);
        }
        CoolTextualExecutionListener consolelistener = new CoolTextualExecutionListener(message, config, results);

        long startTime = System.currentTimeMillis();
        ExecutionStrategy strategy = config.getStrategy();
        try {
            strategy.execute(config, consolelistener);
        } catch (Exception ex) {
            Logger.getLogger(ConsoleRegexTurtle.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (config.getPostProcessor() != null) {
            startTime = System.currentTimeMillis() - startTime;
            config.getPostProcessor().elaborate(config, results, startTime);
        }
        writeBestPerformances(results.getBestSolution(), config.isIsFlagging());
    }

    private static DataSet loadDataset(String dataSetFilename) throws IOException {
        FileInputStream fis = new FileInputStream(new File(dataSetFilename));
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
        return loadDatasetJson(json);
    }

    private static DataSet loadDatasetJson(String jsonDataset) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        DataSet dataset = gson.fromJson(jsonDataset, DataSet.class);
        return dataset;
    }

    private static void writeBestPerformances(FinalSolution solution, boolean isFlagging) {
        if (solution != null) {
            System.out.println("Best on learning (JAVA): " + solution.getSolution());
            System.out.println("Best on learning (JS): " + solution.getSolutionJS());
            if (!isFlagging) {
                System.out.println("******Stats for Extraction task******");
                System.out.println("******Stats on training******");
                System.out.println("F-measure: " + solution.getTrainingPerformances().get("match f-measure"));
                System.out.println("Precision: " + solution.getTrainingPerformances().get("match precision"));
                System.out.println("Recall: " + solution.getTrainingPerformances().get("match recall"));
                System.out.println("Char precision: " + solution.getTrainingPerformances().get("character precision"));
                System.out.println("Char recall: " + solution.getTrainingPerformances().get("character recall"));
                System.out.println("******Stats on validation******");
                System.out.println("F-measure " + solution.getValidationPerformances().get("match f-measure"));
                System.out.println("Precision: " + solution.getValidationPerformances().get("match precision"));
                System.out.println("Recall: " + solution.getValidationPerformances().get("match recall"));
                System.out.println("Char precision: " + solution.getValidationPerformances().get("character precision"));
                System.out.println("Char recall: " + solution.getValidationPerformances().get("character recall"));
                System.out.println("******Stats on learning******");
                System.out.println("F-measure: " + solution.getLearningPerformances().get("match f-measure"));
                System.out.println("Precision: " + solution.getLearningPerformances().get("match precision"));
                System.out.println("Recall: " + solution.getLearningPerformances().get("match recall"));
                System.out.println("Char precision: " + solution.getLearningPerformances().get("character precision"));
                System.out.println("Char recall: " + solution.getLearningPerformances().get("character recall"));
            } else {
                System.out.println("******Stats for Flagging task******");
                System.out.println("******Stats on training******");
                System.out.println("Accuracy: " + solution.getTrainingPerformances().get("flag accuracy"));
                System.out.println("Fpr: " + solution.getTrainingPerformances().get("flag fpr"));
                System.out.println("Fnr: " + solution.getTrainingPerformances().get("flag fnr"));
                System.out.println("F-measure: " + solution.getTrainingPerformances().get("flag f-measure"));
                System.out.println("Precision: " + solution.getTrainingPerformances().get("flag precision"));
                System.out.println("Recall: " + solution.getTrainingPerformances().get("flag recall"));
                System.out.println("******Stats on validation******");
                System.out.println("Accuracy: " + solution.getValidationPerformances().get("flag accuracy"));
                System.out.println("Fpr: " + solution.getValidationPerformances().get("flag fpr"));
                System.out.println("Fnr: " + solution.getValidationPerformances().get("flag fnr"));
                System.out.println("F-measure " + solution.getValidationPerformances().get("flag f-measure"));
                System.out.println("Precision: " + solution.getValidationPerformances().get("flag precision"));
                System.out.println("Recall: " + solution.getValidationPerformances().get("flag recall"));
                System.out.println("******Stats on learning******");
                System.out.println("Accuracy: " + solution.getLearningPerformances().get("flag accuracy"));
                System.out.println("Fpr: " + solution.getLearningPerformances().get("flag fpr"));
                System.out.println("Fnr: " + solution.getLearningPerformances().get("flag fnr"));
                System.out.println("F-measure: " + solution.getLearningPerformances().get("flag f-measure"));
                System.out.println("Precision: " + solution.getLearningPerformances().get("flag precision"));
                System.out.println("Recall: " + solution.getLearningPerformances().get("flag recall"));
            }
        }
    }

    static private final String HELP_MESSAGE
            = "Usage:\n"
            + "java -jar ConsoleRegexTurtle -t 4 -p 500 -g 1000 -e 20.0 -c \"interesting evolution\" -x true -d dataset.json -o ./outputfolder/\n"
            + "\nOn linux you can invoke this tool using the alternative script:\n"
            + "regexturtle.sh -t 4 -p 500 -g 1000 -e 20.0 -c \"interesting evolution\" -d dataset.json -o ./outputfolder/\n"
            + "\nParameters:\n"
            + "-t number of threads, default is 2\n"
            + "-p population size, default is 500\n"
            + "-g maximum number of generations, per Job, default si 1000\n"
            + "-j number of Jobs, default si 32\n"
            + "-e percentange of number generations, defines a threshold for the separate and conquer split criteria, when best doesn't change for the provided % of generation the Job evolution separates the dataset.\n"
            + "   Default is 20%, 200 geberations with default 1000 generations.\n"
            + "-d path of the dataset json file containing the examples, this parameter is mandatory.\n"
            + "-o name of the output folder, results.json is saved into this folder; default is '.'\n"
            + "-x boolean, populates an extra field in results file, when 'true' adds all dataset examples in the results file 'examples' field, default is 'false'\n"
            + "-s boolean, when 'true' enables dataset striping, striping is an experimental feature, default is disabled: 'false'\n"
            + "-c adds an optional comment string\n"
            + "-f enables the flagging mode: solves a flagging problem with a separate-and-conquer strategy\n"
            + "-h visualizes this help message\n";

    static private void parseArgs(String[] args, SimpleConfig simpleConfig) {
        try {
            boolean mandatoryDatasetCheck = true;
            if (args.length == 0) {
                System.out.println(HELP_MESSAGE);
            }
            for (int i = 0; i < args.length; i++) {
                String string = args[i];
                i = i + 1;
                String parameter = args[i];
                switch (string) {
                    case "-t":
                        simpleConfig.numberThreads = Integer.valueOf(parameter);
                        break;
                    case "-p":
                        simpleConfig.populationSize = Integer.valueOf(parameter);
                        break;
                    case "-d":
                        simpleConfig.datasetName = parameter;
                        mandatoryDatasetCheck = false;
                        break;
                    case "-o":
                        simpleConfig.outputFolder = parameter;
                        break;
                    case "-g":
                        simpleConfig.generations = Integer.valueOf(parameter);
                        break;
                    case "-j":
                        simpleConfig.numberOfJobs = Integer.valueOf(parameter);
                        break;
                    case "-e":
                        simpleConfig.termination = Double.valueOf(parameter);
                        break;
                    case "-x":
                        simpleConfig.populateOptionalFields = Boolean.valueOf(parameter);
                        break;
                    case "-h":
                        System.out.println(HELP_MESSAGE);
                        break;
                    case "-c":
                        simpleConfig.comment = parameter;
                        break;
                    case "-s":
                        simpleConfig.isStriped = Boolean.valueOf(parameter);
                        break;
                    case "-f":
                        simpleConfig.isFlagging = true;
                        i=i-1; //Do not use parameter
                        break;
                }
            }

            if (simpleConfig.isStriped && simpleConfig.isFlagging) {
                System.out.println("Striping and flagging cannot be enabled toghether.\n" + HELP_MESSAGE);
                System.exit(1);
            }

            if (mandatoryDatasetCheck) {
                System.out.println("Dataset path is needed.\n" + HELP_MESSAGE);
                System.exit(1);
            }
        } catch (RuntimeException ex) {
            System.out.println("Problem parsing commandline parameters.\n" + HELP_MESSAGE);
            System.out.println("Error details:" + ex.toString());
            System.exit(1);
        }

    }

}
