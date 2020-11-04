/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.examples;

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
public class NeurophAutoEncoder {
    
    public static DataSet createDataSet(int length){
        DataSet  trainingSet = new DataSet(112*6,112*6);
        List<LayerData> outData = LayerData.generateSet(length);
        List<LayerData> inData  = LayerData.nosify(outData, 40);
        
        for(int i = 0; i < length; i++){
            double[] data = inData.get(i).flat();
            double[] result = outData.get(i).flat();
            
             trainingSet.add(new DataSetRow(data,result));
        }
        return trainingSet;
    }
    public static void evaluate(NeuralNetwork network, DataSet test){
        List<DataSetRow> rows = test.getRows();
        
        LayerData layer = new LayerData();
        LayerData layerOut = new LayerData();
        
        for(int i = 0; i < rows.size(); i++){
            double[] input = rows.get(i).getInput();
            network.setInput(input);
            network.calculate();
            double[] output = network.getOutput();
            double[] desired = rows.get(i).getDesiredOutput();
            layer.set(desired);
            layerOut.set(output);
            int[] result = layer.getCount(layerOut);
            System.out.printf("%6d %4d %4d %4d %4d\n",i,result[0],result[1],result[2],result[3]);
        }
    }
    
    public static void main(String[] args){
        NeuralNetwork network = null;
        network = new MultiLayerPerceptron(112*6,56,56/2,56,112*6);
        
        ((LMS) network.getLearningRule()).setMaxIterations(1);
        ((LMS) network.getLearningRule()).setMaxError(0.00000001);
         
        MomentumBackpropagation learningRule = (MomentumBackpropagation) network.getLearningRule();
        learningRule.setLearningRate(0.1);
        learningRule.setMomentum(0.9);
        
        DataSet tr = NeurophAutoEncoder.createDataSet(200);
        DataSet ts = NeurophAutoEncoder.createDataSet(200);
        
        int nEpochs = 25;
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
        
        NeurophAutoEncoder.evaluate(network, ts);
        /*
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
        }*/
    }
}
