/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.data;

import deepnetts.data.TabularDataSet;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Node;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class HipoDataLoader {
    
    public static TabularDataSet readDataList(String filename, int tag, int max){
        TabularDataSet  dataset = new TabularDataSet(6,3);
        
        List<float[]> dataList = new ArrayList<>();
        HipoReader reader = new HipoReader();
        reader.setTags(tag);
        reader.setDebugMode(0);
        reader.open(filename);        
        Event event = new Event();        
        int counter = 0;
        int counterPositive = 0;
        int counterNegative = 0;
        
        while(reader.hasNext()&&counter<max){
            //counter++;
            reader.nextEvent(event);
            //event.scanShow();
            Node params = event.read(1001,1);
            Node   chi2 = event.read(1001,2);
            //Node vector = event.read(1001,6);
            Node vertex = event.read(1001,7);
            Node  means = event.read(1001,4);
            Node  falseMeans = event.read(1001,8);
            
            
            //if(params.getShort(1)<0&&
            if(chi2.getFloat(0)<10.0
                    &&vertex.getFloat(2)>-15&&vertex.getFloat(2)<5){
                float[]  dataTrue = new float[6];
                float[] dataFalse = new float[6];
                for(int i = 0; i < 6; i++) {
                    dataTrue[i] = (float) (means.getFloat(i)/112.0);
                    dataFalse[i] = (float) (falseMeans.getFloat(i)/112.0);
                }
                int charge = params.getShort(1);
                
                if(charge<0){
                    //dataset.add(new TabularDataSet.Item( dataTrue, new float[]{0.0f,1.0f,0.0f}));
                    //dataset.add(new TabularDataSet.Item(dataFalse, new float[]{1.0f,0.0f,0.0f}));
                        dataset.add(new TabularDataSet.Item( dataTrue, new float[]{0.0f,1.0f,0.0f}));
                        dataset.add(new TabularDataSet.Item(dataFalse, new float[]{1.0f,0.0f,0.0f}));   
                        counterNegative++;
                } else { 
                        dataset.add(new TabularDataSet.Item( dataTrue, new float[]{0.0f,0.0f,1.0f}));
                        dataset.add(new TabularDataSet.Item(dataFalse, new float[]{1.0f,0.0f,0.0f}));                    
                        counterPositive++;
                }
                counter++;    
            }
            //dataList.add(dataTrue);
            //dataList.add(dataFalse);
                        
            
            if(counter>=max) break;
        }
        
        System.out.printf("hipo::read >> loaded events with tag = %4d, size = %d (pos = %d, neg = %d) \n"
                ,tag,counter,counterPositive,counterNegative);
        return dataset;
    }
    
    public static TabularDataSet readDataSet(String filename, int max){
        TabularDataSet  dataset = new TabularDataSet(6,3);
        TabularDataSet  dsn = HipoDataLoader.readDataList(filename, 0, max);
        dataset.addAll(dsn);
        TabularDataSet  dsp = HipoDataLoader.readDataList(filename, 1, max);
        dataset.addAll(dsp);
        /*for(int i = 1; i <=20; i++){
            TabularDataSet  ds = HipoDataLoader.readDataList(filename, i, max);
            dataset.addAll(ds);
        
        }*/

        dataset.setColumnNames(new String[]{"a","b","c","d","e","f","no","neg","pos"});
        dataset.shuffle();
        return dataset;
    }
}
