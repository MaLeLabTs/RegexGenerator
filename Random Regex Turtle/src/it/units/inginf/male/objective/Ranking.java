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

import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.Utils;

/**
 *
 * @author MaleLabTs
 */
public class Ranking {

    private Node tree;
    private double[] fitness;

    public Ranking(Node tree, double[] fitness) {
        this.tree = tree;
        this.fitness = fitness;
        
    }

    public double[] getFitness() {
        return fitness;
    }


    public Node getTree() {
        return tree;
    }    
    
    public String getDescription(){
        StringBuilder sb = new StringBuilder();
        this.tree.describe(sb);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Ranking other = (Ranking) obj;
        if (this.tree != other.tree && (this.tree == null || !this.tree.equals(other.tree))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.tree != null ? this.tree.hashCode() : 0);
        return hash;
    }       
    
}
