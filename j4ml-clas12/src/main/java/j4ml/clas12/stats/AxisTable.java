/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.stats;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class AxisTable {
    private List<AxisCounter> counterList = new ArrayList<>();
    private int               nBins = 0;
    
    public AxisTable(List<AxisCounter> counter){
        counterList.addAll(counter);
        nBins = counter.get(0).getNBins();
    }
    
    public AxisTable(int size, int bins, double min, double step){
        for(int i = 0; i < size; i++){
            counterList.add(new AxisCounter(bins,min,step));
        }
        nBins = bins;
    }
    
    public AxisTable fill(int index, double value){
        this.counterList.get(index).fill(value); return this;
    }
    
    public void show(){
        System.out.printf("showing table -> %d counters\n", counterList.size() );
        AxisCounter counter = counterList.get(0);
        for(int i = 0; i < nBins; i++){            
            System.out.printf("%8.5f",counter.axisValue(i));
            for(int c = 0; c < counterList.size(); c++){
                System.out.printf(" %8d ", counterList.get(c).getCounter(i));
            }
            System.out.printf("\n");
        }
    }
}
