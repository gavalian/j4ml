/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.stats;

import j4ml.clas12.network.Clas12TrackFinder;
import j4ml.clas12.tracking.ClusterCombinations;
import j4ml.clas12.tracking.Track;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.utils.data.TextHistogram;
import org.jlab.jnp.utils.options.OptionParser;

/**
 *
 * @author gavalian
 */
public class Clas12TrackValidation {
    
    Clas12TrackFinder      finder = null;//new Clas12TrackFinder();
    ValidationStatistics    stats = new ValidationStatistics();
    ParticleStats         partStats = new ParticleStats();
    ParticleStats         partStatsAI = new ParticleStats();
    
    Bank     bankTracksTB = null;
    Bank     bankTracksAI = null;
    Bank   bankClustersAI = null;
    Bank   bankClustersTB = null;
    Bank   bankClustersHB = null;
    
    Bank   bankParticles  = null;
    Bank   bankParticlesAI  = null;
    
    boolean showHistograms = true;
    List<AxisCounter>  counterList = new ArrayList<>();
    
    
    public Clas12TrackValidation(){
        finder = Clas12TrackFinder.createEJML();        
        //finder.init(Arrays.asList("etc/ejml/trackClassifierModel.csv",
        //        "etc/ejml/trackFixerModel.csv"));
        finder.init(Arrays.asList("trackClassifier.network",
                "trackFixer.network"));
    }
    
    public Clas12TrackValidation(String envDir){
        finder = Clas12TrackFinder.createEJML(envDir,"etc/ejml/ejmlclas12.network");
        /*finder = Clas12TrackFinder.createEJML();
        finder.setEnvironment(envDir);
        finder.init(Arrays.asList("trackClassifier.network",
                "trackFixer.network"));*/
        
    }
    
    public void showHistogrmas(boolean flag){
        this.showHistograms = flag;
    }
    
    public void init(HipoReader reader){
        
        bankTracksTB   = reader.getBank("TimeBasedTrkg::TBTracks");
        bankTracksAI   = reader.getBank("TimeBasedTrkg::AITracks");
        bankClustersTB = reader.getBank("TimeBasedTrkg::TBClusters");
        bankClustersAI = reader.getBank("TimeBasedTrkg::AIClusters");
        bankClustersHB = reader.getBank("HitBasedTrkg::HBClusters");
        
        //bankParticles = reader.getBank("REC::Particle");
        //bankParticlesAI = reader.getBank("RECAI::Particle");
        
        stats.addMetrics("positive");
        stats.addMetrics("negative");
        stats.addMetrics("positive (6 SL) AI");
        stats.addMetrics("negative (6 SL) AI");
        stats.addMetrics("positive (5 SL)");
        stats.addMetrics("negative (5 SL)");
        
        for(int i = 0 ; i < 12; i++ ) this.counterList.add(new AxisCounter(20,0.0,0.5));
    }
    
    public void processEventAnalyze(Event event){
        
        event.read(bankTracksTB);
        //event.read(bankTracksAI);
        
        event.read(bankClustersTB);
        //event.read(bankClustersAI);
        
        event.read(bankClustersHB);
        
        //finder.process(bankClustersHB);
        
        finder.process(bankClustersTB);
        
        ClusterCombinations result = finder.getResults();

        List<Track>       tracks = Track.read(bankTracksTB,bankClustersTB);
        List<Track>  validTracks = Track.getComplete(tracks);
        
        for(int i = 0; i < validTracks.size(); i++){
            Track t = validTracks.get(i);
            int[] clusters = validTracks.get(i).clusters;
             if(t.charge<0) {
                   counterList.get(0).fill(t.vector.mag());
               } else {
                   counterList.get(1).fill(t.vector.mag());
               }
            if(result.find(clusters)>=0){
               if(t.charge<0) {
                   counterList.get(2).fill(t.vector.mag());
               } else {
                   counterList.get(3).fill(t.vector.mag());
               }
            } else {
                
                float[] features = validTracks.get(i).getFeatures();
                float[]   output = finder.getNetwork().getOutput(features);
                
                if(t.charge<0){
                    if(output[1]>0.8){
                        counterList.get(5).fill(t.vector.mag());
                        if(output[1]>0.9) counterList.get(4).fill(t.vector.mag());
                    } else {
                        if(output[1]<0.5) counterList.get(8).fill(t.vector.mag());
                    }
                    
                   
                } else {
                    
                    if(output[2]>0.8){
                        counterList.get(7).fill(t.vector.mag());
                        if(output[2]>0.9) counterList.get(6).fill(t.vector.mag());
                    } else {
                        if(output[2]<0.5) counterList.get(9).fill(t.vector.mag());
                    }
                }
                System.out.println("------------------------------------");
                System.out.println(" track not found");
                System.out.println(validTracks.get(i));
                for(int k = 0; k < output.length; k++) System.out.printf("%8.5f ",output[k]);
                System.out.println();
                System.out.println(result);
            }
        }
    }
    
