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

import it.units.inginf.male.strategy.ExecutionStrategy;
import it.units.inginf.male.strategy.RunStrategy;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author MaleLabTs
 */
public abstract class AbstractExecutionStrategy implements ExecutionStrategy{

    public static final String RUN_STRATEGY_KEY = "runStrategy";
    

    private static final Logger LOG = Logger.getLogger(AbstractExecutionStrategy.class.getName());

    protected Class<? extends RunStrategy> getStrategy(Map<String, String> parameters) {
        String paramValue = parameters.get(RUN_STRATEGY_KEY);
        Class<? extends RunStrategy> strategyClass;
        try{
            strategyClass = Class.forName(paramValue).asSubclass(RunStrategy.class);
        }catch(Exception x){
            LOG.warning("Falling back to default RunStrategy");
            strategyClass = DefaultStrategy.class;
        }
        return strategyClass;
    }

}
