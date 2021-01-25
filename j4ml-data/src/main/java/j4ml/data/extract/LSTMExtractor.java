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
public class LSTMExtractor extends DataExtractor {
    
    Bank rawTDC = null;
    List<BankExpression> exp = new ArrayList<>();
    BankIterator   iter = new BankIterator(5000);
    TrackObject    track = new TrackObject();
    
    public LSTMExtractor(String filename){
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
                   
        
            track.read(event);
            
            List<Integer>  list = track.getTracksList(s+1);
            
            if(list.size()>0){
                List<Integer> clusters = track.getTrackClusters(0);
                List<WireHit> hits     = track.getWireHits(clusters);
                
                DriftChamberSector sector = this.createSectorData(hits);
                DriftChamberSector sectorFixed = sector.getFixed();
                //System.out.println( ">>>>>>>>>>\n");                
                //System.out.println( sector + " \n>>> AFTER FIX ");
                //System.out.println(sectorFixed);
                //System.out.println(sectorFixed.getTrackSeries());
                 this.outputLines.clear();
                outputLines.add(sectorFixed.getTrackSeries());
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