    public void printStats(){
        
        int ntracks = counterList.get(0).integral();
        int ntracksMatched = counterList.get(2).integral();
        int ntracksValidated90 = counterList.get(4).integral();
        int ntracksValidated80 = counterList.get(5).integral();
        int unMatched = counterList.get(8).integral();
        System.out.println("+-------------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+");
        System.out.printf("|    negative | %9d | %9d | %9d | %9d | %9d | %9.6f | %9.6f |\n",
                counterList.get(0).integral(),counterList.get(2).integral(),
                counterList.get(4).integral(),counterList.get(5).integral(),
                unMatched,
                ((double) ntracksMatched)/ntracks,
                ((double) (ntracksMatched + ntracksValidated80))/ntracks
        );
        ntracks = counterList.get(1).integral();
        ntracksMatched = counterList.get(3).integral();
        ntracksValidated90 = counterList.get(6).integral();
        ntracksValidated80 = counterList.get(7).integral();
        unMatched = counterList.get(9).integral();
        System.out.printf("|    positive | %9d | %9d | %9d | %9d | %9d | %9.6f | %9.6f |\n",
                counterList.get(1).integral(),counterList.get(3).integral(),
                counterList.get(6).integral(),counterList.get(7).integral(),
                unMatched,
                ((double) ntracksMatched)/ntracks,
                ((double) (ntracksMatched + ntracksValidated80))/ntracks
        );
        System.out.println("+-------------+-----------+-----------+-----------+-----------+-----------+-----------+-----------+");
    }
    
    public void processEvent(Event event){
        
        event.read(bankTracksTB);
        event.read(bankTracksAI);
        
        event.read(bankClustersTB);
        event.read(bankClustersAI);
        event.read(bankClustersHB);
        
        
        event.read(bankParticles);
        event.read(bankParticlesAI);
        
        this.partStats.analyze(bankParticles);
        this.partStatsAI.analyze(bankParticlesAI);
        
        List<Track>  tracksAI = Track.read(bankTracksAI,bankClustersAI);
        List<Track>  validTracksAI = Track.getComplete(tracksAI);
        //System.out.println(" tracks in AI = " + validTracksAI.size());
        for(int i = 0; i < validTracksAI.size(); i++){
            
            //int[] clusters = validTracksAI.get(i).clusters;
            //System.out.println(" charge = " + validTracksAI.get(i).charge + " , mag = " + validTracksAI.get(i).vector.mag());
            if(validTracksAI.get(i).charge>0){
                //System.out.println("filling positive");
                stats.getMetrics("positive (6 SL) AI").fill(validTracksAI.get(i).vector.mag());
                stats.getMetrics("positive (6 SL) AI").addMatched();
            } else {
                //System.out.println("filling negative");
                stats.getMetrics("negative (6 SL) AI").fill(validTracksAI.get(i).vector.mag());
                stats.getMetrics("negative (6 SL) AI").addMatched();
            }
        }
        
        List<Track>  tracks = Track.read(bankTracksTB,bankClustersTB);
                
        List<Track>  validTracks = Track.getComplete(tracks);
        List<Track>  validTracksMissing = Track.getCompleteWithMissing(tracks);
        //bankClustersHB.show();
        finder.process(bankClustersHB);
        //finder.process(bankClustersTB);
        
        ClusterCombinations result = finder.getResults();

        int matches = 0;
        for(int i = 0; i < validTracks.size(); i++){
            int[] clusters = validTracks.get(i).clusters;
            if(validTracks.get(i).charge>0){
                stats.getMetrics("positive").fill(validTracks.get(i).vector.mag());
            } else {
                stats.getMetrics("negative").fill(validTracks.get(i).vector.mag());
            }
            
            if(result.find(clusters)>=0){
                //matches++;
                if(validTracks.get(i).charge>0){
                    stats.getMetrics("positive").addMatched();
                    stats.getMetrics("positive").fillMatched(validTracks.get(i).vector.mag());
                } else {
                    stats.getMetrics("negative").addMatched();
                    stats.getMetrics("negative").fillMatched(validTracks.get(i).vector.mag());
                }
            } else {
                if(validTracks.get(i).charge>0){
                    stats.getMetrics("positive").addUnMatched();
                } else {
                    stats.getMetrics("negative").addUnMatched();
                }
            }
        }
        
        //System.out.println(" missing super-layer tracks : " + validTracksMissing.size());
        for(int i = 0; i < validTracksMissing.size(); i++){
            int[] clusters = validTracksMissing.get(i).clusters;
            if(result.find(clusters,false)>=0){
                //matches++;
                if(validTracksMissing.get(i).charge>0){
                    stats.getMetrics("positive (5 SL)").addMatched();
                } else {
                    stats.getMetrics("negative (5 SL)").addMatched();
                }
            } else {
                if(validTracksMissing.get(i).charge>0){
                    stats.getMetrics("positive (5 SL)").addUnMatched();
                } else {
                    stats.getMetrics("negative (5 SL)").addUnMatched();
                }
            }
        }
        /*System.out.printf(" tracks = %3d , valid = %3d , ai = %3d , matched = %3d\n",
                tracks.size(),validTracks.size(), result.getSize(), matches);
        
        if(validTracks.size()>=2){
            for(Track t : validTracks){
                System.out.println(t);
            }
            for(Track t : validTracksMissing){
                System.out.println(t);
            }
            System.out.println(result);
        }*/        
        
        int nresults = result.getSize();
        for(int i = 0; i < nresults; i++){
            int[] clusters = result.getLabels(i);
            int     status = result.setRow(i).getStatus();
            int nSegments = result.countIds(i);
            if(nSegments==6){                
                if(Track.contains(tracks, clusters)==false){
                    if(status==22){
                        stats.getMetrics("positive").addExcess();
                    } else {
                        stats.getMetrics("negative").addExcess();
                    }
                }
            }
            
            if(nSegments==5){
                if(Track.contains(tracks, clusters)==false){
                    if(status==22){
                        stats.getMetrics("positive (5 SL)").addExcess();
                    } else {
                        stats.getMetrics("negative (5 SL)").addExcess();
                    }
                }
            } 
        }
    }
    
