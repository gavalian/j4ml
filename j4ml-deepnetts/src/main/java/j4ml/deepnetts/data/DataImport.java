/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.data;

import deepnetts.data.TabularDataSet;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class DataImport {
    
    public static int getMinEntries(String filename){
        int min = 0;
        for(int t = 1; t < 20; t++){
            HipoReader reader = new HipoReader();
            reader.setTags(t);
            int entries = reader.getEventCount();
            if(t==1) min = entries;
            if(entries<min) min = entries;
           reader.close();
        }
        return min;
    }
    
    public static List<float[]> readFileWithTag(String filename, int tag, int max){
        List<float[]> data = new ArrayList<>();
        
        return data;
    }
    
    public static TabularDataSet dataSetWithTag(String filename, int tag, int max){
        TabularDataSet  dataset = new TabularDataSet(6,3);
        
        return dataset;
    }
}
