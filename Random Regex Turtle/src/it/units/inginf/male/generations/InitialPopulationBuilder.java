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

import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.tree.Node;
import java.util.List;

/**
 *
 * @author MaleLabTs
 */
public interface InitialPopulationBuilder {

    /**
     * Returns a copy of the shared population list
     * @return
     */
    public List<Node> init();
    
    
    /**
     * Returns a copy of the shared population list
     * @param context
     * @return
     */
    public List<Node> init(Context context);

    /**
     * Updates the shared population object (into main configuration) 
     * @param configuration
     */
    public void setup(Configuration configuration);
}
