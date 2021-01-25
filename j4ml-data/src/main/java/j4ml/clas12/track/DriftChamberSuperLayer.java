/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.track;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class DriftChamberSuperLayer {
    
    private int[][] wires = new int[6][112];
    
    private double  intercept = 0.0;
    private double      slope = 0.0;
    
    public DriftChamberSuperLayer(){
        
    }
    
    public void set(int layer, int wire, int value){
        wires[layer][wire] = value;
    }
    
    public int get(int layer, int wire){
        return wires[layer][wire];
    }
    
    public double getLayerAverage(int layer){
       double summ = 0.0;
       int      count = 0;
       for(int i = 0; i < 112; i++) if(get(layer,i)>0){
           summ += (double) (i+1);
           count++;
       }
       if(count>0) return summ/count;
       return 0.0;
    }
    
    public void fix(){
        fit();
        for(int i = 0; i < 6; i++){
            double average = getLayerAverage(i);
            if(average<0.01){
                double wire = intercept + slope * (i) - 1;
                int wirepos = (int) Math.round(wire);
                if(wirepos>=112) wirepos = 111;
                if(wirepos<0) wirepos = 0;
                set(i,wirepos,1);
                //System.out.printf("wire position %d = %f\n",i,wire);
            }
        }
    }
    
    public void fit(){
        List<Double> x = new ArrayList<Double>();
        List<Double> y = new ArrayList<Double>();
        for(int i = 0; i < 6; i++){
            double value = getLayerAverage(i);
            if(value>0.01){
                x.add((double)i);
                y.add(value);
            }
        }
        
        int n = x.size();
        double   sumX = 0.0;
        double  sumX2 = 0.0;
        double   sumY = 0.0;
        double  sumXY = 0.0;
        
        for(int i = 0; i < n; i++){
            sumX  += x.get(i);
            sumX2 += x.get(i)*x.get(i);
            sumY  += y.get(i);
            sumXY += x.get(i)*y.get(i);
        }
        slope = (n*sumXY - sumX*sumY)/(n*sumX2-sumX*sumX);
        intercept = (sumY - slope*sumX)/n;
        //System.out.printf("fit:: intercept = %12.5f , slope = %12.5f\n",intercept,slope);
    }
    
    @Override
    public String toString(){
         StringBuilder str = new StringBuilder();
        for(int layer = 0; layer < 6; layer++){
            for(int wire = 0; wire < 112; wire++){
                if(get(layer,wire)==0){
                    str.append("-");
                } else {
                    str.append("X");
                }
            }
            str.append("\n");
        }
        return str.toString();
    }
    
    public static void main(String[] args){
        DriftChamberSuperLayer sl = new DriftChamberSuperLayer();
        sl.set(0, 20, 1);
        //sl.set(1, 20, 1);
        sl.set(2, 24, 1);
        sl.set(3, 26, 1);
        sl.set(4, 28, 1);
        sl.set(5, 30, 1);
        
        System.out.println(sl);
        
        sl.fit();
        sl.fix();
    }
}
