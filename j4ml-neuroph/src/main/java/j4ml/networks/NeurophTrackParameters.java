/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import j4ml.io.TextUtils;
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
public class NeurophTrackParameters {
     private NeuralNetwork            network = null;
     private String  networkTitle = "neuroph";
         private int            printoutFrequency = 1;
         
     public final void init(int[] layersSize){
         network = new MultiLayerPerceptron(layersSize);
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < layersSize.length; i++) str.append(String.format("%dx", layersSize[i]));
        networkTitle = str.toString();
     }
     
     protected double toSeconds(long then, long now){
        return ( (double) (now-then) )/1000.0;
     }
     
     public void train(DataSet ds, int nEpochs){
         System.out.println("starting training network.... with sample size = " + ds.getRows().size());
         ((LMS) network.getLearningRule()).setMaxIterations(10);
         ((LMS) network.getLearningRule()).setMaxError(0.00000001);
         MomentumBackpropagation learningRule = (MomentumBackpropagation) network.getLearningRule();
         learningRule.setLearningRate(0.1);
         learningRule.setMomentum(0.9);
         for(int i = 0; i < nEpochs; i++){
             long then = System.currentTimeMillis();
             network.learn(ds);
             long now  = System.currentTimeMillis();
             
             if((i)%this.printoutFrequency==0){
                 int    epoch = ((LMS) network.getLearningRule()).getCurrentIteration();
                 double error = ( (LMS) network.getLearningRule()).getTotalNetworkError();
                 double time = toSeconds(then,now);
                 System.out.printf(">*< %8d => error = %18.12f , time = %12.2f seconds\n",i+1,error,time);
             }
         }
     }
     
     public DataSet readDataSet(String filename, int max){
         DataSet dataset = new DataSet(6,4);
         
         TextFileReader reader = new TextFileReader();
         reader.open(filename);
         int counter = 0;
         int counterImport = 0;
         while(true){
             List<String> lines = reader.readLines(1);
             if(lines.size()<1) break;
             counter++;
             if(lines.get(0).startsWith("1,")==true){
                 double[]  input = TextUtils.getAsDoubleArray(lines.get(0), new int[]{6,7,8,9,10,11}, 
                         new double[]{0,112,0,112,0,112,0,112,0,112,0,112}, ",");
                 double[] output = TextUtils.getAsDoubleArray(lines.get(0), new int[]{2,3,4,5}, 
                         new double[] {0.0,10.0,0.0,Math.PI/2.0,
                             -Math.PI,Math.PI,
                             -30.0,5.0},  ",");
                 double[] params = TextUtils.getAsDoubleArray(lines.get(0), new int[]{1,2,3,4,5}, ",");
                 if(params[0]<8.0&&params[1]<3.5&&params[4]>-30&&params[4]<5.0){
                     if(counterImport<max) {
                         dataset.add(new DataSetRow(input,output));                     
                         counterImport++;
                     }
                 }
             }

         }
         System.out.println("lines read = " + counter + " lines imported = " + counterImport);
         return dataset;
     }
     public void evaluate(DataSet ds){
         int nrows = ds.getRows().size();
         for(int i = 0; i < nrows; i++){
             double[] input = ds.get(i).getInput();
             network.setInput(input);
             network.calculate();
             double[] output = network.getOutput();
             double[] desired = ds.get(i).getDesiredOutput();
             for(int k = 0; k < desired.length; k++){
                 System.out.printf("%12.6f %12.6f ", desired[k],output[k]);
             }
             System.out.print("\n");
         }
     }
     public static void main(String[] args){
         
         NeurophTrackParameters network = new NeurophTrackParameters();
         DataSet training = network.readDataSet("track_parameters_full.csv",30000);
         
         training.shuffle();
         DataSet[] split = training.split(0.6,0.4);
         
         
         network.init(new int[]{6,24,24,4});
         network.train(split[0], 50);
         network.evaluate(split[1]);
     }
}
