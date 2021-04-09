/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.process;

import j4ml.clas12.tracking.ClusterCombinations;
import j4ml.clas12.tracking.ClusterStore;
import j4ml.clas12.tracking.Track;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Node;
import org.jlab.jnp.hipo4.io.HipoChain;
import org.jlab.jnp.hipo4.io.HipoWriter;
import org.jlab.jnp.hipo4.io.HipoWriterSorted;

/**
 *
 * @author gavalian
 */
public class TrackDataExtractor {
    
    public static List<Node> getNode(Track trk){
        
        List<Node> nodeList = new ArrayList<Node>();
        short[] desc = new short[]{(short) trk.sector,(short) trk.charge};
        
        nodeList.add(new Node(1001,1,desc));
        nodeList.add(new Node(1001,2,new float[]{ (float) trk.chi2}));
        nodeList.add(new Node(1001,3,new short[]{
            (short) trk.clusters[0],
            (short) trk.clusters[1],
            (short) trk.clusters[2],
            (short) trk.clusters[3],
            (short) trk.clusters[4],
            (short) trk.clusters[5]
        }));
        
        nodeList.add(new Node(1001,4,new float[] {
            (float) trk.means[0],
            (float) trk.means[1],
            (float) trk.means[2],
            (float) trk.means[3],
            (float) trk.means[4],
            (float) trk.means[5]
        }));
        nodeList.add(new Node(1001,5,new float[]{
            (float) trk.slopes[0],
            (float) trk.slopes[1],
            (float) trk.slopes[2],
            (float) trk.slopes[3],
            (float) trk.slopes[4],
            (float) trk.slopes[5]
        }));
        
        nodeList.add(new Node(1001,6, new float[]{ (float) trk.vector.x(), (float)trk.vector.y(), (float)trk.vector.z()}));
        nodeList.add(new Node(1001,7, new float[]{ (float)trk.vertex.x(),(float) trk.vertex.y(),(float) trk.vertex.z()}));
                
        
        return nodeList;
    }
    
    
    public void export(){
        //TextFileWriter writer 
    }
    
    public void readStore(){
        
    }
    
    public static int getTrackBin(Track t){
        double p = t.vector.mag();
        int bin = -1;
        if(p>0.0&&p<10.0){
            bin = (int) (p/0.5);
        }
        return bin;
    }
    
    public static void read(ClusterStore store, Bank bank, int sector){
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
    }
    
    public static void main(String[] args){
        
        List<String> filesList = new ArrayList<>();
        
        String filename = "/Users/gavalian/Work/DataSpace/raw/out_ai_005038.00165-00169.hipo";
        for(int i = 0; i < args.length; i++){
            filesList.add(args[i]);
        }
        
        HipoChain chain = new HipoChain();
        
        //chain.addFile(filename);
        chain.addFiles(filesList);
        chain.open();
        
        Bank tBank = chain.getBank("TimeBasedTrkg::TBTracks");
        Bank cBank = chain.getBank("TimeBasedTrkg::TBClusters");
        Bank hBank = chain.getBank("HitBasedTrkg::HBClusters");
        
        Event event = new Event();
        Event outEvent = new Event();
        
        HipoWriterSorted writer = new HipoWriterSorted();
        
        writer.open("extract_output.hipo");
        
        int counter = 0;
        //for(int i = 0; i < 10000; i++){
        // while(chain.hasNext()&&counter<1000){  
        ClusterStore store = new ClusterStore();
        ClusterCombinations comb = new ClusterCombinations();
        
        while(chain.hasNext()){  

              counter++;
              chain.nextEvent(event);
              event.read(cBank);
              event.read(tBank);
              event.read(hBank);
              
              List<Track>  trkList = Track.read(tBank,cBank);
              store.reset();
              
              for(Track t : trkList){                  
                  if(t.complete()==true&&t.chi2<10.0&&t.vertex.z()>-25.0&&t.vertex.z()<35.0){
                      //System.out.println(t);
                      List<Node> nodes = TrackDataExtractor.getNode(t);
                      //System.out.println(t);
                      //System.out.println("n nodes = " + nodes.size());
                      outEvent.reset();
                      int bin = TrackDataExtractor.getTrackBin(t);
                      if(bin>=0&&bin<20){
                          TrackDataExtractor.read(store, hBank, t.sector);
                          store.getCombinationsFull(comb);
                          //System.out.println(t);
                          //System.out.println("size = " + comb.getSize());
                          int index = comb.bestMatch(t.clusters, t.means);
                          if(index>=0) {
                              double distance = comb.distance(index, t.means);
                              if(distance<25.0&&distance>2.0){
                                  //System.out.printf("distance = %12.6f\n",comb.distance(index, t.means));
                                  //System.out.println(comb.getRowString(index));
                                  double[] means = comb.getFeatures(index);
                                  Node negativeNode = new Node(1001,8, 
                                          new float[]{ 
                                              (float)means[0],
                                              (float)means[1],
                                              (float)means[2],
                                              (float)means[3],
                                              (float)means[4],
                                              (float) means[5]}
                                  );
                                  outEvent.setEventTag(bin+1);
                                  for(Node n : nodes) outEvent.write(n);
                                  outEvent.write(negativeNode);
                                  //writer.addEvent(outEvent,bin+1);
                                  if(t.charge>0){
                                      writer.addEvent(outEvent,1);
                                  } else {
                                      writer.addEvent(outEvent,0);
                                  }
                              }
                          }
                      }
                  }
              }
        }
        
        System.out.println("processed event = " + counter);
        writer.close();
    }
}
