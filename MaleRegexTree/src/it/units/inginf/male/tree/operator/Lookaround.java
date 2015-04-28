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

import it.units.inginf.male.tree.Anchor;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.RegexRange;

/**
 *
 * @author MaleLabTs
 */
public abstract class Lookaround extends UnaryOperator {

    @Override
    public boolean isValid() {
        Node child = getChildrens().get(0);
        return child.isValid() && !(child instanceof RegexRange || child instanceof Anchor || child instanceof Backreference);
    }
    private int numberQuantifier = 0;
    private boolean hasOnlyMinMax = true;

    protected void checkQuantifiers(Node root) {
        if (root instanceof Quantifier) {
            hasOnlyMinMax = false;
            numberQuantifier++;
        } else if (root instanceof MatchMinMax || root instanceof MatchMinMaxGreedy) {
            numberQuantifier++;
        }

        for (Node child : root.getChildrens()) {
            checkQuantifiers(child);
        }

    }

    protected boolean isLookbehindValid() {
        checkQuantifiers(this);
        return hasOnlyMinMax || (numberQuantifier < 1);
    }
}
