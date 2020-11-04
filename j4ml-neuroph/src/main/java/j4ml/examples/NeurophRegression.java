/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.examples;

import j4ml.networks.NeurophEncoder;
import java.util.List;
import java.util.Random;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.nnet.learning.MomentumBackpropagation;

/**
 *
 * @author gavalian
 */
public class NeurophRegression {
    public static DataSet createDataSet(int length){
        DataSet  trainingSet = new DataSet(4,2);
        
        Random rand = new Random();
        for(int i = 0; i < length; i++){
            double[] data = new double[4];
            double[] result = new double[2];
            for(int k = 0; k < 4; k++){
                data[k] = rand.nextFloat();
             }
             result[0] = data[0]*data[1];
             result[1] = data[3]*data[1]*data[2];
             trainingSet.add(new DataSetRow(data,result));
        }
        return trainingSet;
    }
    
    
    public static void main(String[] args){
        NeuralNetwork network = null;                
        network = new MultiLayerPerceptron(4,8,8,8,2);
        
        ((LMS) network.getLearningRule()).setMaxIterations(50);
        ((LMS) network.getLearningRule()).setMaxError(0.00000001);
         
        MomentumBackpropagation learningRule = (MomentumBackpropagation) network.getLearningRule();
        learningRule.setLearningRate(0.1);
        learningRule.setMomentum(0.7);
        
        DataSet tr = NeurophRegression.createDataSet(80000);
        DataSet ts = NeurophRegression.createDataSet(200);
        
        int nEpochs = 100;
        System.out.println(" starting training with data : " + tr.getRows().size());
        for(int i = 0; i < nEpochs; i++){
            long start_time = System.currentTimeMillis();
            network.learn(tr);
            long end_time   = System.currentTimeMillis();
            if(i%2==0){
                long   epochTime = end_time - start_time;
                int    epoch = ((LMS) network.getLearningRule()).getCurrentIteration();
                double error = ( (LMS) network.getLearningRule()).getTotalNetworkError();
                System.out.println( " ITERATION = " + i + "  TOTAL ERROR = " 
                        + error + "  epoch time = "  + epochTime + " ms");
                //String output = "dc_encoder_results_iter_"+ i + ".txt";
                 //NeurophEncoder.test(network, "dc_raw_test.lsvm", nSamples,output);
            }
        }
        
        List<DataSetRow>  rows = ts.getRows();
        
        for(int i = 0; i < rows.size(); i++){
        
            DataSetRow row = rows.get(i);
            
            double[] input = row.getInput();
            double[] result = row.getDesiredOutput();
            
            network.setInput(input);
            network.calculate();
            double[] output = network.getOutput();
            System.out.printf("%12.6f %12.6f %12.6f %12.6f %12.6f %12.6f \n",
                    result[0]-output[0],result[1]-output[1],
                    result[0],result[1],output[0],output[1]);
        }
    }
}
