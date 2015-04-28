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

import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.strategy.ExecutionListener;
import it.units.inginf.male.strategy.ExecutionListenerFactory;
import it.units.inginf.male.strategy.ExecutionStrategy;
import it.units.inginf.male.strategy.RunStrategy;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This execution listener is only a STUB, an example.
 * @author MaleLabTs
 */
public class DefaultExecutionListener implements ExecutionListener,ExecutionListenerFactory {

    private final static Logger LOG = Logger.getLogger(DefaultExecutionListener.class.getName());

    @Override
    public void logGeneration(RunStrategy strategy, int generation, Node best, double[] fitness, List<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();       
        //LOG.log(Level.INFO, "Job {0} Gen => {1} Fitness: {2}", new Object[]{jobId, generation, fitness});
    }

    private double getRatio(long cacheHit, long cacheMiss) {
        return cacheHit + cacheMiss == 0 ? 0 : cacheHit / ((double) cacheHit + cacheMiss);
    }

    @Override
    public void evolutionComplete(RunStrategy strategy, int generation, List<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();

        try {
            Node best = population.get(0).getTree();
            StringBuilder abuilder = new StringBuilder();
            best.describe(abuilder);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void evolutionStarted(RunStrategy strategy) {
        int jobId = strategy.getConfiguration().getJobId();        
    }

    @Override
    public ExecutionListener getNewListener() {
        return this;
    }

    @Override
    public void evolutionFailed(RunStrategy strategy, TreeEvaluationException cause) {
        int jobId = strategy.getConfiguration().getJobId();
        try {

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void register(ExecutionStrategy strategy) {
        //NO OP
    }
    
    @Override
    public void evolutionStopped() {
        //let's do nothing, there is no status here
    }
}
