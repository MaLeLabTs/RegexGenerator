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
package it.units.inginf.male.objective;

import it.units.inginf.male.utils.BasicStats;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.evaluators.TreeEvaluator;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.inputs.DataSet.Bounds;
import it.units.inginf.male.inputs.DataSet.Example;
import it.units.inginf.male.tree.Node;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Flagging objective, accuracy, precision, length
 *
 * @author MaleLabTs
 */
public class FlaggingAccuracyPrecisionLengthObjective implements Objective {

    private Context context;
    
    @Override
    public void setup(Context context) {
        this.context = context;


    }

    @Override
    public double[] fitness(Node individual) {
         
        DataSet dataSetView = this.context.getCurrentDataSet();
        TreeEvaluator evaluator = context.getConfiguration().getEvaluator();
        double[] fitness = new double[3];

        double fitnessLenght;

        List<List<Bounds>> evaluate;
        try {
            evaluate = evaluator.evaluate(individual, context);
            StringBuilder builder = new StringBuilder();
            individual.describe(builder);
            fitnessLenght = builder.length();
        } catch (TreeEvaluationException ex) {
            Logger.getLogger(FlaggingAccuracyPrecisionLengthObjective.class.getName()).log(Level.SEVERE, null, ex);
            Arrays.fill(fitness, Double.POSITIVE_INFINITY);
            return fitness;
        }

        //true positive and false positive matches

       BasicStats statsOverall = new BasicStats();

        for (int i = 0; i < evaluate.size(); i++) {
            List<Bounds> result = evaluate.get(i);
            BasicStats stats = new BasicStats();
            
            Example example = dataSetView.getExample(i);
            
            if (this.isUnannotated(example)){
                continue;
            }
            
            // TODO check example integrity: an example can NOT have both matches and unmatches
            
            stats.tp = isTruePositive(result, example.match) ? 1 : 0;
            stats.fp = isFalsePositive(result, example.unmatch) ? 1 : 0;
            stats.fn = isFalseNegative(result, example.match) ? 1 : 0;
            stats.tn = isTrueNegative(result, example.unmatch) ? 1 : 0;
            
            statsOverall.add(stats);
        }

        fitness[0] = 1 - statsOverall.accuracy();
        fitness[1] = 1 - statsOverall.precision();
        fitness[2] = fitnessLenght;

        return fitness;
    }

    public static boolean isUnannotated(Example ex){
        return ex.match.isEmpty() && ex.unmatch.isEmpty();
    }
     
    public static boolean isTruePositive(List<Bounds> individualMatches, List<Bounds> expectedMatches){
        return !individualMatches.isEmpty() && !expectedMatches.isEmpty();
    }

    public static boolean isFalsePositive(List<Bounds> individualMatches, List<Bounds> expectedUnmatches){
        return !individualMatches.isEmpty() && !expectedUnmatches.isEmpty();
    }
    
    public static boolean isFalseNegative(List<Bounds> individualMatches, List<Bounds> expectedMatches){
        return individualMatches.isEmpty() && !expectedMatches.isEmpty();
    }
    
    public static boolean isTrueNegative(List<Bounds> individualMatches, List<Bounds> expectedUnmatches){
        return individualMatches.isEmpty() && !expectedUnmatches.isEmpty();
    }
    
    @Override
    public TreeEvaluator getTreeEvaluator() {
        return context.getConfiguration().getEvaluator();
    }

    @Override
    public Objective cloneObjective() {
        return new FlaggingAccuracyPrecisionLengthObjective();
    }
}
