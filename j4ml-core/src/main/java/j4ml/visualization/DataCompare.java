/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.visualization;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author gavalian
 */
public class DataCompare {
    
    public static void compare(INDArray output, INDArray outputTrue){
        long nrows = output.size(0);
        for(int i = 0; i < nrows; i++){
            int counter = 0;
            int diff    = 0;
            int noise   = 0;
            int[] index = new int[]{i,0};
            long nfeatures = output.size(1);
            for( int f = 0; f < nfeatures; f++){
                index[1] = f;
                double value     = output.getDouble(index);
                double valueTrue = outputTrue.getDouble(index);
                if(valueTrue>0.5){
                    counter++;
                    if(value>0.5) diff++;
                } else {
                    if(value>0.5) noise++;
                }
            } 
            System.out.printf(" %5d : difference = %5d/%5d/%5d\n",i,noise,diff,counter);
        }
    }
}
