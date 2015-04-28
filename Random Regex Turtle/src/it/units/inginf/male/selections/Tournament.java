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
package it.units.inginf.male.selections;

import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.tree.Node;
import java.util.List;

/**
 *
 * @author MaleLabTs
 */
public class Tournament implements Selection {

    private final Context context;

    public Tournament(Context context) {
        this.context = context;
    }

    @Override
    public Node select(List<Ranking> population) {

        return tournament(population);

    }

    private Node tournament(List<Ranking> population) {

        int best = population.size();        
        for (int t = 0; t < 7; t++) {
            int index = context.getRandom().nextInt(population.size());
            best = Math.min(best, index);
        }

        return population.get(best).getTree();

    }
}
