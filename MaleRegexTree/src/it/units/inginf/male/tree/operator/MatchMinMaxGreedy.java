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

import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.DescriptionContext;
import it.units.inginf.male.tree.Node;

/**
 *
 * @author MaleLabTs
 */
public class MatchMinMaxGreedy extends TernaryOperator {
    static int MAX_N_GENERATION = 20;
    @Override
    protected TernaryOperator buildCopy() {
        return new MatchMinMaxGreedy();
    }

    @Override
    public void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour) {
        getFirst().describe(builder, context, flavour);
        builder.append("{");
        builder.append(Integer.parseInt(getSecond().toString()));
        builder.append(",");
        builder.append(Integer.parseInt(getThird().toString()));
        builder.append("}");
    }

    @Override
    public boolean isValid() {
        Node first = getFirst();
        boolean validFirst = first.isValid() && !(first instanceof Concatenator || first instanceof Quantifier || first instanceof MatchMinMax || first instanceof MatchMinMaxGreedy || first instanceof Lookaround);
        
        Node second = getSecond();
        Node third = getThird();

        if (third instanceof Constant && second instanceof Constant) {
            int leftValue;
            int rightValue;
            try {
                leftValue = Integer.parseInt(second.toString());
                rightValue = Integer.parseInt(third.toString());
            } catch (NumberFormatException ex) {
                return false;
            }
            if (leftValue < 0 || rightValue < 0) {
                return false;
            }
            if(leftValue>=rightValue)
                return false;
            return validFirst;
        }


        return false;
    }
}
