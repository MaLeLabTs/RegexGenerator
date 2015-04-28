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
package it.units.inginf.male.evaluators;

import it.units.inginf.male.strategy.RunStrategy;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author MaleLabTs
 */
public class TreeEvaluationException extends Exception {

    private RunStrategy associatedStrategy;

    /**
     * Creates a new instance of <code>TreeEvaluationException</code> without detail message.
     */
    public TreeEvaluationException() {
    }


    /**
     * Constructs an instance of <code>TreeEvaluationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TreeEvaluationException(String msg) {
        super(msg);
    }

    public TreeEvaluationException(String message, Throwable cause, RunStrategy associatedStrategy) {
        super(message, cause);
        this.associatedStrategy = associatedStrategy;
    }

    public TreeEvaluationException(String message, RunStrategy associatedStrategy) {
        super(message);
        this.associatedStrategy = associatedStrategy;
    }

    public TreeEvaluationException(PatternSyntaxException ex) {
        super(ex);
    }

    public RunStrategy getAssociatedStrategy() {
        return associatedStrategy;
    }


}
