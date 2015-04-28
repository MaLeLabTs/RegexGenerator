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
package it.units.inginf.male.utils;

/**
 *
 * @author MaleLabTs
 */
public class BasicStats {

    //added transient modifier for webapp
    public long fp = 0;
    transient public long fn = 0;
    public long tp = 0;
    transient public long tn = 0;

    public double accuracy() {
        return ((double) (tp + tn)) / (tp + tn + fp + fn);
    }

    public double precision() {
        return ((double) tp) / (tp + fp);
    }

    public double recall() {
        return ((double) tp) / (tp + fn);
    }
    
    public double fpr(){
        return ((double) fp) / (tn + fp);
    }
    
    public double fnr(){
        return ((double) fn) / (tp + fn);
    }

    /**
     * To use when number of positives cases != tp+fn (eg: text extraction)
     *
     * @param positives
     * @return
     */
    public double recall(int positives) {
        return ((double) tp) / (positives);
    }

    public double trueNegativeRate() {
        return ((double) tn) / (tn + fn);
    }

    public double specificity() {
        return ((double) tn) / (tn + fp);
    }

    /**
     * To use when number of negative cases != tn+fp (eg: text extraction)
     *
     * @param negatives
     * @return
     */
    public double specificity(int negatives) {
        return ((double) tn) / (negatives);
    }

    public double fMeasure() {
        double precision = this.precision();
        double recall = this.recall();
        return 2 * (precision * recall) / (precision + recall);
    }

    public BasicStats add(BasicStats stats) {
        this.fp += stats.fp;
        this.fn += stats.fn;
        this.tp += stats.tp;
        this.tn += stats.tn;
        return this;
    }
}
