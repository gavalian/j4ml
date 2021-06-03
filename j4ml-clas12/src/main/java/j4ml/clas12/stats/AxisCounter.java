/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.stats;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class AxisCounter {
    
    int[]      buffer = null;
    double  axisStep  = 0.0;
    double  axisMin   = 0.0;
    
    public AxisCounter(int bins, double min, double step){
        buffer = new int[bins]; axisStep = step; axisMin = min;
    }
        
    public int getNBins(){ return buffer.length;}
    
    public int getBin(double value){
        int index = -1;
        //return (int) (buffer.length*(value-axisMin)*axisStep);
        for(int i = 0; i < buffer.length-1; i++){
            if(value> i*axisStep && value < (i+1)*axisStep) return i;
        }
        return index;
    }
    
    public double axisValue(int bin){
        return bin*axisStep + axisMin;
    }
    
    public int getCounter(int bin){
        return buffer[bin];
    }
    
    public void fill(double value){
        int bin = getBin(value);
        //System.out.printf("%8.5f  %12d\n",value,bin);
        increment(bin);
    }
    
    private void increment(int bin){
        if(bin>=0&&bin<buffer.length){
            buffer[bin] = buffer[bin] + 1;
        }
    }
    
    public Map<String,Double>  getMap(){
        Map<String,Double> axis = new LinkedHashMap<>();
        for(int i = 0; i < buffer.length; i++){
            
        }
        return axis;
    }
    public int  integral(){
        int summ = 0;
        for(int i = 0; i < buffer.length; i++){
            summ += buffer[i];
        }
        return summ;
    }
    
    public static Map<String,Double>  getRatio(AxisCounter a, AxisCounter b){
        Map<String,Double> axis = new LinkedHashMap<>();
        for(int i = 0; i < a.buffer.length; i++){
            
            double value = 0.0;
            //System.out.printf("%14d %14d\n",a.buffer[i],b.buffer[i]);
            if(b.buffer[i]>0)
                value = ((double) a.buffer[i])/b.buffer[i];
            String key = String.format("%12.4f", i*a.axisStep+a.axisMin);
            axis.put(key, value);
        }
        return axis;
    }
}
