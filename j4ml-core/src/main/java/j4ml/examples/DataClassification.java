/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.examples;

import j4ml.networks.MLPClassifier;
import j4ml.readers.LibSVMReader;
import org.nd4j.linalg.dataset.DataSet;

/**
 *
 * @author gavalian
 */
public class DataClassification {
    
    public static void classificationTwo(){
        MLPClassifier classifier = new MLPClassifier(6,new int[]{64,64},2);
        // Initialize svm reader for reading classification data
        LibSVMReader  dataReader = LibSVMReader.classification(2, 6);
        DataSet training = dataReader.readClassification("sample/data.lsvm");
        DataSet testing  = dataReader.readClassification("sample/test.lsvm");
        
        System.out.println(training);
        // train network with given training sample 5 epochs
        // second argument is the testing data. if null - no testing is performed
        classifier.train(training, testing, 5);
    }
    
    public static void classificationThree(){
        MLPClassifier classifier = new MLPClassifier(6,new int[]{64,64,64,64},3);
        // Initialize svm reader for reading classification data
        LibSVMReader  dataReader = LibSVMReader.classification(3, 6);
        DataSet training = dataReader.readClassification("sample/data3.lsvm");
        DataSet testing  = dataReader.readClassification("sample/test3.lsvm");
        
        System.out.println(training);
        // train network with given training sample 5 epochs
        // second argument is the testing data. if null - no testing is performed
        classifier.train(training, testing, 5);
    }
    
    public static void main(String[] args){
        
        //---- Run classification training on file with 2 classes
        DataClassification.classificationTwo();
        
        //---- Run classification training on file with 2 classes
        DataClassification.classificationThree();
                
    }
}
