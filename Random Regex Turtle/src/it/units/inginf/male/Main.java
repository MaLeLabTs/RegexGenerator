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
package it.units.inginf.male;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.configuration.Configurator;
import it.units.inginf.male.strategy.ExecutionStrategy;
import it.units.inginf.male.strategy.impl.CoolTextualExecutionListener;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.outputs.Results;
import it.units.inginf.male.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * This is the GP engine project, this is not intended to be executed directly from this class.
 * It accepts the filename of a serialized(JSON) configuration file.
 * This is used for experiments and by GP engine developers.
 * @author MaleLabTs
 */
public class Main {
    //This is has beeen reduced for production, the official commandline code has been moved to the ConsoleRegexTurtle project  
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TreeEvaluationException, IOException, Exception {
        if (args.length < 1) {
            printUsage();
            System.exit(0);
        }


        Configuration configuration = Configurator.configure(args[0]);
        //Configuration configuration = new Configuration();
           
        Logger.getLogger("").addHandler(new FileHandler(new File(configuration.getOutputFolder(), "log.xml").getCanonicalPath()));
        Results results = new Results(configuration);
        results.setMachineHardwareSpecifications(Utils.cpuInfo());

        ExecutionStrategy strategy = configuration.getStrategy();
        long startTime = System.currentTimeMillis();
        
        strategy.execute(configuration, new CoolTextualExecutionListener(args[0], configuration, results));
        
        if (configuration.getPostProcessor() != null) {
            startTime = System.currentTimeMillis() - startTime;
            configuration.getPostProcessor().elaborate(configuration, results, startTime);
        }

    }

    private static void printUsage() {
        System.out.println("Usage: java -jar \"Random_Regex_Turtle.jar\" configFileName [startGui]");
    }
}
