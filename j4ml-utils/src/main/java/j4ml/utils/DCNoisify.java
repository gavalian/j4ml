/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.utils;

import j4ml.io.LibSVMUtils;
import j4ml.io.TextFileReader;
import java.util.List;
import java.util.Random;

/**
 *
 * @author gavalian
 */
public class DCNoisify {
    public static void main(String[] args){
        
        String filename = "";
        double noiseLevel = 5.0;
        
        if(args.length > 0 ) filename = args[0];
        if(args.length > 1 ) noiseLevel = Double.parseDouble(args[1]);
        
        Random rand = new Random();
        int    nNoise = (int) (6*112*2*noiseLevel);
        //System.out.println("NOISE HITS COUNT = " + nNoise);
        
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        
        while(true){
            List<String> lines = reader.readLines(2);
            if(lines.size()<2) break;    
            
            double[]     array = LibSVMUtils.toArray(lines.get(1),36*112);
            for(int i = 0; i < nNoise; i++){
                int r = rand.nextInt(12*112);
                array[r] = 1.0;
            }
            String dataInput = LibSVMUtils.toLSVMString(array, 0.5);

           System.out.println(lines.get(0));
           System.out.println("0 " + dataInput); 
        }
    }
}
