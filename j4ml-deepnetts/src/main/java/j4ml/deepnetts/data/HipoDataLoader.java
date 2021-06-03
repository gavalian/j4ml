/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.data;

import deepnetts.data.TabularDataSet;
import java.util.ArrayList;
import java.util.List;
import javax.visrec.ml.data.DataSet;

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
            Node  slopes = event.read(1001,5);
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
                
               /* System.out.printf(" %3d %8.5f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f\n",
                        charge, chi2.getFloat(0), dataTrue[0]*112,dataTrue[1]*112,
                        dataTrue[2]*112,dataTrue[3]*112,
                        dataTrue[4]*112,dataTrue[5]*112,
                        slopes.getFloat(0),slopes.getFloat(1),
                        slopes.getFloat(2),slopes.getFloat(3),
                        slopes.getFloat(4),slopes.getFloat(5)
                        
                        );
                */
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
    
    public static TabularDataSet readEncoderDataWithTag(String filename, int tag, int max){
        
        TabularDataSet  dataset = new TabularDataSet(6,6);
        
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
            Node  slopes = event.read(1001,5);
            Node  falseMeans = event.read(1001,8);
                        
            //if(params.getShort(1)<0&&
            if(chi2.getFloat(0)<10.0
                    &&vertex.getFloat(2)>-15&&vertex.getFloat(2)<5){
                
                for(int m = 0; m < 6; m++){
                
                    float[]     dataTrue = new float[6];                    
                    float[]  dataCorrupt = new float[6];
                    
                    for(int i = 0; i < 6; i++) {
                        dataTrue[i] = (float) (means.getFloat(i)/112.0);
                        dataCorrupt[i] = (float) (means.getFloat(i)/112.0);
                        //dataFalse[i] = (float) (falseMeans.getFloat(i)/112.0);
                    }                    
                    dataCorrupt[m] = (float) 0.0;
                    dataset.add(new TabularDataSet.Item( dataCorrupt, dataTrue));
                }
                
                int charge = params.getShort(1);
                
               /* System.out.printf(" %3d %8.5f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f\n",
                        charge, chi2.getFloat(0), dataTrue[0]*112,dataTrue[1]*112,
                        dataTrue[2]*112,dataTrue[3]*112,
                        dataTrue[4]*112,dataTrue[5]*112,
                        slopes.getFloat(0),slopes.getFloat(1),
                        slopes.getFloat(2),slopes.getFloat(3),
                        slopes.getFloat(4),slopes.getFloat(5)
                        
                        );
                */
                if(charge<0){                   
                        counterNegative++;
                } else {                                           
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
    
    public static TabularDataSet readEncoderData(String filename, int max){
        TabularDataSet  dataset = new TabularDataSet(6,6);
        for(int i = 1; i <= 40; i++){
            TabularDataSet  dsn = HipoDataLoader.readEncoderDataWithTag(filename, i, max);
            dataset.addAll(dsn);
        }
        dataset.setColumnNames(new String[]{"a","b","c","d","e","f",
            "aa","bb","cc","dd","ee","ff"});
        dataset.shuffle();
        return dataset;
    }
    
    public static TabularDataSet readDataSet(String filename, int max){
        
        
        TabularDataSet  dataset = new TabularDataSet(6,3);
        for(int i = 1; i <= 40; i++){
            TabularDataSet  dsn = HipoDataLoader.readDataList(filename, i, max);
            dataset.addAll(dsn);
        }
        //TabularDataSet  dsp = HipoDataLoader.readDataList(filename, 1, max);
        //dataset.addAll(dsp);
        /*for(int i = 1; i <=20; i++){
            TabularDataSet  ds = HipoDataLoader.readDataList(filename, i, max);
            dataset.addAll(ds);
        
        }*/

        dataset.setColumnNames(new String[]{"a","b","c","d","e","f","no","neg","pos"});
        
        dataset.shuffle();
        return dataset;
    }
    
    public static TabularDataSet readParameterData(String filename, int charge, int max){
        TabularDataSet  dataset = new TabularDataSet(6,4);
        if(charge>0){
            for(int i = 1; i <= 20; i++){
                TabularDataSet  dsn = HipoDataLoader.readParameterDataWithTag(filename, i, max);
                dataset.addAll(dsn);
            }
        } else {
           for(int i = 21; i <= 40; i++){
                TabularDataSet  dsn = HipoDataLoader.readParameterDataWithTag(filename, i, max);
                dataset.addAll(dsn);
            } 
        }
        
       dataset.setColumnNames(new String[]{"c1","c2","c3","c4","c5","c6","p","theta","phi","z"});
        
       dataset.shuffle();
       return dataset; 
    }
    
    public static TabularDataSet readParameterDataWithTag(String filename, int tag, int max){
        TabularDataSet  dataset = new TabularDataSet(6,4);
        
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
            Node vector = event.read(1001,6);
            Node vertex = event.read(1001,7);
            Node  means = event.read(1001,4);
            Node  slopes = event.read(1001,5);
            Node  falseMeans = event.read(1001,8);
            if(params.getInt(0)==1){
            //if(params.getShort(1)<0&&
            if(chi2.getFloat(0)<10.0
                    &&vertex.getFloat(2)>-15&&vertex.getFloat(2)<35){                
                float[]     dataTrue = new float[6];        
                float[]   dataParams = new float[4];                    
                for(int i = 0; i < 6; i++) {
                    dataTrue[i] = (float) (means.getFloat(i)/112.0);
                    //dataFalse[i] = (float) (falseMeans.getFloat(i)/112.0);
                }                    
                
                double px = vector.getFloat(0);
                double py = vector.getFloat(1);
                double pz = vector.getFloat(2);
                double mag = Math.sqrt(px*px+py*py+pz*pz);
                double theta = Math.acos(pz/mag);
                double phi = Math.atan2(py, px);
                
                
                if(mag>0.5&&mag<10.0&&
                        theta>Math.toRadians(5.0)&&
                        theta<Math.toRadians(45.0)
                        ){
                    double n_mag   = (mag)/10.0;
                    double n_theta = (Math.toDegrees(theta) - 5.0)/40.0;
                    double n_phi   = (phi + Math.PI)/(2*Math.PI);
                    double n_z     = (vertex.getFloat(2) + 15)/50.0;
                    //System.out.printf("%8.5f %8.5f\n",phi*57.29, theta*57.29);
                    
                    dataParams[0] = (float) n_mag;
                    dataParams[1] = (float) n_theta;
                    dataParams[2] = (float) n_phi;
                    dataParams[3] = (float) n_z;
                    
                    dataset.add(new TabularDataSet.Item( dataTrue,dataParams));
                }
            }
                
                int charge = params.getShort(1);
                
               /* System.out.printf(" %3d %8.5f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f %8.3f\n",
                        charge, chi2.getFloat(0), dataTrue[0]*112,dataTrue[1]*112,
                        dataTrue[2]*112,dataTrue[3]*112,
                        dataTrue[4]*112,dataTrue[5]*112,
                        slopes.getFloat(0),slopes.getFloat(1),
                        slopes.getFloat(2),slopes.getFloat(3),
                        slopes.getFloat(4),slopes.getFloat(5)
                        
                        );
                */
                if(charge<0){                   
                        counterNegative++;
                } else {                                           
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
    
    public static void main(String[] args){
        //DataSet set = HipoDataLoader.readDataSet(args[0], 2500);
        String filename = "/Users/gavalian/Work/DataSpace/ml/extract_output.hipo";
        DataSet set = HipoDataLoader.readParameterData(filename,-1, 100);

    }
}
