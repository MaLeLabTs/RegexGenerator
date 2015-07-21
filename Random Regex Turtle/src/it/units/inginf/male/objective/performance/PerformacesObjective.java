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
package it.units.inginf.male.objective.performance;

import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.utils.BasicStats;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.evaluators.TreeEvaluator;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.inputs.DataSet.Bounds;
import it.units.inginf.male.inputs.DataSet.Example;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.outputs.FinalSolution;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is not a fitness, but returns individual performances like in a
 * mutli-objective fitness style. fitness[0] = precision fitness[1] = recall
 * fitness[2] = charPrecision fitness[3] = charRecall fitness[4] = charAccuracy
 * fitness[5] = match fmeasure
 *
 * @author MaleLabTs
 */
public class PerformacesObjective implements Objective {

    private Context context;
    //private DataSet dataSetView;

    //private int numberCharsInMatches = 0;
    @Override
    public void setup(Context context) {
        this.context = context;
        //this.dataSetView = this.context.getCurrentDataSet();
        //this.dataSetView.populateMatchesStrings();
    }

    @Override
    public double[] fitness(Node individual) {
        DataSet dataSetView = this.context.getCurrentDataSet();
        TreeEvaluator evaluator = context.getConfiguration().getEvaluator();
        double[] fitness = new double[12];
        List<List<Bounds>> evaluate;
        try {
            evaluate = evaluator.evaluate(individual, context);
        } catch (TreeEvaluationException ex) {
            Logger.getLogger(PerformacesObjective.class.getName()).log(Level.SEVERE, null, ex);
            Arrays.fill(fitness, Double.POSITIVE_INFINITY);
            return fitness;
        }

        //match stats makes sense only for tp e fp values... we cannot use instance statistic formulas other than precision
        BasicStats statsOverall = new BasicStats();

        //char stats can be managed as ususal
        BasicStats statsCharsOverall = new BasicStats();

        //Flagging
        BasicStats statsOverallFlagging = new BasicStats();

        int i = 0;
        for (List<Bounds> result : evaluate) {
            BasicStats stats = new BasicStats();
            BasicStats statsChars = new BasicStats();
            BasicStats statsFlagging = new BasicStats();

            //Characted extracted in the right place (match)
            Example example = dataSetView.getExample(i);
            List<Bounds> expectedMatchMask = example.getMatch();
            List<Bounds> expectedUnmatchMask = example.getUnmatch();
            List<Bounds> annotatedMask = new ArrayList<>(expectedMatchMask);
            annotatedMask.addAll(expectedUnmatchMask);

            stats.tp = countIdenticalRanges(result, expectedMatchMask);
            stats.fp = Bounds.countRangesThatCollideZone(result, annotatedMask) - stats.tp;
            statsChars.tp = intersection(result, expectedMatchMask);
            statsChars.fp = intersection(result, expectedUnmatchMask);

            //Flagging
            
            if (!this.isUnannotated(example)){
                statsFlagging.tp = isTruePositive(result, example.match) ? 1 : 0;
                statsFlagging.fp = isFalsePositive(result, example.unmatch) ? 1 : 0;
                statsFlagging.fn = isFalseNegative(result, example.match) ? 1 : 0;
                statsFlagging.tn = isTrueNegative(result, example.unmatch) ? 1 : 0;
                statsOverallFlagging.add(statsFlagging);
            }
            
            statsOverall.add(stats);
            statsCharsOverall.add(statsChars);
            i++;
        }

        statsCharsOverall.tn = dataSetView.getNumberUnmatchedChars() - statsCharsOverall.fp;
        statsCharsOverall.fn = dataSetView.getNumberMatchedChars() - statsCharsOverall.tp;

        double charAccuracy = statsCharsOverall.accuracy(); //Chars extraction accuracy
        double charPrecision = statsCharsOverall.precision(); //Chars precision (how many extracted chars are in positive matches?)
        double charRecall = statsCharsOverall.recall(); //Chars recall (how many extracted chars vs overall chars in positive matches)
        double precision = statsOverall.precision(); //How many extractions are correct?
        double recall = statsOverall.recall(dataSetView.getNumberMatches()); //Right extractions vs overall matches number
        double fmeasure = 2 * (precision * recall) / (precision + recall);

        fitness[0] = precision;
        fitness[1] = recall;
        fitness[2] = charPrecision;
        fitness[3] = charRecall;
        fitness[4] = charAccuracy;
        fitness[5] = fmeasure;
        //Flagging
        fitness[6] = statsOverallFlagging.accuracy();
        fitness[7] = statsOverallFlagging.fpr();
        fitness[8] = statsOverallFlagging.fnr();
        fitness[9] = statsOverallFlagging.precision();
        fitness[10] = statsOverallFlagging.recall();
        fitness[11] = statsOverallFlagging.fMeasure();

        return fitness;
    }

