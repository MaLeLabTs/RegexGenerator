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
package it.units.inginf.male.selections.best;

import it.units.inginf.male.outputs.Results;
import java.util.Map;

/**
 * Its implementations describe the final post processing individual selection. The best individual selection is part of the 
 * solution search algorithm. The implementations may access the results structure and look at the Jobs outcome in order to find out the best solution,
 * --i.e.: performance on validation.
 * @author MaleLabTs
 */
public interface BestSelector {
    public void setup(Map<String,String> parameters);
    public void elaborate(Results results);
}
