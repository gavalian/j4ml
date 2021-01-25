/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.utils;

import j4ml.io.LibSVMUtils;
import j4ml.io.TextFileReader;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class LibSVMDataCompare {
    public static void main(String[] args){
        
        

        String filename = "prediction.lsvm";
        if(args.length>0) filename = args[0];
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        
        //for(int i = 0; i < 100; i++){
        int counterLow = 0;
        int counterHigh = 0;
        
        while(true){
            List<String> list = reader.readLines(3);
            if(list.size()!=3) break;
            
            double[]     raw = LibSVMUtils.toArray(list.get(0), 112*36);
            double[] desired = LibSVMUtils.toArray(list.get(1), 112*36);
            double[] result  = LibSVMUtils.toArray(list.get(2), 112*36);
            
            double coincidence = LibSVMUtils.coincidence(desired, result, 0.5);
            int    countRaw = LibSVMUtils.count(raw, 0.5);
            int    countDesired = LibSVMUtils.count(desired, 0.5);
            int    countResult = LibSVMUtils.count(result, 0.5);
            
            double       noise = LibSVMUtils.noise(desired, result, 0.5);
            System.err.printf("%8d %8d %8d %12.4f %12.4f\n",
                    countRaw, countDesired, countResult, coincidence, noise);
            
            if(counterLow<20){
                if(coincidence<0.40){
                    LibSVMUtils.saveImage(desired, 112, 36, "desired_low_"+counterLow+".png");
                    LibSVMUtils.saveImage(result, 112, 36, "result_low_"+counterLow+".png");
                    counterLow++;
                }
            }
        }
    }
}