    //Returns number of chars of this extracted ranges which falls into expected ranges
    private int intersection(List<Bounds> extractedRanges, List<Bounds> expectedRanges) {
        int overallNumChars = 0;

        for (Bounds extractedBounds : extractedRanges) {
            for (Bounds expectedBounds : expectedRanges) {
                int numChars = Math.min(extractedBounds.end, expectedBounds.end) - Math.max(extractedBounds.start, expectedBounds.start);
                overallNumChars += Math.max(0, numChars);
            }
        }
        return overallNumChars;
    }

    //Rerurns the number of idential intervals in two list of ranges
    private int countIdenticalRanges(List<Bounds> rangesA, List<Bounds> rangesB) {
        int identicalRanges = 0;

        for (Bounds boundsA : rangesA) {
            for (Bounds boundsB : rangesB) {
                if (boundsA.equals(boundsB)) {
                    identicalRanges++;
                    break;
                }
            }
        }
        return identicalRanges;
    }

    @Override
    public TreeEvaluator getTreeEvaluator() {
        return context.getConfiguration().getEvaluator();
    }

    @Override
    public Objective cloneObjective() {
        return new PerformacesObjective();
    }

    public static void populatePerformancesMap(double[] performances, Map<String, Double> performancesMap, boolean isFlagging) {
        if(!isFlagging){
            performancesMap.put("match precision", performances[0]);
            performancesMap.put("match recall", performances[1]);
            performancesMap.put("character precision", performances[2]);
            performancesMap.put("character recall", performances[3]);
            performancesMap.put("character accuracy", performances[4]);
            performancesMap.put("match f-measure", performances[5]);
        } else {
            //flagging performances
            performancesMap.put("flag accuracy", performances[6]);
            performancesMap.put("flag fpr", performances[7]);
            performancesMap.put("flag fnr", performances[8]);
            performancesMap.put("flag precision", performances[9]);
            performancesMap.put("flag recall", performances[10]);
            performancesMap.put("flag f-measure", performances[11]);
        } 
    }

    /**
     * Populate the the performances of finalSolution for the requested phase
     * (training, validation, training)
     *
     * @param phase training, validation or learning phase
     * @param configuration the configuration instance
     * @param finalSolution
     */
    public static void populateFinalSolutionPerformances(Context.EvaluationPhases phase, Configuration configuration, FinalSolution finalSolution, boolean isFlagging) {
        Objective phaseObjective = PerformancesFactory.buildObjective(phase, configuration);
        Node finalTree = new Constant(finalSolution.getSolution());
        double[] phasePerformaceRoughtValues = phaseObjective.fitness(finalTree);
        Map<String, Double> phasePerformances = null; //let it explode in case of errors
        switch (phase) {
            case TRAINING:
                phasePerformances = finalSolution.getTrainingPerformances();
                break;
            case VALIDATION:
                phasePerformances = finalSolution.getValidationPerformances();
                break;
            case LEARNING:
                phasePerformances = finalSolution.getLearningPerformances();
                break;
        }

        PerformacesObjective.populatePerformancesMap(phasePerformaceRoughtValues, phasePerformances, isFlagging);
    }

    public static boolean isUnannotated(Example ex) {
        return ex.match.isEmpty() && ex.unmatch.isEmpty();
    }

    public static boolean isTruePositive(List<Bounds> individualMatches, List<Bounds> expectedMatches) {
        return !individualMatches.isEmpty() && !expectedMatches.isEmpty();
    }

    public static boolean isFalsePositive(List<Bounds> individualMatches, List<Bounds> expectedUnmatches) {
        return !individualMatches.isEmpty() && !expectedUnmatches.isEmpty();
    }

    public static boolean isFalseNegative(List<Bounds> individualMatches, List<Bounds> expectedMatches) {
        return individualMatches.isEmpty() && !expectedMatches.isEmpty();
    }

    public static boolean isTrueNegative(List<Bounds> individualMatches, List<Bounds> expectedUnmatches) {
        return individualMatches.isEmpty() && !expectedUnmatches.isEmpty();
    }
}
