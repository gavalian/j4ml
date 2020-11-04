/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.denoise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author gavalian
 */
public class LayerData8 {
    
    private double[][] data = null;//new double[6][8];
    
    
    int columns = 24;
    
    
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
    
    public LayerData8(){
        patterns.add(pattern);
        patterns.add(pattern2);
        patterns.add(pattern3);
        data = new double[6][columns];
//.addAll(pattern,pattern2,pattern3);
    }
    
    public LayerData8(double[] flat){
        patterns.add(pattern);
        patterns.add(pattern2);
        patterns.add(pattern3);

        data = new double[6][columns];
        set(flat);
//.addAll(pattern,pattern2,pattern3);
    }

    public final void set(double[] flat){
        int count = 0;
        for(int x = 0; x < columns; x++){
            for(int y = 0; y < 6; y++){
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
                if(x+position<columns)
                    data[y][x+position] = pat[y][x];
            }
        }
    }
    
    public void addPatternRandom(){
        int count = 3;//rand.nextInt(4)+1;
        for(int i = 0; i < count; i++){
            int pindex = rand.nextInt(this.patterns.size());
            int position = rand.nextInt(columns-3);
            addPattern(pindex,position);
        }
    }
    
    public void show(){
        System.out.println(">>>>>");
      for(int y = 0; y < 6; y++){
          for(int x = 0; x < columns ; x++){
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
    
    public LayerData8 getCopy(){
        LayerData8 layer = new LayerData8();
        for(int x = 0; x < columns; x++){
            for(int y = 0; y < 6; y++){
                if(data[y][x]>0.9)
                layer.set(y,x);
            }
        }
        return layer;
    }
    
    
    public double[] flat(){
        double[] result = new double[columns*6];
        int count = 0;
        for(int y = 0; y < 6; y++){
          for(int x = 0; x < columns ; x++){
             result[count] = data[y][x];
             count++;
          }
        }
        return result;
    }
    
    public void addNoise(int npoints){
        for(int i = 0; i < npoints; i++){
            int x = rand.nextInt(columns);
            int y = rand.nextInt(6);
            data[y][x] = 1.0;
        }
    }
    
    public int getCount(){
        int count = 0; 
        for(int x = 0; x < columns; x++){
            for(int y = 0; y < 6; y++){
                if(data[y][x]>0.9) count++;
            }
        }
        return count;
    }
    
    public static List<LayerData8> generateSet(int size){
        List<LayerData8> list = new ArrayList<>();
        for(int i = 0 ; i < size; i++){
            LayerData8 data = new LayerData8();
            data.addPatternRandom();
            list.add(data);
        }
        return list;
    }
    
    public static  List<LayerData8> nosify(List<LayerData8> dataList, int noise){
         List<LayerData8> list = new ArrayList<>();
         for(int i = 0; i < dataList.size(); i++){
             LayerData8 data = dataList.get(i).getCopy();
             data.addNoise(noise);
             list.add(data);
         }
         return list;
    }
    
    public int[] getCount(LayerData layer){
        int[] count = new int[]{0,0,0,0};
        for(int x = 0; x < columns; x++){
            for(int y = 0; y < 6; y++){
                if(data[y][x]>0.5){
                    count[3] = count[3]+1;
                    if(layer.get(y, x)>0.5){
                        count[0] = count[0] +1;
                    } else {
                        count[1] = count[1] +1;
                    }                    
                } else {
                    if(layer.get(y, x)>0.5) count[2] = count[2] +1;
                }
            }
        }
        return count;
    }
    
    public static void main(String[] args){
        
        for(int i = 0; i < 10; i++){      
            LayerData8     data  = new LayerData8();
            data.addPatternRandom();
            //data.addNoise(5);
            data.show();
        }        

    }
}
