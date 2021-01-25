/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.clas12;

import j4ml.data.clas12.TrackObject.WireHit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;
import org.jlab.jnp.hipo4.operations.BankIterator;
import org.jlab.jnp.hipo4.operations.BankSelector;

/**
 *
 * @author gavalian
 */
public class MultiTrackExtraction extends DataExtractor {
    TrackObject trackObject = new TrackObject();
    
    public MultiTrackExtraction(String filename){
        output(filename);
    }
    
    @Override
    public void init(HipoChain chain){
        
        trackObject.init(chain);
        
        this.open();
        this.openChannel(1, "dc_rawdata_tracks.graph");
    }
    
    public String getWireString(List<WireHit> list){
        StringBuilder str = new StringBuilder();
        for(WireHit hit : list){
            str.append(" ").append(hit.idString());
        }
        return str.toString();
    }
    public String getHitsString(List<WireHit> list){
        StringBuilder str = new StringBuilder();
        for(int i =0; i < list.size(); i++){
            WireHit hit = list.get(i);
            str.append(String.format(" %.1f:%.1f", (hit.sector-1)*112.0+hit.wire,(float) hit.layer+24));
        }
        return str.toString();
    }
    
    public void processFull(Event event){
        
    }
    
    @Override
    public void process(Event event){   
        int counter = 0;
        int nevents = 0;
        trackObject.read(event);
        
        
        /*
        List<Integer> tracksALL   = new ArrayList<>();
        List<Integer> clustersALL = new ArrayList<>();
        for(int i = 1; i <= 6; i++){
            tracksALL.addAll(trackObject.trackClustersForSector(i));
            clustersALL.addAll(trackObject.clustersForSector(i));            
        }
        
        List<WireHit>   trackHitsALL = trackObject.getWireHits(tracksALL);
        List<WireHit> clusterHitsALL = trackObject.getWireHits(clustersALL);
        
        String tString = getHitsString(trackHitsALL);
        String cString = getHitsString(clusterHitsALL);
        //System.out.println("-------> multiplicity : " + trackHitsALL.size());
        if(trackHitsALL.size()>70&&clusterHitsALL.size()>550){
            this.writeChannel(1,"1 " + tString);
            this.writeChannel(1,"0 " + cString);
            System.out.println("-------> multiplicity : " + trackHitsALL.size() + " " + clusterHitsALL.size());
        }*/
        
        for(int sector = 1; sector <= 6; sector++){
           
            List<Integer> tracks = trackObject.trackClustersForSector(sector);
            List<Integer> clusters = trackObject.clustersForSector(sector);         
            
            List<WireHit> trackHits = trackObject.getWireHits(tracks);
            List<WireHit> clusterHits = trackObject.getWireHits(clusters);
            Collections.sort(trackHits);
            Collections.sort(clusterHits);
            
            if(tracks.size()>7){
                
                /*System.out.println("BEFORE");
                for(int i = 0; i < trackHits.size(); i++){
                    System.out.printf("%s ",trackHits.get(i).idString());
                }
                System.out.println("");
                Collections.sort(trackHits);
                System.out.println("AFTER");
                for(int i = 0; i < trackHits.size(); i++){
                    System.out.printf("%s ",trackHits.get(i).idString());
                }
                System.out.println("");
                */
                /*System.out.printf(" TRACKS CLUSTERS = %5d / %5d, ALL CLUSTERS = %5d / %5d\n",
                tracks.size(),trackHits.size(),
                clusters.size(),clusterHits.size());*/
                String trackString = getWireString(trackHits);
                String clusterString = getWireString(clusterHits);
                //System.out.println(trackString);
                outputLines.clear();
                outputLines.add("1 " + trackString);
                outputLines.add("0 " + clusterString);
                write();
            }
        }
    }
    
    public static void main(String[] args){
        //String filename = "/Users/gavalian/Work/DataSpace/autoencoder/test_reduced.hipo";
        String filename = "/Users/gavalian/Work/DataSpace/autoencoder/test_filtered.hipo";
        int nfiles = args.length;
        List<String> list = new ArrayList<>();
        for(int i = 0; i < nfiles; i++) list.add(args[i]);
        
        ChainDataExtractor ce = new ChainDataExtractor(list);
        MultiTrackExtraction ext = new MultiTrackExtraction("driftchamber_multitracks.lsvm");
        ce.addExtractor(ext);
        //ce.setLimit(100);
        ce.process();
    }
}
