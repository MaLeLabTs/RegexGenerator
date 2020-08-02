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

    private DataSet dataSet = new DataSet("test", "striping test", "");
    private final int MARGIN_SIZE = 2;
    private DataSet stripedDataset = dataSet.initStripedDatasetView(MARGIN_SIZE);
    private Example example;

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

    public void loopContentTest(Example stripedExample, String expected, String actual) {
        stripedExample.populateAnnotatedStrings();
        assertEquals(expected, actual);
        for(String matchString : stripedExample.getMatchedStrings()){
            assertEquals("PROVA", matchString);
        }
    }

    public void init(boolean flag) {
        for (Example stripedExample : stripedDataset.getExamples()) {
            String expected = String.valueOf("PROVA".length()*(MARGIN_SIZE+1));
            String actual = String.valueOf(stripedExample.getString().length());
            if (flag) {
                expected = example.getString();
                actual = stripedExample.getString();
            }
            loopContentTest(stripedExample, expected, actual);
            if (flag) {
                assertArrayEquals(example.getMatch().toArray(), stripedExample.getMatch().toArray());
            }
        }
    }

    /**
     * Test of initStripedDatasetView method, of class DataSet.
     */
    @Test
    public void testInitStripedDatasetView() {

        example = new Example();
        example.setString("123456789123456789PROVA123456789123456789 123456789123456789123456789123456789PROVA123456789123456789");
        int provaIndex1 = example.getString().indexOf("PROVA");
        int provaIndex2 = example.getString().indexOf("PROVA", provaIndex1+2);
        example.getMatch().add(new Bounds(provaIndex1,provaIndex1+"PROVA".length()));
        example.getMatch().add(new Bounds(provaIndex2,provaIndex2+"PROVA".length()));
        dataSet.getExamples().add(example);
        dataSet.updateStats();
        int marginSize = 2;
        int expExperimentsNumber = 2;
        assertEquals(expExperimentsNumber, stripedDataset.getNumberExamples());
        init(false);
        //Test the boundires merge operation
        marginSize = 20;
        stripedDataset = dataSet.initStripedDatasetView(marginSize);
        expExperimentsNumber = 1;
        assertEquals(expExperimentsNumber, stripedDataset.getNumberExamples());
        init(true);
    }

      
}
