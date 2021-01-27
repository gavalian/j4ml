/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12;

import j4ml.clas12.track.ClusterCombinations;
import j4ml.clas12.track.ClusterStore;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class Clas12TrackAnalyzer {
    
    private ClusterCombinations         resolvedTracks = new ClusterCombinations();
    private ClusterCombinations         combinations   = new ClusterCombinations();
    private ClusterCombinations         combinations5   = new ClusterCombinations();
    private ClusterStore  store = new ClusterStore();
    
    
    
    public Clas12TrackAnalyzer(){
        
    }
    
    public ClusterCombinations getTracks(){ return resolvedTracks;}
    
    public void readBank(Bank bank, int sector){
        store.reset();
        //this.resolvedTracks.reset();
        int nrows = bank.getRows();
        
        for(int i = 0; i < nrows; i++){
                int sec = bank.getInt("sector", i);
                int id     = bank.getInt("id", i);
                int superlayer = bank.getInt("superlayer", i);
                double wire = bank.getFloat("avgWire", i);
                if(sector==sec){
                    store.add(superlayer-1, id, wire);
                }
        }
        combinations.reset();
        combinations5.reset();
        store.getCombinationsFull(combinations);
        store.getCombinations(combinations5);
    }
    
    public ClusterCombinations getCombinations(){ return this.combinations;}
    public ClusterCombinations getCombinationsPartial(){ return this.combinations5;}
    
    public void analyze(){
        
        //resolvedTracks.reset();
        combinations.analyze();
        
        int nsize = combinations.getSize();
            
        //System.out.println("THEN \n" + comb5);
        for(int i = 0; i < nsize; i++){
            
            if(combinations.setRow(i).getStatus()>10){
                    
                    combinations.setRow(i);
                    double[] means = combinations.getFeatures(i);
                    int[]      ids = combinations.getLabels(i);
                    
                    resolvedTracks.add(ids, means);
                    resolvedTracks.setRow(resolvedTracks.getSize()-1);
                    resolvedTracks.setProbability(combinations.getProbability());
                    resolvedTracks.setStatus(combinations.getStatus());
                    
                    int nsize5 = combinations5.getSize();
                    for(int i5 = 0; i5 < nsize5; i5++){
                        combinations5.setRow(i5);
                        if(combinations5.contains(ids)==true) combinations5.setStatus(0);
                    }
                }
        }
        
        combinations5.analyze();
        
        int nsize5 = combinations5.getSize();
        for(int i5 = 0; i5 < nsize5; i5++){
            combinations5.setRow(i5);
            if(combinations5.getStatus()>10){
                double[] means = combinations5.getFeatures(i5);
                int[]      ids = combinations5.getLabels(i5);
                if(Math.abs(means[0]-means[1])<25&&
                        Math.abs(means[0]-means[1])<25&&
                        Math.abs(means[0]-means[1])<25){
                    boolean addTrack = true;
                    /*for(int c = 0; c < resolvedTracks.getSize(); c++){
                    if(resolvedTracks.setRow(c).contains(ids)==true) addTrack = false;
                    }*/
                    if(addTrack==true){
                        resolvedTracks.add(ids, means);
                        resolvedTracks.setRow(resolvedTracks.getSize()-1);
                        resolvedTracks.setProbability(combinations5.getProbability());
                        resolvedTracks.setStatus(combinations5.getStatus());
                    }
                }
            }
        }
    }
    
    public static void main(String[] args){
        String filename = args[0];
        int    eventNumber = Integer.parseInt(args[1]);
        
        Clas12TrackClassifier classifier = new Clas12TrackClassifier("trackClassifier.nnet","trackFixer.nnet");
        
        Clas12TrackAnalyzer analyzer = new Clas12TrackAnalyzer();
        
        HipoReader reader = new HipoReader();
        reader.open(filename);
        Event event = new Event();
        Bank  cBank = reader.getBank("HitBasedTrkg::HBClusters");
        Bank  tBank = reader.getBank("HitBasedTrkg::HBTracks");
        reader.getEvent(event, eventNumber);
        
        event.read(cBank);
        event.read(tBank);
        //cBank.show();
        //cBank.show();
        tBank.show();
        System.out.println("BANK SIZE = " + cBank.getRows());
        
        //classifier.processBankPartial(cBank);
        for(int sector = 1 ; sector <= 6; sector++){
            analyzer.readBank(cBank, sector);
            classifier.evaluate(analyzer.getCombinations());
            //analyzer.getCombinations().analyze();
            //System.out.println(analyzer.getCombinations());
            classifier.evaluate5(analyzer.getCombinationsPartial());
            
            analyzer.analyze();
        }
        long now = System.currentTimeMillis();
        System.out.println("Resolved tracks >>>\n" + analyzer.getTracks());
        
    }
}
