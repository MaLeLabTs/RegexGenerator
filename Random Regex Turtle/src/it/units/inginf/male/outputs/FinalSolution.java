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
import it.units.inginf.male.tree.DescriptionContext;
import it.units.inginf.male.tree.Node;
import java.util.HashMap;
import java.util.Map;

/**
 * Extends the Solution class; adds performances on Training, Validation and Learning
 * @author MaleLabTs
 */
public class FinalSolution extends Solution{

    public FinalSolution() {
    }

    public FinalSolution(Ranking individual) {
        super(individual);
        this.solutionJS = getDescriptionJavascript(individual.getTree());
    }
   
    private String getDescriptionJavascript(Node node){
        StringBuilder sb = new StringBuilder();
        node.describe(sb, new DescriptionContext(), Node.RegexFlavour.JS);
        return sb.toString();
    }

    /**
     * Create a FinalSolution for the Java regex (String) solution parameter.
     * Javascript version is not automatically generate, this needs initialization
     * with a Node individual.
     * @param solution
     */
    public FinalSolution(String solution) {
        super(solution);
    }

    public FinalSolution(String solution, double[] fitness) {
        super(solution, fitness);
    }

    private String solutionJS;
    private Map<String,Double> trainingPerformances = new HashMap<>();
    private Map<String,Double> validationPerformances = new HashMap<>();
    private Map<String,Double> learningPerformances = new HashMap<>();

    public Map<String, Double> getTrainingPerformances() {
        return trainingPerformances;
    }

    public void setTrainingPerformances(Map<String, Double> trainingPerformances) {
        this.trainingPerformances = trainingPerformances;
    }

    public Map<String, Double> getValidationPerformances() {
        return validationPerformances;
    }

    public void setValidationPerformances(Map<String, Double> validationPerformances) {
        this.validationPerformances = validationPerformances;
    }

    public Map<String, Double> getLearningPerformances() {
        return learningPerformances;
    }

    public void setLearningPerformances(Map<String, Double> learningPerformances) {
        this.learningPerformances = learningPerformances;
    }

    public String getSolutionJS() {
        return solutionJS;
    }
     
}
