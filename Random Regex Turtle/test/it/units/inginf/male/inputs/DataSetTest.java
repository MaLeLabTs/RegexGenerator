/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.units.inginf.male.inputs;

import it.units.inginf.male.inputs.DataSet.Bounds;
import it.units.inginf.male.inputs.DataSet.Example;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author MaleLabTs
 */
public class DataSetTest {
    
    public DataSetTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    

    /**
     * Test of initStripedDatasetView method, of class DataSet.
     */
    @Test
    public void testInitStripedDatasetView() {
        DataSet dataSet = new DataSet("test", "striping test", "");
        Example example = new Example();
        example.setString("123456789123456789PROVA123456789123456789 123456789123456789123456789123456789PROVA123456789123456789");
        int provaIndex1 = example.getString().indexOf("PROVA");
        int provaIndex2 = example.getString().indexOf("PROVA", provaIndex1+2);
        example.getMatch().add(new Bounds(provaIndex1,provaIndex1+"PROVA".length()));
        example.getMatch().add(new Bounds(provaIndex2,provaIndex2+"PROVA".length()));
        dataSet.getExamples().add(example);
        dataSet.updateStats();
        int marginSize = 2;
        DataSet stripedDataset = dataSet.initStripedDatasetView(marginSize);
        int expExperimentsNumber = 2;
        assertEquals(expExperimentsNumber, stripedDataset.getNumberExamples());
        for(Example stripedExample : stripedDataset.getExamples()){
            stripedExample.populateAnnotatedStrings();
            assertEquals("PROVA".length()*(marginSize+1), stripedExample.getString().length());
            for(String matchString : stripedExample.getMatchedStrings()){
                assertEquals("PROVA", matchString);
            }
        }
        
        //Test the boundires merge operation
        marginSize = 20;
        stripedDataset = dataSet.initStripedDatasetView(marginSize);
        expExperimentsNumber = 1;
        assertEquals(expExperimentsNumber, stripedDataset.getNumberExamples());
        for(Example stripedExample : stripedDataset.getExamples()){
            stripedExample.populateAnnotatedStrings();
            //Example should be unaltered
            assertEquals(example.getString(), stripedExample.getString());
            for(String matchString : stripedExample.getMatchedStrings()){
                assertEquals("PROVA", matchString);
            }
            assertArrayEquals(example.getMatch().toArray(), stripedExample.getMatch().toArray());
        }
    }

      
}
