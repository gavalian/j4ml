/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.clas12;

import j4ml.data.clas12.TrackObject.WireHit;
import java.util.Arrays;
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
    }
    
    public String getWireString(List<WireHit> list){
        StringBuilder str = new StringBuilder();
        for(WireHit hit : list){
            str.append(" ").append(hit.idString());
        }
        return str.toString();
    }
    
    @Override
    public void process(Event event){   
        int counter = 0;
        int nevents = 0;
        trackObject.read(event);
        
        for(int sector = 4; sector <= 6; sector++){
            
            List<Integer> tracks = trackObject.trackClustersForSector(sector);
            List<Integer> clusters = trackObject.clustersForSector(sector);
         
            
            List<WireHit> trackHits = trackObject.getWireHits(tracks);
            List<WireHit> clusterHits = trackObject.getWireHits(clusters);
            
            if(tracks.size()>7){
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
        ChainDataExtractor ce = new ChainDataExtractor(Arrays.asList(filename));
        MultiTrackExtraction ext = new MultiTrackExtraction("driftchamber_multitracks.lsvm");
        ce.addExtractor(ext);
        //ce.setLimit(1000);
        ce.process();
    }
}
