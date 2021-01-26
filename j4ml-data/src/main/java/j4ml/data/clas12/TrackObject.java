/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.clas12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<Integer,Integer> clustersMap = new HashMap<>();
    
   public void init(HipoChain chain){
       trackBank   = new Bank(chain.getSchemaFactory().getSchema(trackBankName));
       clusterBank = new Bank(chain.getSchemaFactory().getSchema(clusterBankName));
       tdcBank     = new Bank(chain.getSchemaFactory().getSchema(tdcBankName));
   }
   
   public void read(Event event){
       event.read(trackBank);
       event.read(clusterBank);
       event.read(tdcBank);
       clustersMap.clear();
       for(int i = 0; i < clusterBank.getRows(); i++){
           int id = clusterBank.getInt("id", i);
           clustersMap.put(id, i);
       }
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
   
   public String getTrackDescriptionString(int index){
       StringBuilder str = new StringBuilder();
       str.append(String.format("%d,",trackBank.getInt("q", index)));
       str.append(String.format("%d,",trackBank.getInt("sector", index)));
       str.append(String.format("%.4f,",trackBank.getFloat("chi2", index)/trackBank.getInt("ndf", index)));
       //str.append(String.format("%d,",trackBank.getInt("ndf", index)));
       double px = trackBank.getFloat("p0_x", index);
       double py = trackBank.getFloat("p0_y", index);
       double pz = trackBank.getFloat("p0_z", index);
       str.append(String.format("%.4f,",Math.sqrt(px*px+py*py+pz*pz)));
       str.append(String.format("%.4f,",Math.acos(pz/Math.sqrt(px*px+py*py+pz*pz))));
       str.append(String.format("%.4f,",Math.atan2(px,py)));
       str.append(String.format("%.4f",trackBank.getFloat("Vtx0_z", index)));
       
       return str.toString();
   }
   
   public String getTrackClustersString(int index){
       List<Integer> list = this.getTrackClusters(index);
       StringBuilder str = new StringBuilder();
       for(int i = 0; i < 6; i++){
           int row = clustersMap.get(list.get(i));
           double mean = clusterBank.getFloat("avgWire", row);
           str.append(String.format("%.4f,", mean));
       }
       for(int i = 0; i < 6; i++){
           int row = clustersMap.get(list.get(i));
           double slope = clusterBank.getFloat("fitSlope", row);
           str.append(String.format("%.4f,", slope));
       }
       str.append("0");
       return str.toString();
   }
   
   public List<Integer>  getTracksList(int sector){
       int ntracks = trackBank.getRows();
       List<Integer> index = new ArrayList<Integer>();
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
                       index.add(i);
                   }
               }
           }
       }
       return index;
   }
   
   public List<Integer>  getTrackClusters(int index){
       
       int cid1 = trackBank.getInt("Cluster1_ID",index);
       int cid2 = trackBank.getInt("Cluster2_ID",index);
       int cid3 = trackBank.getInt("Cluster3_ID",index);
       int cid4 = trackBank.getInt("Cluster4_ID",index);
       int cid5 = trackBank.getInt("Cluster5_ID",index);
       int cid6 = trackBank.getInt("Cluster6_ID",index);
       List<Integer> list = new ArrayList<Integer>();
       list.addAll(Arrays.asList(cid1,cid2,cid3,cid4,cid5,cid6));
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
                       list.addAll(Arrays.asList(cid1,cid2,cid3,cid4,cid5,cid6));
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
       
       Map<Integer,WireHit> wireMap = new HashMap<>();
       
       
       for(int i = 0; i < nrows; i++){
           int id = clusterBank.getInt("id", i);
           //System.out.println(" id = " + id + "  contains = " + );
           if(clusterList.contains(id)==true){
               for(int k = 1; k <= 12; k++){
                   int index = clusterBank.getInt("Hit"+k+"_ID",i);
                   if(index>=0){
                       WireHit hit = getWireHit(index-1);
                       //list.add(hit);
                       wireMap.put(hit.getHash(), hit);
                   }
               }
           }
           
       }
       List<WireHit> list = new ArrayList<WireHit>();
       for(Map.Entry<Integer,WireHit> entry : wireMap.entrySet()){
           list.add(entry.getValue());
       }
       return list;
   }
   
   public static class WireHit implements Comparable<WireHit> {
       
       int sector = 0;
       int layer  = 0;
       int wire   = 0;
       public WireHit(int __s, int __l, int __c){
           sector = __s; layer = __l; wire = __c;
       }
       
       public int getLayer(){return layer;}
       public int getWire(){return wire;}
       
       public int getHash(){
           return (wire-1)+(layer-1)*112;
       }
       
       public String idString(){
           return String.format("%d:1.0", (wire-1)+(layer-1)*112);
       }

        @Override
        public int compareTo(WireHit o) {
            Integer lw = (wire-1)+(layer-1)*112;
            Integer gw = (o.wire-1)+(o.layer-1)*112;
            return lw.compareTo(gw);
        }
   }
}
