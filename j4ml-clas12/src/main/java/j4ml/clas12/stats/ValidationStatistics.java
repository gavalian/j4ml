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
public class ValidationStatistics {
    
    Map<String,TrackStatistics> statsMap = new LinkedHashMap<>();
    
    public ValidationStatistics(){
        
    }
    
    public void addMetrics(String name){
        statsMap.put(name, new TrackStatistics(name));
    }
    
    public TrackStatistics getMetrics(String name){
        return statsMap.get(name);
    }
    
    public void show(){
        System.out.println();
        System.out.println("+------------------------------------------------------------------------------------------+");
        System.out.printf("| %26s | %14s | %12s | %12s | %12s |\n", "name","tracks (conv)", "matched","unmatched","excess");
        System.out.println("+------------------------------------------------------------------------------------------+");
       
       for(Map.Entry<String,TrackStatistics> entry : statsMap.entrySet()){
           System.out.println(entry.getValue().getString());
       }       
       System.out.println("+------------------------------------------------------------------------------------------+");
       System.out.println();
    }
    
    public static class TrackStatistics {
        
    private String name = "stats";
    
    private Long conventional = 0L;
    private Long artificialMatched = 0L;
    private Long artificialUnMatched = 0L;
    private Long artificialExcess = 0L;
    
    private AxisCounter counterTracks = null;
    private AxisCounter counterPredictions = null;
            
    public TrackStatistics(String _name){
        name = _name;
        counterTracks = new AxisCounter(20,0.0,0.5);
        counterPredictions = new AxisCounter(20,0.0,0.5);
    }
    
    public String getName(){
        return name;
    }
    
    public void addMatched(){
        conventional++;
        artificialMatched++;
    }
    
    public Map<String,Double> histogram(){
        return AxisCounter.getRatio(counterPredictions, counterTracks);
    }
    
    public void fill(double momentum){
        counterTracks.fill(momentum);
    }
    
    public void fillMatched(double momentum){
        counterPredictions.fill(momentum);
    }
    
    public void addUnMatched(){
        conventional++;
        artificialUnMatched++;
    }
    
    public void addExcess(){
        artificialExcess++;
    }
    
    public String getString(){
        
        double percentMatches = ((double)  artificialMatched )/conventional;
        double percentUnMatches = ((double)  artificialUnMatched )/conventional;
        double percentExcess = ((double)  artificialExcess )/conventional;
        return String.format("| %26s | %14d | %12.4f | %12.4f | %12.4f |", name, conventional,
                percentMatches,percentUnMatches, percentExcess);
        
    }
    }
}
