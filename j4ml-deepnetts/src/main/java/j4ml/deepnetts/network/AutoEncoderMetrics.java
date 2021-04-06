/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.network;

import deepnetts.data.TabularDataSet;
import deepnetts.net.FeedForwardNetwork;
import deepnetts.net.NeuralNetwork;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.visrec.ml.data.DataSet;

/**
 *
 * @author gavalian
 */
public class AutoEncoderMetrics {
    
    double[] error = new double[6];
    double[] means = new double[6];
    int[]    count = new int[6];
    
    
    public String getString(float[] array){
        StringBuilder str = new StringBuilder();
        str.append("[");
        for(int i = 0; i < array.length; i++)
            str.append(String.format("%7.4f ", array[i]));
        str.append("]");
        return str.toString();
    }
    
    public void evaluate(FeedForwardNetwork network, List<float[]> items){
        
        int nrows = items.size();
        Random r = new Random();
        for(int i = 0; i < nrows; i++){
            int index = r.nextInt(6);
            float[] input  = new float[6];
            for(int k = 0; k < 6; k++) input[k] = items.get(i)[k];
            float value = input[index];
            input[index] = 0.0f;
            float[] output = network.predict(input);
            
            //means[index] = output[index] - value;
            //count[index] = count[index] + 1;
            System.out.println(getString(input) 
                    //" =  (" 
                    //+ String.format("%7.4f", value) + ", " 
                    //+ String.format("%7.4f", output[index])
                    //+ " ) = " 
                    + " ==> "
                    + getString(output));
            //System.out.printf("%3d %7.5f %7.5f\n",index,value,output[index]);
        }
        
        for(int i = 0; i < 6; i++){
            means[i] = means[i]/count[i];
        }        
        
    }
    
    public String getMetricsString(){
        StringBuilder str = new StringBuilder();
        return str.toString();
    }
}
