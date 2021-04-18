/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.stats;

import j4ml.clas12.tracking.Track;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class LuminocityScan {
    
    public static List<Track> filter(List<Track> tracks){
        List<Track>  filtered = new ArrayList<>();
        for(int i = 0; i < tracks.size(); i++){
            Track t = tracks.get(i);
            if(t.vertex.z()>-15&&t.vertex.z()<5&&t.chi2<10.0){
                if(t.complete()==true) filtered.add(t);
            }
        }
        return filtered;
    }
    
    public static boolean findCluster(Bank bank, int sector, int superlayer, double position, double distance){
        int nrows = bank.getRows();
        for(int i = 0; i < nrows; i++){
            int sec = bank.getInt("sector", i);
            int sup = bank.getInt("superlayer", i);
            double avg = bank.getFloat("avgWire", i);
            if(sec==sector&&sup==superlayer){
                if(Math.abs(avg-position)<distance) return true;
            }
        }
        return false;
    }
    
    public static boolean isPresent(Track track, Bank bank, double distance){
        int sector = track.sector;
        for(int i = 0; i < 6; i++){
            double mean = track.means[i];
            if(LuminocityScan.findCluster(bank, sector, i+1, mean, distance)==false) return false;
        }
        return true;
    }
    
    public static boolean compareTracks(Track trk, Track dest, double distance){
        for(int i = 0; i < 6; i++){
            double wire = trk.means[i];
            double wire2 = dest.means[i];
            if(Math.abs(wire-wire2)>distance) return false;
        }
        return true;
    }
    
    public static boolean findTrack(Track trk, List<Track> tracks, double distance){
        for(int i = 0; i < tracks.size(); i++){
            if(LuminocityScan.compareTracks(trk, tracks.get(i), distance)==true) return true;
        }
        return false;
    }
    
    public static boolean compareTrackParameters(Track trk, Track dest){
        double ratio = (dest.vector.mag()-trk.vector.mag())/trk.vector.mag();
        if(Math.abs(ratio)>0.05) return false;
        return true;
    }
    
    public static int findTrackParameters(Track trk, List<Track> tracks){
        for(int i = 0; i < tracks.size(); i++){
            if(LuminocityScan.compareTrackParameters(trk, tracks.get(i))==true) return 1;
        }
        return 0;
    }
    
    public static void scan(String file1, String file2){
        HipoReader reader1 = new HipoReader();
        HipoReader reader2 = new HipoReader();
        
        reader1.open(file1);
        reader2.open(file2);
        

        Bank hbtr = reader1.getBank("HitBasedTrkg::HBTracks");
        Bank hbcl = reader1.getBank("HitBasedTrkg::HBClusters");
        
        int nentries = reader1.getEventCount();
        Event event = new Event();
        
        for(int i = 0; i < nentries; i++){
            reader1.getEvent(event, i);
            event.read(hbcl);
            event.read(hbtr);
            
            List<Track>  tracks1 = Track.read(hbtr,hbcl);
            List<Track>  reference = LuminocityScan.filter(tracks1);
            
            reader2.getEvent(event, i);
            event.read(hbcl);
            event.read(hbtr);
            List<Track>  tracks2 = Track.read(hbtr,hbcl);
            
            //System.out.println("--> new event");
            for(Track track : reference){
                //System.out.println(track);
                boolean find = LuminocityScan.findTrack(track, tracks2, 3.5);
                boolean isPresent = LuminocityScan.isPresent(track, hbcl,3.5);
                
                int    statusFind = 1;
                int statusPresent = 1;
                
                if(find==false) statusFind = 0;
                if(isPresent==false) statusPresent = 0;
                
                int status = LuminocityScan.findTrackParameters(track, tracks2);
                System.out.printf("%8.4f %3d %3d %3d\n", track.vector.mag(), statusFind, 
                        statusPresent, status);
                //        "result = " + find + "  present = " + isPresent);
                
                /*if(find==false&&isPresent==true){
                    System.out.println(track);
                    for(Track t : tracks2){
                        System.out.println("\t" + t);
                    }
                    
                }*/
            }
        }
    }
    
    public static void main(String[] args){
        String file = "/Users/gavalian/Work/DataSpace/luminocity/cooked_005418_5na.hipo";
        String file2 = "/Users/gavalian/Work/DataSpace/luminocity/cooked_005418_90na.hipo";
        if(args.length>0){
            file = args[0];
        }
        if(args.length>1){
            file2 = args[1];
        }
        LuminocityScan.scan(file, file2);
    }
}
