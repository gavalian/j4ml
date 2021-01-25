/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12;

import j4ml.clas12.track.ClusterCombinations;
import j4ml.clas12.track.ClusterStore;
import j4ml.networks.NeurophTrackClassifier;
import j4ml.networks.NeurophTrackFixer;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class Clas12TrackClassifier {
    
    NeurophTrackClassifier neurophClassifier = null;
    NeurophTrackFixer           neurophFixer = null;
    
    ClusterCombinations         resolvedTracks = new ClusterCombinations();
    private String envDirectory = "CLAS12DIR";
    private String envPath      = "etc/data/neuroph";
    ClusterStore  store = new ClusterStore();
    
    public Clas12TrackClassifier(){
        
    }
    
    public Clas12TrackClassifier(String file){
         neurophClassifier  = new NeurophTrackClassifier();
         neurophClassifier.load(file);
    }
    
    public void setEnvDirectory(String env){
        this.envDirectory = env;
    }
    
    public void setEnvPath(String path){
        this.envPath = path;
    }
     
    public Clas12TrackClassifier(String file, String fixerFile){
         neurophClassifier  = new NeurophTrackClassifier();
         neurophClassifier.load(file);
         neurophFixer = new NeurophTrackFixer();
         neurophFixer.load(fixerFile);
    }
    
    
    public final void load(String classifierFile, String fixerFile){
        String env = System.getenv(envDirectory);
        neurophClassifier  = new NeurophTrackClassifier();
         neurophClassifier.load(env + "/" + envPath + "/" + classifierFile);
         neurophFixer = new NeurophTrackFixer();
         neurophFixer.load(env + "/"  + envPath + "/" + fixerFile);
    }
    
    public ClusterCombinations getTracks(){return resolvedTracks;}
    
    
    public void processBank(Bank bank){      
        
        int nrows = bank.getRows();
        ClusterCombinations  comb = new ClusterCombinations();
        ClusterCombinations comb5 = new ClusterCombinations();
        
        resolvedTracks.reset();
        
        for(int sector = 1; sector <=6 ; sector++){
            store.reset();            
            for(int i = 0; i < nrows; i++){
                int sec = bank.getInt("sector", i);
                int id     = bank.getInt("id", i);
                int superlayer = bank.getInt("superlayer", i);
                double wire = bank.getFloat("avgWire", i);
                if(sector==sec){
                    store.add(superlayer-1, id, wire);
                }
            }
            
            store.getCombinationsFull(comb);
            store.getCombinations(comb5);
            
            /*int nsize = comb.getSize();
            
            for(int i = 0; i < nsize; i++){
                double[] features = comb.getFeatures(i);
                for(int f = 0; f < features.length; f++) features[f] = features[f]/112.0;
                double[] result   = neurophClassifier.evaluate(features);
                int index = neurophClassifier.getOutputClass(result);
                comb.setRow(i).setProbability(result[index]);
                comb.setRow(i).setStatus(index);
            }*/
            
            this.evaluate(comb);
            this.evaluate5(comb5);
            
            comb.analyze();
            int nsize = comb.getSize();
            
            //System.out.println("THEN \n" + comb5);
            for(int i = 0; i < nsize; i++){
                
                if(comb.setRow(i).getStatus()>10){
                    
                    comb.setRow(i);
                    double[] means = comb.getFeatures(i);
                    int[]      ids = comb.getLabels(i);
                    
                    resolvedTracks.add(ids, means);
                    resolvedTracks.setRow(resolvedTracks.getSize()-1);
                    resolvedTracks.setProbability(comb.getProbability());
                    resolvedTracks.setStatus(comb.getStatus());
                    
                    int nsize5 = comb5.getSize();
                    for(int i5 = 0; i5 < nsize5; i5++){
                        comb5.setRow(i5);
                        if(comb5.contains(ids)==true) comb5.setStatus(0);
                    }
                }
            }
            //System.out.println("NOW \n" + comb5);
            comb5.analyze();
            
            //System.out.println(comb5);
            int nsize5 = comb5.getSize();
            for(int i5 = 0; i5 < nsize5; i5++){
                comb5.setRow(i5);
                if(comb5.getStatus()>10){
                    double[] means = comb5.getFeatures(i5);
                    int[]      ids = comb5.getLabels(i5);
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
                            resolvedTracks.setProbability(comb5.getProbability());
                            resolvedTracks.setStatus(comb5.getStatus());
                        }
                    }
                }
            }
            /*System.out.println("sector = " + sector);
            System.out.println(comb);
            System.out.println("combination 5 >>> ");
            System.out.println(comb5);
            System.out.println(">");*/
        }
        
        /*System.out.println("resolved tracks");
        System.out.println(resolvedTracks);*/
        /*
        store.getCombinationsFull(comb);        
        int nsize = comb.getSize();
        
        for(int i = 0; i < nsize; i++){
            double[] features = comb.getFeatures(i);
            for(int f = 0; f < features.length; f++) features[f] = features[f]/112.0;
            double[] result   = neurophClassifier.evaluate(features);
            int index = neurophClassifier.getOutputClass(result);
            comb.setRow(i).setProbability(result[index]);
            comb.setRow(i).setStatus(index);
        }
        System.out.println(comb);
        comb.analyze();
                
        System.out.println("AFTER ANALYSIS");
        
        
        System.out.println(comb);
        */
    }
    
    public void evaluate(ClusterCombinations comb){
         int nsize = comb.getSize();
            
            for(int i = 0; i < nsize; i++){
                double[] features = comb.getFeatures(i);
                for(int f = 0; f < features.length; f++) features[f] = features[f]/112.0;
                double[] result   = neurophClassifier.evaluate(features);
                int index = neurophClassifier.getOutputClass(result);
                comb.setRow(i).setProbability(result[index]);
                comb.setRow(i).setStatus(index);
            }
    }
    
    public void evaluate5(ClusterCombinations comb){
        
        int nsize = comb.getSize();
        for(int i = 0; i < nsize; i++){
            
            double[] features = comb.getFeatures(i);


            for(int f = 0; f < features.length; f++) features[f] = features[f]/112.0;
            
            double[] fixedFeatures = neurophFixer.evaluate(features);
            
            /*for(int k = 0; k < features.length; k++) System.out.printf("%5.3f ",features[k]);
            System.out.print(" ===>>> ");
            for(int k = 0; k < fixedFeatures.length; k++) System.out.printf("%5.3f ",fixedFeatures[k]);
            System.out.println();
            */
            for(int k = 0; k < features.length; k++){
                if(features[k]<0.001){
                    features[k] = fixedFeatures[k];
                    comb.setRow(i).setMean(k, features[k]*112);
                }
            }
            double[] result   = neurophClassifier.evaluate(features);
            int index = neurophClassifier.getOutputClass(result);
            comb.setRow(i).setProbability(result[index]);
            comb.setRow(i).setStatus(index);
        }
    }
    
    public void processBankPartial(Bank bank){
      
        store.reset();
        int nrows = bank.getRows();
        for(int i = 0; i < nrows; i++){
            int sector = bank.getInt("sector", i);
            int id     = bank.getInt("id", i);
            int superlayer = bank.getInt("superlayer", i);
            double wire = bank.getFloat("avgWire", i);
            if(sector==1){
                store.add(superlayer-1, id, wire);
            }
        }
        
        ClusterCombinations comb = new ClusterCombinations();
       
        store.getCombinations(comb);        
        int nsize = comb.getSize();
        
        
        for(int i = 0; i < nsize; i++){
            
            double[] features = comb.getFeatures(i);


            for(int f = 0; f < features.length; f++) features[f] = features[f]/112.0;
            
            double[] fixedFeatures = neurophFixer.evaluate(features);
            
            /*for(int k = 0; k < features.length; k++) System.out.printf("%5.3f ",features[k]);
            System.out.print(" ===>>> ");
            for(int k = 0; k < fixedFeatures.length; k++) System.out.printf("%5.3f ",fixedFeatures[k]);
            System.out.println();
            */
            for(int k = 0; k < features.length; k++){
                if(features[k]<0.001){
                    features[k] = fixedFeatures[k];
                    comb.setRow(i).setMean(k, features[k]*112);
                }
            }
            double[] result   = neurophClassifier.evaluate(features);
            int index = neurophClassifier.getOutputClass(result);
            comb.setRow(i).setProbability(result[index]);
            comb.setRow(i).setStatus(index);
        }

        System.out.println(comb);
    }
    
    public void processEvent(Event event){
        
    }
    
    public static void main(String[] args){
        String filename = args[0];
        int    eventNumber = Integer.parseInt(args[1]);
        
        Clas12TrackClassifier classifier = new Clas12TrackClassifier("trackClassifier.nnet","trackFixer.nnet");
        HipoReader reader = new HipoReader();
        reader.open(filename);
        Event event = new Event();
        Bank  cBank = reader.getBank("HitBasedTrkg::HBClusters");
        Bank  tBank = reader.getBank("HitBasedTrkg::HBTracks");
        reader.getEvent(event, eventNumber);
        
        event.read(cBank);
        event.read(tBank);
        cBank.show();
        tBank.show();
        System.out.println("BANK SIZE = " + cBank.getRows());
        
        //classifier.processBankPartial(cBank);
        long then = System.currentTimeMillis();
        for(int i = 0; i < 10; i++){
            classifier.processBank(cBank);
        }
        long now = System.currentTimeMillis();
        System.out.printf("elapset time = %d ms\n",now-then );
        System.out.println("Resolved tracks >>>\n" + classifier.getTracks());
    }
}
