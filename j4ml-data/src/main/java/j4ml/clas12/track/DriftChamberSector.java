/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.track;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.operations.BankIterator;

/**
 *
 * @author gavalian
 */
public class DriftChamberSector {
    private int[][]  sectorData = new int[36][112];
    
    public DriftChamberSector(){}
    
    public void set(int layer, int wire, int value){
        sectorData[layer][wire] = value;
    }
    
    public int get(int layer, int wire){
        return sectorData[layer][wire];
    }
    
    public double getLayerAverage(int layer){
        double wires = 0.0;
        int   count  = 0;        
        for(int i = 0; i < 112; i++){            
            if(get(layer,i)>0) {
                wires += (double) i;
                count++;
            }
        }
        
        double average = 0.0;
        if(count>0) average = wires/count;
        return average;
    }
    
    public String getTrackSeries(){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < 36; i++){
            str.append(String.format("%d:%.5f ", i+1,getLayerAverage(i)/112.0));
        }
        return str.toString();
    }
    
    public String getDataString(){
        StringBuilder str = new StringBuilder();
        for(int layer = 0; layer < 36; layer++){
            for(int wire = 0; wire < 112; wire++){      
                if(get(layer,wire)>0){
                    int id = 1 + layer*112 + wire;
                    str.append(String.format("%d:1.0 ", id));
                }
            }
        }
        return str.toString();
    }
    
    public int getLayerCount(int layer){
        int counter = 0;
        for(int i = 0; i < 112; i++) if(get(layer,i)>0) counter++;
        return counter;
    }
    
    public int getSuperLayerCount(int superlayer){
       int counter = 0;
       int start   = superlayer*6;
       for(int i = 0; i < 6; i++){
           counter += getLayerCount(start+i);
       }
       return counter;
    }
    
    public void read(Bank bank, BankIterator iter){
        int nrows = iter.count();        
        for(int i = 0; i < nrows; i++){
            int index = iter.getIndex(i);
            int layer = bank.getInt("layer",index);
            int wire  = bank.getInt("component",index);
            set(layer-1,wire-1,1);
        }
    }
    
    public DriftChamberSuperLayer getSuperLayer(int s){
        int start = s*6;
        DriftChamberSuperLayer sl = new DriftChamberSuperLayer();
        for(int i = 0; i < 6; i++){
            for(int w = 0; w < 112; w++){
                if(get(start+i,w)>0) sl.set(i, w, 1);
            }
        }
        return sl;
    }
    
    public  DriftChamberSector getFixed(){
        DriftChamberSector sector = new DriftChamberSector();        
        for(int i = 0; i < 6; i++){
            DriftChamberSuperLayer sl = getSuperLayer(i);
            sl.fix();
            int start = i*6;
            for(int layer = 0; layer < 6; layer++){
                for(int wire = 0; wire < 112; wire++){
                    sector.set(layer+start, wire, sl.get(layer, wire));
                }
            }
        }
        return sector;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int layer = 0; layer < 36; layer++){
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
}
