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
package it.units.inginf.male.strategy.impl;

import it.units.inginf.male.outputs.FinalSolution;

/**
 *
 * @author MaleLabTs
 */
public class BasicExecutionStatus {
    
    public boolean isSearchRunning;
    public boolean hasFinalResult;
    public String evolutionEta;
    public FinalSolution best = null;
    
    //public double bestPerformance;
    public int jobTotal = 0;
    public int jobDone = 0;
    public int jobFailed = 0;
    public int overallGenerations = 0;
    public int overallGenerationsDone = 0;
    
    /**
     * Checks the candidate parameter to be better than previous recorded best.
     * When the candidate is better than the best, candidate becames the new best.
     * The best and the candidate need a populated trainingPerformance or the update'll fail
     * 
     * The best solution is the solution with higher training f-measure, when f-measure is the same
     * the best solution is the smaller (string length wise) one.
     * @param candidate
     */
    synchronized public void updateBest(FinalSolution candidate){
        if(this.best == null){
            this.best = candidate;
            return;
        }
        //int index = 0;
        Double bestPerformance = best.getTrainingPerformances().get("match f-measure");
        Double candidatePerformance = candidate.getTrainingPerformances().get("match f-measure");
        if((candidatePerformance > bestPerformance) || 
                ((candidatePerformance.equals(bestPerformance)) && (candidate.getSolution().length()<best.getSolution().length()))){
            this.best = candidate;
        }
        
    
    }
}
