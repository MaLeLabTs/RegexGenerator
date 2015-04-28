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
package it.units.inginf.male.generations;

import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.tree.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MaleLabTs
 */
public class Ramped implements Generation {

    Generation full;
    Generation grow;
    Context context;

    public Ramped(int maxDepth, Context context ) {
        this.full = new Full(maxDepth,context);
        this.grow = new Growth(maxDepth,context);
        this.context=context;
    }

     /**
     * This method returns a new population of the desired size. An half of the
     * population is generated through Growth algorithm, the other half by the
     * Full method
     * @param popSize the desired population size
     * @return a List of Node of size popSize
     */
    @Override
    public List<Node> generate(int popSize) {
        List<Node> population = new ArrayList<>();

        int popSizeGrow = (int)popSize/2;
        int popSizeFull = popSize-popSizeGrow;

        population.addAll(this.full.generate(popSizeGrow));
        population.addAll(this.grow.generate(popSizeFull));

        return population;
    }
}
