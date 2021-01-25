/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.extract;

import j4ml.clas12.track.DriftChamberSector;
import j4ml.data.clas12.DataExtractor;
import j4ml.data.clas12.TrackObject;
import j4ml.data.clas12.TrackObject.WireHit;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;
import org.jlab.jnp.hipo4.operations.BankExpression;
import org.jlab.jnp.hipo4.operations.BankIterator;

/**
 *
 * @author gavalian
 */
public class RawDataDenoise extends DataExtractor {
    
    Bank rawTDC = null;
    List<BankExpression> exp = new ArrayList<>();
    BankIterator   iter = new BankIterator(5000);
    TrackObject    track = new TrackObject();
    
    public RawDataDenoise(String filename){
        output(filename);
    }
    
    @Override
    public void init(HipoChain chain){
        rawTDC = new Bank(chain.getSchemaFactory().getSchema("DC::tdc"));
        exp.clear();
        exp.add( new BankExpression("sector==1",rawTDC));
        exp.add( new BankExpression("sector==2",rawTDC));
        exp.add( new BankExpression("sector==3",rawTDC));
        exp.add( new BankExpression("sector==4",rawTDC));
        exp.add( new BankExpression("sector==5",rawTDC));
        exp.add( new BankExpression("sector==6",rawTDC));
        track.init(chain);
        
        this.openChannel(1, "dc_denoise_one_track.lsvm");
        this.openChannel(2, "dc_denoise_two_track.lsvm");
        this.openChannel(3, "dc_denoise_three_track.lsvm");
        
        open();
    }
    
    
    public DriftChamberSector  createSectorData(List<WireHit> hits){
        DriftChamberSector sector = new DriftChamberSector();
        for(int i = 0; i < hits.size(); i++){
            sector.set(hits.get(i).getLayer()-1,hits.get(i).getWire()-1 , 1);
        }
        return sector;
    }
    
     public DriftChamberSector  createSectorData(TrackObject obj, int sector){
         List<Integer>  list = obj.getTracksList(sector);
         
        DriftChamberSector sec = new DriftChamberSector();
        
        List<Integer>  clusters = new ArrayList<>();
        
        for(int i = 0; i < list.size(); i++){
            List<Integer> c = obj.getTrackClusters(list.get(i));
            clusters.addAll(c);
        }
        
        List<WireHit>  hits = obj.getWireHits(clusters);
        for(int i = 0; i < hits.size(); i++){
            sec.set(hits.get(i).getLayer()-1,hits.get(i).getWire()-1 , 1);
        }
        return sec;
    }
    
    @Override
    public void process(Event event){
        
        event.read(rawTDC);
        
        for(int s = 0; s < 6; s++){
         
            exp.get(s).getIterator(rawTDC, iter);        
            DriftChamberSector sector = new DriftChamberSector();        
            sector.read(rawTDC, iter);
        
            track.read(event);
            
            List<Integer>  list = track.getTracksList(s+1);
            
            if(list.size()>0){
                
                int c1 = sector.getSuperLayerCount(0);
                int c2 = sector.getSuperLayerCount(1);
            
                //System.out.printf("# of tracks = %4d occupancy %6d %6d\n", list.size(),c1,c2);
                
                List<Integer>  clusters = track.getTrackClusters(0);
                List<WireHit>  hits     = track.getWireHits(clusters);
                DriftChamberSector  trackSector = this.createSectorData(track,s+1);
                
                String positive = String.format("%d %5d %5d %s", list.size(),c1,c2,trackSector.getDataString());
                String negative = String.format("%d %5d %5d %s", 0,c1,c2,sector.getDataString());
              
               // System.out.println(positive);
               // System.out.println(negative);
                //this.outputLines.clear();
                //outputLines.add(positive);
                //outputLines.add(negative);
                
                if(list.size()==1){
                    this.writeChannel(1, positive);
                    this.writeChannel(1, negative);
                }
                if(list.size()==2){
                    this.writeChannel(2, positive);
                    this.writeChannel(2, negative);
                }
                if(list.size()==3){
                    this.writeChannel(3, positive);
                    this.writeChannel(3, negative);
                }
                //System.out.println(">>>>>>>>>>>");
                //System.out.println(trackSector + ">>>>>>>>>>\n");
                write();
            //if(c1>120) 
            //System.out.println(sector);
        }
        //this.outputLines.clear();
        //outputLines.add("1 " + outputMapClusters);
        //outputLines.add("0 " + outputMapRaw);        
        //System.out.println("1 -> " + outputMapClusters);
        //System.out.println("0 -> " + outputMapRaw);
        //write();
        }
    }
    
}
