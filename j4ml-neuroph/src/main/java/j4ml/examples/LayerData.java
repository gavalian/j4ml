/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author gavalian
 */
public class LayerData {
    
    private double[][] data = new double[6][112];
    
    private double[][] pattern = new double[][]{
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };
    
    private double[][] pattern2 = new double[][]{
        {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };
    
    private double[][] pattern3 = new double[][]{
        {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };
    
    List<double[][]> patterns = new ArrayList<double[][]>();
    
    private Random rand = new Random();
    
    public LayerData(){
        patterns.add(pattern);
        patterns.add(pattern2);
        patterns.add(pattern3);
//.addAll(pattern,pattern2,pattern3);
    }
    
    public LayerData(double[] flat){
        patterns.add(pattern);
        patterns.add(pattern2);
        patterns.add(pattern3);
        set(flat);
//.addAll(pattern,pattern2,pattern3);
    }

    public final void set(double[] flat){
        int count = 0;

        for(int y = 0; y < 6; y++){
            for(int x = 0; x < 112; x++){
                data[y][x] = flat[count];
                count++;
            }
        }
    }
    
    public void set(int layer, int wire){
        data[layer][wire] = 1.0;
    }
    
    public double get(int layer, int wire){
        return data[layer][wire];
    }
    
    public void addPattern(int pindex, int position){
        double[][] pat = patterns.get(pindex);
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 6; y++){
                data[y][x+position] = pat[y][x];
            }
        }
    }
    
    public void addPatternRandom(){
        int pindex = rand.nextInt(this.patterns.size());
        int position = rand.nextInt(112-8);
        addPattern(pindex,position);
    }
    
    public void show(){
        System.out.println(">>>>>");
      for(int y = 0; y < 6; y++){
          for(int x = 0; x < 112 ; x++){
              if(data[y][x]<0.9){
                  System.out.print("-");
              } else {
                  System.out.print("X");
              }
          }
          System.out.println();
      }  
      System.out.println(">>>>>");
    }
    
    public LayerData getCopy(){
        LayerData layer = new LayerData();
        for(int x = 0; x < 112; x++){
            for(int y = 0; y < 6; y++){
                if(data[y][x]>0.9)
                layer.set(y,x);
            }
        }
        return layer;
    }
    
    
    public double[] flat(){
        double[] result = new double[112*6];
        int count = 0;
        for(int x = 0; x < 112 ; x++){
            for(int y = 0; y < 6; y++){
             result[count] = data[y][x];
             count++;
          }
        }
        return result;
    }
    
    public void addNoise(int npoints){
        for(int i = 0; i < npoints; i++){
            int x = rand.nextInt(112);
            int y = rand.nextInt(6);
            data[y][x] = 1.0;
        }
    }
    
    public int getCount(){
        int count = 0; 
        for(int x = 0; x < 112; x++){
            for(int y = 0; y < 6; y++){
                if(data[y][x]>0.9) count++;
            }
        }
        return count;
    }
    
    public static List<LayerData> generateSet(int size){
        List<LayerData> list = new ArrayList<>();
        for(int i = 0 ; i < size; i++){
            LayerData data = new LayerData();
            data.addPatternRandom();
            list.add(data);
        }
        return list;
    }
    
    public static  List<LayerData> nosify(List<LayerData> dataList, int noise){
         List<LayerData> list = new ArrayList<>();
         for(int i = 0; i < dataList.size(); i++){
             LayerData data = dataList.get(i).getCopy();
             data.addNoise(noise);
             list.add(data);
         }
         return list;
    }
    
    public int[] getCount(LayerData layer){
        int[] count = new int[]{0,0,0,0};
        for(int x = 0; x < 112; x++){
            for(int y = 0; y < 6; y++){
                
                if(data[y][x]>0.5) count[0] = count[0] + 1;
                if(layer.get(y, x)>0.5) count[1] = count[1] + 1;
                if(data[y][x]>0.5&&layer.get(y, x)>0.5) count[2] = count[2] + 1;
                if(data[y][x]<0.5&&layer.get(y, x)>0.5) count[3] = count[3] + 1;
            }
        }
        return count;
    }
    
    public static void main(String[] args){
        
        for(int i = 0; i < 10; i++){      
            LayerData     data  = new LayerData();
            data.addPatternRandom();
            data.addNoise(25);
            data.show();
        }        

    }
}
