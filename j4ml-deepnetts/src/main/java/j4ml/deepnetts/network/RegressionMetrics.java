/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.network;

import deepnetts.net.FeedForwardNetwork;
import java.util.List;
import java.util.Random;

/**
 *
 * @author gavalian
 */
public class RegressionMetrics {
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
            /*System.out.println(getString(input) + " =  (" 
                    + String.format("%7.4f", value) + ", " 
                    + String.format("%7.4f", output[index])
                    + " ) = " 
                    + getString(output));*/
            System.out.printf("%3d %7.5f %7.5f\n",index,value,output[0]);
        }
    }
}
