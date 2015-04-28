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
package it.units.inginf.male.strategy.impl;

/**
 * This class is the same as DefaultStrategy but changes the defaults for the termination criteria. With CEIndependentStategy the steady-best
 * termination criteria is enabled by default. With termination criteria enabled a Job should terminate first when there are no changes in the best
 * individual for the specified amount of generations. The default number of generations without changes which triggers the job interruption is 200.
 * You can change the default values or configurations by using the configuration strategyParametners in configuration class/file, they are the same
 * as DefaultStrategy, let's refer to DefaultStrategy documentation.
 * @author MaleLabTs
 */
public class CESteadyBestStrategy extends  DefaultStrategy{

    
    public CESteadyBestStrategy() {
        this.terminationCriteria = true;
        this.terminationCriteriaGenerations = 200;
    }
  
}
