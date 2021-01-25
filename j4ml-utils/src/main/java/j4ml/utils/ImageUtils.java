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
public class ImageUtils {
    public static void main(String[] args){
         String filename = "";
         int         row = 0;
        
        if(args.length > 0 ) filename = args[0];
        if(args.length > 1 ) row = Integer.parseInt(args[1]);
        
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        
        List<String> skip = reader.readLines(row);
        
        List<String> line = reader.readLines(1);
        double[] array = LibSVMUtils.toArray(line.get(0),36*112);
        String outputFile = filename + "_" + row + "_" + "_image.png";
        System.out.println("saving file : " + outputFile);
        LibSVMUtils.saveImage(array, 112, 36, outputFile);
    }
}