    public void processFile(String filename,int max){
        HipoReader reader = new HipoReader();
        reader.open(filename);
        this.init(reader);
        Event event = new Event();
        int counter = 0;
        while(reader.hasNext()){
            counter++;
            reader.nextEvent(event);
            //processEvent(event);
            //System.out.println("--- # " + counter);
            processEventAnalyze(event);
            if(max>0&&counter>max) break;
        }
        
        System.out.println("FILE: " + filename);
        
        this.printStats();
        /*
        stats.show();
        finder.showStatistics();
        
        System.out.print("CONV : ");this.partStats.show();
        System.out.print("  AI : ");this.partStatsAI.show();
        
        if(showHistograms==true){
            Map<String,Double> map = stats.getMetrics("positive").histogram();
            TextHistogram hist = new TextHistogram();
            System.out.println("--------------- positives --------------------");
            hist.setData(map);
            hist.print();
            Map<String,Double> mapNeg = stats.getMetrics("negative").histogram();
            System.out.println("--------------- negatives --------------------");
            hist.setData(mapNeg);
            hist.print();
        } */       
    }
    
    public void processFile(String filename){
        this.processFile(filename, -1);
    }
    
    public static void main(String[] args){
                        
        OptionParser parser = new OptionParser();
        parser.addOption("-dir","CLAS12DIR", "enviroment directory where network files are located");
        parser.addOption("-n","25000", "number of events to process");
        parser.addOption("-h","false", "show histograms");
        
        
        parser.parse(args);
        
        List<String> inputFiles = parser.getInputList();
        if(inputFiles.size()<1){
            parser.printUsage();
            System.exit(0);
        }
        
        String filename = inputFiles.get(0);
        //String filename = "/Users/gavalian/Work/DataSpace/cooked/rec_clas_005038.evio.00360-00364.hipo";
        int nEvents = parser.getOption("-n").intValue();
        String showHistograms = parser.getOption("-h").stringValue();
        String dir  = parser.getOption("-dir").stringValue();
        Clas12TrackValidation validation = null;

        if(dir.startsWith("null")==false){
            validation = 
                    new Clas12TrackValidation(dir);
        } else {
            validation = 
                    new Clas12TrackValidation();
        }
        if(showHistograms.compareTo("true")==0) validation.showHistogrmas(true);
        validation.processFile(filename,nEvents);
    }
}
