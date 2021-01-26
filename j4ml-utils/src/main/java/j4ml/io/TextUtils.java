/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.io;

/**
 *
 * @author gavalian
 */
public class TextUtils {
    public static double[] getAsDoubleArray(String data, int[] indices, String delim){
        double[] result = new double[indices.length];
        String[] tokens = data.split(delim);
        for(int i = 0; i < result.length; i++){
            result[i] = Double.parseDouble(tokens[indices[i]]);
        }
        return result;
    }
    
    public static double[] getAsDoubleArray(String data, int[] indices, double[] normalization, String delim){
        double[] result = new double[indices.length];
        String[] tokens = data.split(delim);
        for(int i = 0; i < result.length; i++){
            double value = Double.parseDouble(tokens[indices[i]]);
            double min = normalization[i*2];
            double max = normalization[i*2+1];
            result[i]  = (value-min)/(max-min);
        }
        return result;
    }
}
