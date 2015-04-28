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

import it.units.inginf.male.objective.Ranking;

/**
 *
 * @author MaleLabTs
 */
public class Solution {

    public Solution() {
    }
    
    public Solution(Ranking individual){
        StringBuilder builder = new StringBuilder();
        individual.getTree().describe(builder);
        this.solution = builder.toString();
        this.fitness = individual.getFitness();
    }

    public Solution(String solution) {
        this.solution = solution;
    }

    public Solution(String solution, double[] fitness) {
        this.solution = solution;
        this.fitness = fitness;
    }
    
    
    private String solution;
    private double[] fitness;
    
    public String getSolution() {
        return solution;
    }

    public void setSolution(String bestSolution) {
        this.solution = bestSolution;
    }

    public double[] getFitness() {
        return fitness;
    }

    public void setFitness(double[] fitness) {
        this.fitness = fitness;
    }
    
    
}
