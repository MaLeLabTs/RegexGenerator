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
package it.units.inginf.male.tree.operator;


import it.units.inginf.male.tree.AbstractNode;
import it.units.inginf.male.tree.Node;


/**
 *
 * @author MaleLabTs
 */
public abstract class UnaryOperator extends AbstractNode {

    private Node parent;

    @Override
    public int getMinChildrenCount() {
        return 1;
    }

    @Override
    public int getMaxChildrenCount() {
        return 1;
    }       

    @Override
    public Node cloneTree() {
        UnaryOperator clone = buildCopy();
        if (!getChildrens().isEmpty()) {
            Node child = getChildrens().get(0).cloneTree();
            child.setParent(clone);
            clone.getChildrens().add(child);
        }
        return clone;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }
   
    protected abstract UnaryOperator buildCopy();  

}
