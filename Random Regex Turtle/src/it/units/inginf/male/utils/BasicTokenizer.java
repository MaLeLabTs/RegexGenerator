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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This tokenizer is implemented with the regular expression "\\w+|\\s+|[^\\w\\s]+".
 * This tokenizer generate no overlapping tokens, in fact the elements, of the tokens list, when concatenated re-generate the original string. 
 * @author MaleLabTs
 */
public class BasicTokenizer implements Tokenizer {
    
    private final Pattern pattern = 
            Pattern.compile("\\w+|\\s+|[^\\w\\s]+");

    
    @Override
    public List<String> tokenize(String string){
        List<String> tokensList = new LinkedList<>();
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            tokensList.add(matcher.group());
        }
        return tokensList;
    }
}
