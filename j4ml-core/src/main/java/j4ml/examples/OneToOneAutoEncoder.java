/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.examples;

import j4ml.networks.AutoEncoder;
import j4ml.readers.LibSVMReader;
import j4ml.readers.TextFileReader;
import java.util.List;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

/**
 *
 * @author gavalian
 */
public class OneToOneAutoEncoder {
    public static void main(String[] args){
        String filename = "sample/encoder.lsvm";
        String filename_test = "sample/encoder_test.lsvm";
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        
        TextFileReader reader_test = new TextFileReader();
        reader_test.open(filename_test);
        
        List<String> lines = reader.readLines(50000);
        List<String> linesTest = reader_test.readLines(200);
        
        LibSVMReader   svm  = LibSVMReader.classification(2, 6);
        

        
        DataSet    ds_input = svm.readClassification(lines);
        DataSet    ds_output = svm.readClassification(lines);
        
        DataSet train = new DataSet(ds_input.getFeatures(),ds_output.getFeatures());
        
        DataSet    t_input = svm.readClassification(linesTest);
        
        
        System.out.println(train);
        
        
        AutoEncoder encoder = new AutoEncoder(6,new int[]{12,6});
        encoder.train(train, 50);
        
        INDArray output = encoder.getNetwork().output(t_input.getFeatures());
        System.out.println(t_input.getFeatures());
        System.out.println("------ and here we go ?\n");
        System.out.println(output);
    }
}
