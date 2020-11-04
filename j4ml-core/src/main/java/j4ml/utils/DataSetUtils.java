/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.utils;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author gavalian
 */
public class DataSetUtils {
    
    public static double[] getRow(INDArray array, int row){
        long size = array.size(1);
        double[] result = new double[(int) size];
        int[]    index = new int[]{row,0};
        for(int i = 0; i < size; i++){
            index[1]  = i;
            result[i] = array.getDouble(index);
        }
        return result;
    }
    
    public static void putRow(INDArray array, int row, double[] data){
        long size = array.size(1);
        if(size!=data.length){
            System.out.println("[error] in putRow():: sizes do not match");
            return;
        }
        int[]    index = new int[]{row,0};
        for(int i = 0; i < size; i++){
            index[1]  = i;
            array.putScalar(index,data[i]);
        }
    }
    
    public static void  threshold(double[] array, double threshold){
        for(int i = 0; i < array.length; i++){
            if(array[i]>threshold){
                array[i] = 1.0;
            } else {
                array[i] = 0.0;
            }
        }
    }
    
    public static int[] compare(INDArray source, INDArray dest, int row, double threshold){
        int[] result = new int[4];
        double[] arraySource = DataSetUtils.getRow(source, row);
        double[] arrayDest   = DataSetUtils.getRow(dest, row);
        
        for(int i = 0; i < arraySource.length; i++){
            if(arraySource[i]>threshold) result[0] = result[0] + 1;
            if(arrayDest[i]>threshold) result[1] = result[1] + 1;
            if(arrayDest[i]>threshold&&arraySource[i]>threshold) result[2] = result[2] + 1;
            if(arrayDest[i]>threshold&&arraySource[i]<threshold) result[3] = result[3] + 1;
        }
        return result;
    }
    public static String doubleArrayString(double[] array, String format, int wrap){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            str.append(String.format(format, array[i]));
            if((i+1)%wrap==0&&i!=0){
                str.append("\n");
            }
        }
        return str.toString();
    }
    
    public static String intArrayString(int[] array, String format){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            str.append(String.format(format, array[i]));
        }
        return str.toString();
    }
    
    public static void normalize(double[] data){
        double max = 0.0;
        for(int i = 0; i < data.length; i++){
            if(data[i]>max) max = data[i];
        }
        if(max>0.00000000001){
            for(int i = 0; i < data.length; i++){
                data[i] = data[i]/max;
            }
        }
    }
    
    
}
