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
package it.units.inginf.male.selections.best;

import it.units.inginf.male.outputs.FinalSolution;
import it.units.inginf.male.outputs.JobEvolutionTrace;
import it.units.inginf.male.outputs.Results;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Picks one individual per job (the best one using the evolution fitness); then it evaluates their performance
 * on the learning set. The individual with the best performance is promoted as the best one.. 
 * 
 * @author MaleLabTs
 */
public class BasicFlaggingLearningBestSelector implements BestSelector {
    
    private static Logger LOG = Logger.getLogger(BasicFlaggingLearningBestSelector.class.getName());
   

    @Override
    public void setup(Map<String, String> parameters) {
        
    }

    @Override
    public void elaborate(Results results) {
        this.selectAndPopulateBest(results);
    }
    
    private void selectAndPopulateBest(Results results) {
        double bestIndividualIndex = Double.NEGATIVE_INFINITY;
        int bestLength = Integer.MAX_VALUE;
        FinalSolution best = null;
        for (JobEvolutionTrace jobEvolutionTrace : results.getJobEvolutionTraces()) {
            FinalSolution bestOfJob = jobEvolutionTrace.getFinalGeneration().get(0);
            double accuracy = bestOfJob.getLearningPerformances().get("flag accuracy");
            int bestJobLength = bestOfJob.getSolution().length();
            accuracy = (Double.isNaN(accuracy))?0:accuracy;
            double individualIndex = accuracy;
            if ((individualIndex > bestIndividualIndex) || ((individualIndex == bestIndividualIndex) && (bestLength > bestJobLength))) {
                    bestLength = bestJobLength;
                    best = bestOfJob;
                    bestIndividualIndex = individualIndex;
            }
            
        }
        results.setBestSolution(best);
     
    }
}
