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
package it.units.inginf.male.outputs.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * DoubleTypeAdapter is a GSON adapter; this maps the NaN double value to a string and viceversa.
 * @author MaleLabTs
 */
public class DoubleTypeAdapter extends TypeAdapter<Double> {

    @Override
    public void write(JsonWriter out, Double value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        if (Double.isNaN(value)) {
            out.value("NaN");
            return;
        }
        out.value(value);
    }

    @Override
    public Double read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        Double nextDouble;
        try {
            //nextDouble parses as a double or fallback to parsing a string using Java parseDouble which MANAGES NaN
            nextDouble = in.nextDouble();
        } catch (NumberFormatException ex) {
            nextDouble = Double.NaN;
        }
        return nextDouble;
    }

}
