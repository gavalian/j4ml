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
public class FileTrackCompare {
    
    List<AxisCounter>  counterList = new ArrayList<>();
    
    
    public FileTrackCompare() {
        for(int i = 0 ; i < 12; i++ ) this.counterList.add(new AxisCounter(20,0.0,0.5));
    }
    
    public void processFile(String reference, String file, int nmax){
        HipoReader readerRef = new HipoReader();
        readerRef.open(reference);
        
        HipoReader reader = new HipoReader();
        reader.open(file);
        
        Event event = new Event();
        Bank  bankTrk = readerRef.getBank("TimeBasedTrkg::AITracks");
        Bank  bankCls = reader.getBank("TimeBasedTrkg::AIClusters");
        
        for(int i = 0; i < nmax; i++){
            readerRef.getEvent(event, i);
            event.read(bankCls);
            event.read(bankTrk);
            
            List<Track>   tracksRef = Track.read(bankTrk,bankCls);
            
            reader.getEvent(event, i);
            event.read(bankCls);
            event.read(bankTrk);
            
            List<Track>   tracks = Track.read(bankTrk,bankCls);
            
            List<Track>   completeRef = Track.getComplete(tracksRef);
            //System.out.printf(" --> %d %d\n", completeRef.size(),tracks.size());
            for(int k = 0; k < completeRef.size(); k++){
                
                Track   trk = completeRef.get(k);
                int  mindex = trk.findMatch(tracks);
                
                //System.out.printf("--- track %5d, matched = %5d\n",k,mindex);
                //System.out.println(trk);
                //System.out.println(tracks.get(mindex));
                if(trk.charge<0){
                    counterList.get(0).fill(trk.vector.mag());
                    int index = trk.findMatch(tracks);
                    if(index>=0){
                        Track recovered = tracks.get(index);
                        if(recovered.complete()==true){
                            counterList.get(1).fill(trk.vector.mag());
                        } else {
                            counterList.get(2).fill(trk.vector.mag());
                        }
                    }
                } else {
                    counterList.get(3).fill(trk.vector.mag());
                    int index = trk.findMatch(tracks);
                    if(index>=0){
                        Track recovered = tracks.get(index);
                        if(recovered.complete()==true){
                            counterList.get(4).fill(trk.vector.mag());
                        } else {
                            counterList.get(5).fill(trk.vector.mag());
                        }
                    }
                }
            }
        }
        System.out.printf("%8d %8d %8d %8d %8d %8d %8d %8d\n",
                counterList.get(0).integral(),
                counterList.get(1).integral()+
                counterList.get(2).integral(),
                counterList.get(1).integral(),
                counterList.get(2).integral(),
                counterList.get(3).integral(),
                counterList.get(4).integral()+
                counterList.get(5).integral(),
                counterList.get(4).integral(),
                counterList.get(5).integral()
                );
    }
    
    public static void main(String[] args){
        String  ref = args[0];
        String file = args[1];
        int     max = Integer.parseInt(args[2]);
        
        
        /*String  ref = "/Users/gavalian/Work/DataSpace/compare/out_5na.hipo";
        String  file = "/Users/gavalian/Work/DataSpace/compare/out_45na.hipo";
        int     max = 80000;
        */
        FileTrackCompare compare = new FileTrackCompare();
        compare.processFile(ref, file, max);
    }
}
