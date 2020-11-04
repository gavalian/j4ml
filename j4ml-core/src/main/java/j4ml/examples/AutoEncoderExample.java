/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.examples;

import j4ml.networks.AutoEncoder;
import j4ml.readers.LibSVMReader;
import j4ml.readers.TextFileReader;
import j4ml.visualization.DataCompare;
import java.util.ArrayList;
import java.util.List;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

/**
 *
 * @author gavalian
 */
public class AutoEncoderExample {
    public static List<String>  getEven(List<String> list){
        List<String> result = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            if(i%2==0) result.add(list.get(i));
        }
        return result;
    }
    
    public static List<String>  getOdd(List<String> list){
        List<String> result = new ArrayList<>();
        for(int i = 0; i < list.size(); i++){
            if(i%2!=0) result.add(list.get(i));
        }
        return result;
    }
    
    public static DataSet readFile(String filename, int count){
        
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        List<String> lines = reader.readLines(count);
    
        List<String>  output = AutoEncoderExample.getEven(lines);
        List<String>   input = AutoEncoderExample.getOdd(lines);
        LibSVMReader   svm  = LibSVMReader.classification(2, 112*36);
        DataSet    ds_input = svm.readClassification(input);
        DataSet    ds_output = svm.readClassification(output);
        System.out.println(
                " Sizes " + 
                ds_input.getFeatures().size(0) + " " + ds_input.getFeatures().size(1));
        return new DataSet(ds_input.getFeatures(), ds_output.getFeatures());
    }
    
    
    public static void testLossFunction(INDArray label, INDArray features){
        LossFunction func = LossFunctions.LossFunction.MSE;
        
        double score = func.getILossFunction().
                computeScore(label, features, Activation.RELU.getActivationFunction(), null, true);
        System.out.println(" SAMPLE SCORE = " + score);
    }
    
    public static void print(INDArray array){
        long dim1 = array.size(0);
        for(int i = 0; i < dim1; i++){
            long dim2 = array.size(1);
            System.out.printf("%5d : ",i);
            for(int f = 0; f < dim2; f++){
                double value = array.getDouble(new int[]{i,f});
                if(value>0.00000001) System.out.printf("%d:%.8f ", f,value);
            }
            System.out.println();
        }
    }
    
    public static void main(String[] args){
        DataSet dataset = AutoEncoderExample.readFile("sample/dc_noise_train.lsvm",50);
        DataSet test    = AutoEncoderExample.readFile("sample/dc_noise_train.lsvm",50);
        System.out.println(dataset);
        
        AutoEncoderExample.testLossFunction(dataset.getLabels(), dataset.getFeatures());
        
        DataCompare.compare(dataset.getFeatures(), dataset.getLabels());
        AutoEncoder encoder = new AutoEncoder(112*36,new int[]{112*2, 112, 112*2});
        encoder.train(dataset, 25);
        
        INDArray output = encoder.getNetwork().output(test.getFeatures(), false);
        System.out.println(test.getFeatures());
        DataCompare.compare(output, test.getLabels());
        
        System.out.println("SCORE = " + encoder.getNetwork().score(test));
        DataCompare.compare( test.getLabels(),output);
        
        AutoEncoderExample.testLossFunction(test.getLabels(), output);
        
        AutoEncoderExample.testLossFunction(test.getFeatures(), output);
        
        AutoEncoderExample.print(output);
    }
}
