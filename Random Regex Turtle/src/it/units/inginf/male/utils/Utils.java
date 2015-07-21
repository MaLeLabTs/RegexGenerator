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

import it.units.inginf.male.inputs.DataSet;
import it.units.inginf.male.inputs.DataSet.Bounds;
import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.tree.RegexRange;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author MaleLabTs
 */
public class Utils {

    public static float[] calculateMeanFitness(List<Ranking> population) {
        float[] out = new float[population.get(0).getFitness().length];
        for (Ranking r : population) {
            double[] fitness = r.getFitness();
            for (int i = 0; i < out.length; i++) {
                out[i] += fitness[i];
            }
        }
        for (int i = 0; i < out.length; i++) {
            out[i] /= population.size();
        }
        return out;
    }

    public static boolean isAParetoDominateByB(double fitnessA[], double fitnessB[]) {
        boolean dominate = false;
        for (int i = 0; i < fitnessA.length; i++) {
            if (fitnessA[i] > fitnessB[i]) {
                dominate = true;
            } else if (fitnessA[i] < fitnessB[i]) {
                return false;
            }
        }
        return dominate;
    }

    public static List<Ranking> getFirstParetoFront(List<Ranking> tmp) {
        List<Ranking> front = new LinkedList<>();

        for (Ranking r1 : tmp) {
            boolean isDominate = false;
            for (Ranking r2 : tmp) {
                if (r1.equals(r2)) {
                    continue;
                }
                if (Utils.isAParetoDominateByB(r1.getFitness(), r2.getFitness())) {
                    isDominate = true;
                    break;
                }
            }
            if (!isDominate) {
                front.add(r1);
            }
        }
        return front;
    }

    public static String cpuInfo() throws IOException {
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            return "Unaviable";
        }
        FileInputStream fis = new FileInputStream(new File("/proc/cpuinfo"));
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.matches("model name.*")) {
                bufferedReader.close();
                return line.replace("model name	: ", "");
            }
        }
        return "";
    }

    public static double diversity(List<Ranking> population) {
        Set<String> tmp = new HashSet<>();
        for (Ranking r : population) {
            tmp.add(r.getDescription());
        }
        return 100 * tmp.size() / (double) population.size();
    }

    //remove empty extractions 
    public static void removeEmptyExtractions(List<DataSet.Bounds> extractions) {
        for (Iterator<Bounds> it = extractions.iterator(); it.hasNext();) {
            Bounds bounds = it.next();
            if (bounds.size() == 0) {
                it.remove();
            }
        }
    }

    public static void saveFile(String text, String pathOfFile) {
        Writer writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(pathOfFile), "utf-8");
            writer.write(text);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, "Cannot save:", ex);
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private static transient final Set<Character> quoteList = new TreeSet<>(
            Arrays.asList(new Character[]{'?', '+', '*', '.', '[', ']', '\\', '$', '(', ')', '^', '{', '|', '-', '&'}));

    /**
     * Returns a set with all n-grams; 1<n<4
     * @param word
     * @return
     */
    public static Set<String> subparts(String word) {
        return Utils.subparts(word,1,4);
    }

    /**
     * Returns a set with all n-grams; nMin<=n<=nMax
     * @param word
     * @return
     */
    public static Set<String> subparts(String word, int nMin, int nMax) {
        Set<String> subparts = new HashSet<String>();
        for (int n = nMin; n <= nMax; n++) {
            for (int i = 0; i < word.length(); i++) {
                StringBuilder builder = new StringBuilder();
                String w = word.substring(i, Math.min(i + n, word.length()));
                for (char c : w.toCharArray()) {
                    builder.append(escape(c));
                }
                subparts.add(builder.toString());
            }
        }
        return subparts;
    }
    
    public static String escape(char c) {
        if (quoteList.contains(c)) {
            return ("\\" + c);
        }
        return ("" + c);
    }
    
    public static String escape(String string){
        StringBuilder stringBuilder = new StringBuilder();
        char[] stringChars = string.toCharArray();
        for(char character : stringChars){
            stringBuilder.append(escape(character));
        }
        return stringBuilder.toString();
    }

    /**
     * Generates RegexRanges i.e. [a-z] from contiguous characters into the <code>charset</code> list.
     * Follows example where output is represented with regex strings:
     * When <code>charset</code> is {a,b,g,r,t,u,v,5}, the return value is {[a-b],[t-v]}.
     * @param charset the character list i.e. {a,b,g,r,t,u,v,5}
     * @return the contiguous character ranges i.e. {[a-b],[t-v]}
     */
    public static List<RegexRange> generateRegexRanges(Collection<Character> charset) {
         
        List<RegexRange> regexRangesList = new LinkedList<>();
        TreeSet<Character> orderedCharset = new TreeSet<>(charset);
        Character start = null;
        Character old = null;
        for (Character c : charset) {
            if (old == null) {
                //The first round
                old = orderedCharset.first();
                start = old;
                continue; //pick the next 
            }
            //when we have an "hole" or is the last character it checks if the previous range (old -start) is larger than 1; 
            //Ranges bigger than 1 char are saved
            if (((c - old) > 1 || Objects.equals(orderedCharset.last(), c))) {
                if ((old - start) > 1) {
                    regexRangesList.add(new RegexRange(escape(start) + "-" + escape(old)));
                }
                start = c;
            }
            old = c;
        }
        return regexRangesList;
    }
}
