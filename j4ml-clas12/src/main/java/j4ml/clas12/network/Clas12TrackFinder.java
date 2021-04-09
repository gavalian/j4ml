/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.network;


import j4ml.clas12.ejml.EJMLTrackNeuralNetwork;
import j4ml.clas12.tracking.ClusterCombinations;
import j4ml.clas12.tracking.ClusterStore;
import j4ml.clas12.tracking.Track;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class Clas12TrackFinder {
    
    //private NetworkTrackClassifier  classifier = new NetworkTrackClassifier();
    private NeuralNetworkTracking     neuralNetworkTracker = null;
    private ClusterCombinations       result     = new ClusterCombinations();
    
    private ClusterStore                store = new ClusterStore();
    
    private ClusterCombinations         resolvedTracks = new ClusterCombinations();
    private ClusterCombinations         combinations   = new ClusterCombinations();
    private ClusterCombinations         combinations5   = new ClusterCombinations();
    
    private long                        timeInference   = 0L;
    private long                          timeReading   = 0L;
    private long                          timeAnalyzing = 0L;
    private String                         envDirectory = null;
    
    public Clas12TrackFinder(){
        //classifier = new Clas12TrackAINeuroph();
        //classifier.init(Arrays.asList("trackClassifier.nnet","trackFixer.nnet"));
    }
    
    public static Clas12TrackFinder createEJML(){
        Clas12TrackFinder finder = new Clas12TrackFinder();
        finder.setTrackingNetwork(new EJMLTrackNeuralNetwork());
        return finder;
    }
    
    public static Clas12TrackFinder createEJML(String envDir, String file){
        Clas12TrackFinder finder = new Clas12TrackFinder();
        EJMLTrackNeuralNetwork network = new EJMLTrackNeuralNetwork();
        
        Map<String,String>  files = new HashMap<String,String>();
        files.put("classifier", "trackClassifier.network");
        files.put("fixer", "trackFixer.network");
        finder.setEnvironment(envDir);
        
        String path = finder.getPathWithEnvironment(file);
        network.initZip(path, "network/5038", files);
        
        finder.setTrackingNetwork(network);
        return finder;
    }
    
    public void setTrackingNetwork(NeuralNetworkTracking nnt){
        this.neuralNetworkTracker = nnt;
    }
    public void setEnvironment(String envDir){ envDirectory = envDir;}
    
    private String getEnvironment(){
       if(envDirectory==null) return null;
       if(System.getenv(envDirectory)!=null) return System.getenv(envDirectory);
       if(System.getProperty(envDirectory)!=null) return System.getProperty(envDirectory);
       return null;
    }
    
    private String getPathWithEnvironment(String relative){
       if(envDirectory==null) return relative;
       if(System.getenv(envDirectory)!=null) return System.getenv(envDirectory)+"/"+relative;
       if(System.getProperty(envDirectory)!=null) return System.getProperty(envDirectory)+"/"+relative;
       return relative;
    }
    public void init(List<String> networkFiles){
        String topDir = getEnvironment();
        if(topDir==null){
            this.neuralNetworkTracker.init(networkFiles);
        } else {
            List<String>  nf = new ArrayList<>();
            for(int i = 0; i < networkFiles.size(); i++){
                nf.add(topDir+"/"+networkFiles.get(i));
            }
            this.neuralNetworkTracker.init(nf);
        }
    }
    /**
     * Reading the bank and filling structures for given sector.
     * @param bank
     * @param sector 
     */
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
        //combinations5.reset();
        store.getCombinationsFull(combinations);
        store.getCombinations(combinations5);
    }
    
    public void process(Bank bank){

        resolvedTracks.reset();
        List<Integer> statusList = Arrays.asList(11,22);
        long now, then;
        
        for(int sector = 1; sector <= 6; sector++){                    
            then = System.currentTimeMillis();
            readBank(bank,sector);
            now  = System.currentTimeMillis();            
            timeReading += (now-then);
            
            then = System.currentTimeMillis();
            this.neuralNetworkTracker.classify(combinations);
            //classifier.evaluate(combinations);
            now  = System.currentTimeMillis();
            timeInference += (now-then);
            //evaluate();            
            //combinations.analyze();
            //System.out.println("sector - > " + sector);
            //System.out.println(combinations);
            then = System.currentTimeMillis();
            combinations.analyze();
            combinations.copyTo(resolvedTracks, statusList);
            now  = System.currentTimeMillis();
            
            timeAnalyzing += (now-then);
            
            //System.out.println("BEFORE \n" + combinations5.getString(false));
            then = System.currentTimeMillis();
            this.neuralNetworkTracker.fix(combinations5);
            this.neuralNetworkTracker.classify(combinations5);
            //classifier.fix(combinations5);
            //classifier.evaluate(combinations5);
            now  = System.currentTimeMillis();
            timeInference += (now-then);
            
            then = System.currentTimeMillis();
            combinations5.removeMatches(resolvedTracks);
            combinations5.analyze();
            //System.out.println("AFTER \n" + combinations5.getString(true));
            //System.out.println(combinations);
            combinations5.copyTo(resolvedTracks, statusList);
            now  = System.currentTimeMillis();
            timeAnalyzing += (now-then);
            
        }
        //System.out.println("RESOLVED => \n" + resolvedTracks);
    }
    
    public void showStatistics(){
        System.out.printf("[TrackFinder] read = %14d ms, analyze = %14d ms, inference = %14d ms\n",
                timeReading,timeAnalyzing,timeInference);
    }
    
    public ClusterCombinations getResults(){
        return this.resolvedTracks;
    }
    
    public void analyze(){
        
    }
        
    /*private void evaluate(){
        int nsize = combinations.getSize();
        
        for(int i = 0; i < nsize; i++){
            double[] features = combinations.getFeatures(i);
            for(int f = 0; f < features.length; f++) features[f] = features[f]/112.0;
            double[] result   = classifier.evaluate(features);
            int index = classifier.getOutputClass(result);
                combinations.setRow(i).setProbability(result[index]);
                combinations.setRow(i).setStatus(index);
            }
    }*/
    
    
    public static void debug(String filename, int nevent){
        
        Clas12TrackFinder finder = Clas12TrackFinder.createEJML("CLAS12DIR","etc/ejml/ejmlclas12.network");
        /*finder.init(Arrays.asList(
                //"etc/ejml/trackClassifierModel.csv",
                "etc/ejml/trackClassifier.network",
                "etc/ejml/trackFixer.network"
                //"etc/ejml/trackFixerModel.csv"
        ));*/
        HipoReader reader = new HipoReader();
        reader.setDebugMode(0);
        reader.open(filename);
        Event event = new Event();
        
        Bank   tbTracks = reader.getBank("TimeBasedTrkg::TBTracks");
        Bank tbClusters = reader.getBank("TimeBasedTrkg::TBClusters");
        Bank hbClusters = reader.getBank("HitBasedTrkg::HBClusters");
        
        reader.getEvent(event, nevent);
        
        event.read(tbTracks);
        event.read(tbClusters);
        event.read(hbClusters);
        
        List<Track>  tracks = Track.read(tbTracks,tbClusters);
        System.out.println("\n\n********** conventionsl tracks ****************************\n");
        for(Track t: tracks){
            //if(t.complete()==true){
                System.out.println(t);                
        }
        
        finder.process(hbClusters);
        System.out.println("\n############ ai predictions ###################################");
        System.out.println(finder.getResults().toString());
        System.out.println("\n");
    }
    
    public static void main(String[] args){
        
        String filename = args[0];
        Integer nevent  = Integer.parseInt(args[1]);
        
        Clas12TrackFinder.debug(filename, nevent);
        
        //Clas12TrackFinder finder = new Clas12TrackFinder();
        /*
        Clas12TrackFinder finder = Clas12TrackFinder.createEJML();
        
        finder.init(Arrays.asList("etc/ejml/trackClassifierModel.csv",
                "etc/ejml/trackFixerModel.csv"));
        
        String filename = "/Users/gavalian/Work/DataSpace/cooked/rec_clas_005038.evio.00360-00364.hipo";                
        
        HipoReader reader = new HipoReader();
        reader.open(filename);
        Event event = new Event();
        
        Bank   tbTracks = reader.getBank("TimeBasedTrkg::TBTracks");
        Bank tbClusters = reader.getBank("TimeBasedTrkg::TBClusters");
        
        long totalTime = 0L;
        
        int counter = 0;
        
        while(reader.hasNext()){
            counter++;
            reader.nextEvent(event);
            
            event.read(tbTracks);
            event.read(tbClusters);
            
            List<Track>  tracks = Track.read(tbTracks,tbClusters);
            
            boolean process = false;
            if(tracks.size()>1){
                System.out.println("event # " + counter + "  track count = " + tracks.size());
                for(Track t: tracks){
                    //if(t.complete()==true){
                    if(t.clusterCount()==5){
                        System.out.println(t);
                        process = true;
                    }
                }
                long then = System.currentTimeMillis();
                if(process == true){
                    finder.process(tbClusters);
                    System.out.println(finder.getResults());
                }
                long now = System.currentTimeMillis();
                totalTime += (now-then);
                
            }
            
        }
        
        double time = ( (double) totalTime )/counter;
        System.out.printf("evaluation time = %.4f ms/event\n",time);
        */
    }
}
