/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.data;

import deepnetts.data.TabularDataSet;
import j4ml.deepnetts.network.TextFileReader;

/**
 *
 * @author gavalian
 */
public class LSVMDataProvider {
    public TabularDataSet readFile(String dataFile){
        TabularDataSet  dataset = new TabularDataSet(6,3);
        TextFileReader reader = new TextFileReader();
        reader.open(dataFile);
        
        while(reader.readNext()==true){
            String line = reader.getString();
            String[] tokens = line.split("\\s+");
            float[] data = new float[6];
            float[] output = new float[3];
            for(int i = 0; i < 3; i++) output[i] = 0;
            int label = Integer.parseInt(tokens[0]);
            output[label] = 1.0f;
            
            for(int i = 1; i < tokens.length; i++){
                String[] binData = tokens[i].split(":");
                int bin = Integer.parseInt(binData[0]);
                float value = Float.parseFloat(binData[1]);
                data[bin-1] = value;
            }
            dataset.add(new TabularDataSet.Item( data, output));
        }
        dataset.setColumnNames(new String[]{"a","b","c","d","e","f","no","neg","pos"});
        dataset.shuffle();
        return dataset;
    }
}
