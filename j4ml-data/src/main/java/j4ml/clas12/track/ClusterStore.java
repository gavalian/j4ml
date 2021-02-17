/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.track;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author gavalian
 */
public class ClusterStore {
    
    private final List<ClusterList> layerClusters = new ArrayList<ClusterList>();
    private final int[][] patterns = new int[][]{
        {1,2,3,4,5},
        {0,2,3,4,5},
        {0,1,3,4,5},
        {0,1,2,4,5},
        {0,1,2,3,5},
        {0,1,2,3,4}
    };
    
    private final int[] missing = {0,1,2,3,4,5};
    
    public ClusterStore(){
        for(int i = 0; i < 6 ; i++) layerClusters.add(new ClusterList());
    }
        
    public void add(int layer, int id, double mean){
        layerClusters.get(layer).add(id, mean);
    }
    
    public void reset(){
        for(int i = 0; i < 6; i++) layerClusters.get(i).reset();
    }
    
    public void getCombinationsFull(ClusterCombinations comb){
        comb.reset();
        int[] ids      = new int[6];
        double[] means = new double[6];            
        for(int l1 = 0; l1 < layerClusters.get(0).getSize(); l1++){
            for(int l2 = 0; l2 < layerClusters.get(1).getSize(); l2++){
                for(int l3 = 0; l3 < layerClusters.get(2).getSize(); l3++){
                    for(int l4 = 0; l4 < layerClusters.get(3).getSize(); l4++){
                        for(int l5 = 0; l5 < layerClusters.get(4).getSize(); l5++){
                            for(int l6 = 0; l6 < layerClusters.get(5).getSize(); l6++){
                                
                                ids[0] = layerClusters.get(0).getId(l1);
                                means[0] = layerClusters.get(0).getMean(l1);
                                
                                ids[1] = layerClusters.get(1).getId(l2);
                                means[1] = layerClusters.get(1).getMean(l2);
                                
                                ids[2] = layerClusters.get(2).getId(l3);
                                means[2] = layerClusters.get(2).getMean(l3);
                                
                                ids[3] = layerClusters.get(3).getId(l4);
                                means[3] = layerClusters.get(3).getMean(l4);
                                
                                ids[4] = layerClusters.get(4).getId(l5);
                                means[4] = layerClusters.get(4).getMean(l5);
                                
                                ids[5] = layerClusters.get(5).getId(l6);
                                means[5] = layerClusters.get(5).getMean(l6);
                                if(Math.abs(means[0]- means[1])<25.0&&
                                        Math.abs(means[2]- means[3])<25.0&&
                                        Math.abs(means[4]- means[5])<25.0){
                                comb.add(ids, means);
                            }
                            }                            
                        }
                    }
                }
            }
        }
    }
    
    public void getCombinations(ClusterCombinations comb){
            comb.reset();
            int[] ids      = new int[6];
            double[] means = new double[6];            
            for(int i = 0; i < 6; i++){
                for(int l1 = 0; l1 < layerClusters.get(patterns[i][0]).getSize(); l1++){
                    for(int l2 = 0; l2 < layerClusters.get(patterns[i][1]).getSize(); l2++){
                        for(int l3 = 0; l3 < layerClusters.get(patterns[i][2]).getSize(); l3++){
                            for(int l4 = 0; l4 < layerClusters.get(patterns[i][3]).getSize(); l4++){
                                for(int l5 = 0; l5 < layerClusters.get(patterns[i][4]).getSize(); l5++){
                                    
                                    ids[patterns[i][0]] = layerClusters.get(patterns[i][0]).getId(l1);
                                    means[patterns[i][0]] = layerClusters.get(patterns[i][0]).getMean(l1);
                                    
                                    ids[patterns[i][1]] = layerClusters.get(patterns[i][1]).getId(l2);
                                    means[patterns[i][1]] = layerClusters.get(patterns[i][1]).getMean(l2);
                                    
                                    ids[patterns[i][2]] = layerClusters.get(patterns[i][2]).getId(l3);
                                    means[patterns[i][2]] = layerClusters.get(patterns[i][2]).getMean(l3);
                                    
                                    ids[patterns[i][3]] = layerClusters.get(patterns[i][3]).getId(l4);
                                    means[patterns[i][3]] = layerClusters.get(patterns[i][3]).getMean(l4);
                                    
                                    ids[patterns[i][4]] = layerClusters.get(patterns[i][4]).getId(l5);
                                    means[patterns[i][4]] = layerClusters.get(patterns[i][4]).getMean(l5);
                                    
                                    ids[missing[i]] = 0;
                                    means[missing[i]] = 0.0;
                                    if((i==0||i==1)&&Math.abs(means[2]-means[3])<25.0&&
                                            Math.abs(means[4]-means[5])<25.0){
                                        comb.add(ids, means);
                                    }
                                    
                                    if((i==2||i==3)&&Math.abs(means[0]-means[1])<25.0&&
                                            Math.abs(means[4]-means[5])<25.0){
                                        comb.add(ids, means);
                                    }
                                    if((i==4||i==5)&&Math.abs(means[0]-means[1])<25.0&&
                                            Math.abs(means[2]-means[3])<25.0){
                                        comb.add(ids, means);
                                    }
                                    
                                }
                            }
                        }
                    }
                }
            }
    }
    
