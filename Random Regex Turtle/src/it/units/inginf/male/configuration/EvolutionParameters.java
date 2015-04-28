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

/**
 *
 * @author MaleLabTs
 */
public class EvolutionParameters {

    private int elitarism = 1;
    private int maxCreationDepth = 7;
    private float rootCrossoverSelectionProbability = 0;
    private float leafCrossoverSelectionProbability = 0.1f;
    private float nodeCrossoverSelectionProbability = 0.9f;
    private int generations;
    private int populationSize;
    private int maxDepthAfterCrossover = 15;
    private float mutationPobability = 0.1f;
    private float crossoverProbability = 0.8f;

    public int getElitarism() {
        return elitarism;
    }

    public void setElitarism(int elitarism) {
        this.elitarism = elitarism;
    }

    public int getGenerations() {
        return generations;
    }

    public void setGenerations(int generations) {
        this.generations = generations;
    }

    public float getLeafCrossoverSelectionProbability() {
        return leafCrossoverSelectionProbability;
    }

    public void setLeafCrossoverSelectionProbability(float leafCrossoverSelectionProbability) {
        this.leafCrossoverSelectionProbability = leafCrossoverSelectionProbability;
    }

    public int getCreationMaxDepth() {
        return maxCreationDepth;
    }

    public void setMaxCreationDepth(int maxCreationDepth) {
        this.maxCreationDepth = maxCreationDepth;
    }

    public float getNodeCrossoverSelectionProbability() {
        return nodeCrossoverSelectionProbability;
    }

    public void setNodeCrossoverSelectionProbability(float nodeCrossoverSelectionProbability) {
        this.nodeCrossoverSelectionProbability = nodeCrossoverSelectionProbability;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public float getRootCrossoverSelectionProbability() {
        return rootCrossoverSelectionProbability;
    }

    public void setRootCrossoverSelectionProbability(float rootCrossoverSelectionProbability) {
        this.rootCrossoverSelectionProbability = rootCrossoverSelectionProbability;
    }

    public float getCrossoverProbability() {
        return crossoverProbability;
    }

    public void setCrossoverProbability(float crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
    }

    public int getMaxDepthAfterCrossover() {
        return maxDepthAfterCrossover;
    }

    public void setMaxDepthAfterCrossover(int maxDepthAfterCrossover) {
        this.maxDepthAfterCrossover = maxDepthAfterCrossover;
    }

    public float getMutationPobability() {
        return mutationPobability;
    }

    public void setMutationPobability(float mutationPobability) {
        this.mutationPobability = mutationPobability;
    }
    

    
}
