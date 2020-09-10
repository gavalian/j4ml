/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.examples;

import j4ml.readers.LibSVMReader;
import j4ml.readers.TextFileReader;
import j4ml.visualization.DataImageMaker;
import java.util.List;
import org.nd4j.linalg.dataset.DataSet;

/**
 *
 * @author gavalian
 */
public class LibSVMExample {
    
    public static void textTest(){
        String filename = "../j4ml-data/dc_raw_tdc.lsvm";
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        List<String>  lines = reader.readLines(2);
        //lines = reader.readLines(2);
        //lines = reader.readLines(2);
        for(String item : lines){System.out.println(item);}
        LibSVMReader svmReader = LibSVMReader.classification(2, 112*36);
        
        DataSet  ds = svmReader.readClassification(lines);
        System.out.println(ds);
        ds.getFeatures();
        
        DataImageMaker img = new DataImageMaker(112,36);
        img.createImage(ds.getFeatures(), 1,0);
        img.save("dc_clusterhits.png");
        
        
        
        img.createImage(ds.getFeatures(), 1,1);
        img.save("dc_rawhits.png");
        
        img.magnify(2, 8);
        img.save("dc_rawhits_magnified.png");
    }
    
    public static void main(String[] args){
        LibSVMExample.textTest();
    }
}