    public List<ClusterEntry> getCombinationsList(){
            List<ClusterEntry> list = new ArrayList<ClusterEntry>();
            int[] ids      = new int[6];
            double[] means = new double[6];            
            for(int i = 0; i < 6; i++){
                for(int l1 = 0; l1 < layerClusters.get(patterns[i][0]).getSize(); l1++){
                    for(int l2 = 0; l2 < layerClusters.get(patterns[i][1]).getSize(); l2++){
                        for(int l3 = 0; l3 < layerClusters.get(patterns[i][2]).getSize(); l3++){
                            for(int l4 = 0; l4 < layerClusters.get(patterns[i][3]).getSize(); l4++){
                                for(int l5 = 0; l5 < layerClusters.get(patterns[i][4]).getSize(); l5++){
                                    
                                    ids[patterns[i][0]] = layerClusters.get(patterns[i][0]).getId(l1);
                                    means[patterns[i][0]] = layerClusters.get(patterns[i][0]).getMean(l1);
                                    
                                    ids[patterns[i][1]] = layerClusters.get(patterns[i][1]).getId(l2);
                                    means[patterns[i][1]] = layerClusters.get(patterns[i][1]).getMean(l2);
                                    
                                    ids[patterns[i][2]] = layerClusters.get(patterns[i][2]).getId(l3);
                                    means[patterns[i][2]] = layerClusters.get(patterns[i][2]).getMean(l3);
                                    
                                    ids[patterns[i][3]] = layerClusters.get(patterns[i][3]).getId(l4);
                                    means[patterns[i][3]] = layerClusters.get(patterns[i][3]).getMean(l4);
                                    
                                    ids[patterns[i][4]] = layerClusters.get(patterns[i][4]).getId(l5);
                                    means[patterns[i][4]] = layerClusters.get(patterns[i][4]).getMean(l5);
                                    
                                    ids[missing[i]] = 0;
                                    means[missing[i]] = 0.0;
                                    list.add(new ClusterEntry(ids,means));
                                }
                            }
                        }
                    }
                }
            }
            return list;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < 6; i++){
            int hits = layerClusters.get(i).getSize();
            str.append(String.format("%2d (%4d) : ", i+1, hits));
            str.append(layerClusters.get(i).toString()).append("\n");
        }
        return str.toString();
    }
    public static class ClusterEntry implements Comparable<ClusterEntry> {
        int[] ids = new int[6];
        double[] means = new double[6];
        int status = 0;
        Double probability = 0.0;
        
        public ClusterEntry(int[] __id, double[] __mean){
            for(int i = 0; i < 6; i++) {
                ids[i] = __id[i];
                means[i] = __mean[i];
            }
        }

        @Override
        public int compareTo(ClusterEntry o) {
            return probability.compareTo(o.probability);
        }
               
    }
    
    public static class ClusterList {
        
        private ByteBuffer buffer = null;
        private int        defaultSize = 512;
        private int        BYTES_PER_ENTRY = 8;        
        
        public ClusterList(){
            byte[] array = new byte[defaultSize*BYTES_PER_ENTRY+4];
            buffer = ByteBuffer.wrap(array);
            buffer.putInt(0,0);
        }
        
        public int getId(int row){
            return buffer.getInt(4 + row * BYTES_PER_ENTRY);
        }
        
        public double getMean(int row){
            return buffer.getFloat(4 + row * BYTES_PER_ENTRY + 4);
        }
        
        public int getSize(){
            return buffer.getInt(0);            
        }
        
        public void add(int id, double mean){
            int row = buffer.getInt(0);
            int id_offset = 4 + row * BYTES_PER_ENTRY;
            buffer.putInt(id_offset, id);
            buffer.putFloat(id_offset+4, (float) mean);
            row++;
            buffer.putInt(0,row);
        }
    
        
        public void reset(){ buffer.putInt(0, 0);}
        
        
        
        
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            int size = getSize();
            for(int i = 0; i < size ; i++){
                str.append(String.format("(%4d, %6.2f) ", getId(i),getMean(i)));
            }
            return str.toString();
        }
    }
    
    
    public static void main(String[] args){
        ClusterStore store = new ClusterStore();
        store.add(0, 1, 0.5);
        //store.add(0, 11, 0.5);
        //store.add(0, 12, 0.5);
        //store.add(0, 14, 0.5);
        store.add(1, 2, 0.5);
        store.add(1, 32, 0.5);
        store.add(1, 33, 0.5);
        store.add(2, 3, 0.5);
        store.add(3, 4, 0.5);
        store.add(4, 5, 0.5);
        //store.add(4, 45, 0.5);
        store.add(5, 6, 0.5);
        //store.add(5, 56, 0.5);
        //store.add(5, 57, 1.5);
        //store.add(5, 58, 1.5);
        
        System.out.println(store);
        
        ClusterCombinations comb = new ClusterCombinations();
        ClusterCombinations comb6 = new ClusterCombinations();
        long iter = 1000000;
        System.out.println("------ warm up round");
        for(int i = 0; i < iter; i++){
            store.getCombinations(comb);
        }
        System.out.println("------ warm up done...");
        long then = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        
        long timeCombi = 0L;
        long timeList = 0L;
        long timeSort = 0L;
        long timeSortComb = 0L;
        Random rand = new Random();
        int[] order = null;
        for(int i = 0; i < iter; i++){
            then = System.currentTimeMillis();
            store.getCombinations(comb);
            now = System.currentTimeMillis();
            timeCombi += (now-then);
            
            then = System.currentTimeMillis();
            List<ClusterEntry> entries = store.getCombinationsList();
            now = System.currentTimeMillis();
            timeList += now - then;
            
            for (int k = 0; k < entries.size(); k++){
                double p = rand.nextDouble();
                entries.get(k).probability = p;
                comb.setRow(k).setProbability(p);
            }
            
            then = System.currentTimeMillis();
            Collections.sort(entries);
            now = System.currentTimeMillis();
            timeSort += now-then;
            
            then = System.currentTimeMillis();
            //comb.sort();
            order = comb.sort();
            now = System.currentTimeMillis();
            timeSortComb += now-then;
            //store.getCombinationsFull(comb6);            
        }
        

        double time = (double) (now-then);
        System.out.printf("time = %d , time list = %d, time sort = %d , time sort list = %d , unit time = %.4f, unit time list = %.4f\n",
                timeCombi,timeList, timeSortComb, timeSort, ((double) timeCombi)/iter,((double) timeList)/iter );
        
        order = comb.sortQuick();
        System.out.println(comb.toOrderedString(order));
        
        for(int i = 0; i < order.length; i++){
            System.out.printf("%4d : %4d\n", i , order[i]);
        }
        
        System.out.println(comb);
        
        order = comb.sort();
        System.out.println(comb.toOrderedString(order));
        for(int i = 0; i < order.length; i++){
            System.out.printf("%4d : %4d\n", i , order[i]);
        }
        
        //System.out.println(comb6);
    }
}
