/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import java.util.List;
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
public class NeurophEncoder {
    NeuralNetwork network = null;
    
    public static double[] getVector(String vecString, int vecSize){
        double[] vector = new double[vecSize];
        String[] tokens = vecString.split("\\s+");
        for(int i = 1; i < tokens.length; i++){
            String[] pair = tokens[i].split(":");
            int item = Integer.parseInt(pair[0]);
            double value = Double.parseDouble(pair[1]);
            vector[item-1] = value;
        }
        return vector;
    }
    
    public static DataSet readFile(String filename, int max){
        
        DataSet  trainingSet = new DataSet(6*112,6*112);
        
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        int counter = 0;
        while(true){
            List<String> lines = reader.readLines(2);
            if(lines.size()<2) break;
            double[]  input = NeurophEncoder.getVector(lines.get(1), 6*112);
            double[] output = NeurophEncoder.getVector(lines.get(0), 6*112);
            trainingSet.add(new DataSetRow(input,output));
            counter++;
            if(counter>=max) break;
        }
        
        return trainingSet;
    }
    
    public static String compare(double[] input, double[] output, double[] result){
        int nhits = 0;
        int nhitsOutput = 0;
        int nhitsOutputInResult = 0;
        int nhitsInputInResult  = 0;
        
        for(int i = 0; i < input.length; i++){
            if(input[i]>0.5){
                nhits++;
                if(result[i]>0.5){
                    nhitsInputInResult++;
                }
            }
            if(output[i]>0.5){
                nhitsOutput++;
                if(result[i]>0.5) nhitsOutputInResult++;
            }
        }
        return String.format("%d %d %d %d", nhits,nhitsOutput,nhitsOutputInResult,nhitsInputInResult);
    }
    
    public static void test(NeuralNetwork network, String filename, int max, String output){
        DataSet ds = NeurophEncoder.readFile(filename, max);
        TextFileWriter writer = new TextFileWriter();
        writer.open(output);
        int size = ds.getRows().size();
        for(int i = 0; i < size; i++){
            network.setInput(ds.getRows().get(i).getInput());
            network.calculate();
            double[] result = network.getOutput();
            String dataString = NeurophEncoder.compare(
                    ds.getRows().get(i).getInput(),
                    ds.getRows().get(i).getDesiredOutput(),result
            );
            //System.out.println(dataString);
            writer.writeString(dataString);
        }
        writer.close();
    }
    
    public static void main(String[] args){
        
        int nEpochs  = 50;
        int nSamples = 350;
        
        if(args.length>0){
            nEpochs = Integer.parseInt(args[0]);
        }
        
        if(args.length>1){
            nSamples = Integer.parseInt(args[1]);
        }
        
        DataSet tr = NeurophEncoder.readFile("dc_raw_train.lsvm",nSamples);
        NeuralNetwork network = null;
        
        
        network = new MultiLayerPerceptron(6*112,112,112/2,112,6*112);
        
        ((LMS) network.getLearningRule()).setMaxIterations(10);
        ((LMS) network.getLearningRule()).setMaxError(0.00000001);
         
        MomentumBackpropagation learningRule = (MomentumBackpropagation) network.getLearningRule();
        learningRule.setLearningRate(0.1);
        learningRule.setMomentum(0.7);
        
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
                String output = "dc_encoder_results_iter_"+ i + ".txt";
                 NeurophEncoder.test(network, "dc_raw_test.lsvm", nSamples,output);
            }
        }
        
        //network.save("encoder_dc.nnet");
        //NeurophEncoder.test(network, "dc_raw_test.lsvm", nSamples);
    }
}
