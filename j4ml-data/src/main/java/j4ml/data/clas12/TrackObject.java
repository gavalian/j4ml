/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.clas12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;

/**
 *
 * @author gavalian
 */
public class TrackObject {
    
    private String trackBankName = "TimeBasedTrkg::TBTracks";
    private String clusterBankName = "HitBasedTrkg::HBClusters";
    private String tdcBankName = "DC::tdc";
    
    private Bank   trackBank  = null;
    private Bank   clusterBank = null;
    private Bank   tdcBank     = null;
    
   public void init(HipoChain chain){
       trackBank   = new Bank(chain.getSchemaFactory().getSchema(trackBankName));
       clusterBank = new Bank(chain.getSchemaFactory().getSchema(clusterBankName));
       tdcBank     = new Bank(chain.getSchemaFactory().getSchema(tdcBankName));
   }
   
   public void read(Event event){
       event.read(trackBank);
       event.read(clusterBank);
       event.read(tdcBank);
   }
   
   public List<Integer>  clustersForSector(int sector){
       int nrows = clusterBank.getRows();
       List<Integer> list = new ArrayList<>();
       for(int i = 0; i < nrows; i++){
           int sec = clusterBank.getInt("sector",i);
           if(sec==sector){
               int id = clusterBank.getInt("id", i);
               list.add(id);
           }
       }
       return list;
   }
   
   public List<Integer>  trackClustersForSector(int sector){
       int ntracks = trackBank.getRows();
       List<Integer> list = new ArrayList<Integer>();
       for(int i = 0; i < ntracks; i++){
           int sec = trackBank.getInt("sector", i);
           if(sec == sector){
               double chi2 = trackBank.getFloat("chi2", i);
               double vz   = trackBank.getFloat("Vtx0_z", i);
               if(chi2<400.0&&vz>-25&&vz<5.0){
                   int cid1 = trackBank.getInt("Cluster1_ID",i);
                   int cid2 = trackBank.getInt("Cluster2_ID",i);
                   int cid3 = trackBank.getInt("Cluster3_ID",i);
                   int cid4 = trackBank.getInt("Cluster4_ID",i);
                   int cid5 = trackBank.getInt("Cluster5_ID",i);
                   int cid6 = trackBank.getInt("Cluster6_ID",i);
                   if(cid1>0&&cid2>0&&cid3>0&&cid4>0&&cid5>0&&cid6>0){
                       list.addAll(Arrays.asList(cid1,cid2,cid3,cid4,cid5,cid5));
                   }
               }
           }
       }
       return list;
   }
   
   public WireHit        getWireHit(int index){
       int nrows = tdcBank.getRows();
       if(index>=nrows){
           System.out.println("[TrackObject] error: in index " + index + ", max is " + nrows);
           return null;
       }
       int sector = tdcBank.getInt("sector", index);
       int layer = tdcBank.getInt("layer", index);
       int component = tdcBank.getInt("component", index);
       WireHit hit = new WireHit(sector,layer,component);
       return hit;
   }
   
   public List<WireHit>  getWireHits(List<Integer> clusterList){
       int nrows = clusterBank.getRows();
       List<WireHit> list = new ArrayList<WireHit>();
       for(int i = 0; i < nrows; i++){
           int id = clusterBank.getInt("id", i);
           //System.out.println(" id = " + id + "  contains = " + );
           if(clusterList.contains(id)==true){
               for(int k = 1; k <= 12; k++){
                   int index = clusterBank.getInt("Hit"+k+"_ID",i);
                   if(index>=0){
                       WireHit hit = getWireHit(index-1);
                       list.add(hit);
                   }
               }
           }
           
       }
       return list;
   }
   
   public static class WireHit {
       int sector = 0;
       int layer  = 0;
       int wire   = 0;
       public WireHit(int __s, int __l, int __c){
           sector = __s; layer = __l; wire = __c;
       }
       public String idString(){
           return String.format("%d:1.0", (wire-1)+(layer-1)*112);
       }
   }
}