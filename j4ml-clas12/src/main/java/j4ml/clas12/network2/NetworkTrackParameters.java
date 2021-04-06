/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.network2;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.nnet.learning.MomentumBackpropagation;

/**
 *
 * @author gavalian
 */
public class NetworkTrackParameters {
    private NeuralNetwork            network = null;
    
    public NetworkTrackParameters(){
        
    }
    public final void load(String filename){
        network = NeuralNetwork.createFromFile(filename);
    }
    
    public final double[] evaluate(double[] features){
        network.setInput(features);
        network.calculate();
        return network.getOutput();
    }
    
    public void create(int[] layers){
        network = new MultiLayerPerceptron(layers);
        ((LMS) network.getLearningRule()).setMaxIterations(10);
         ((LMS) network.getLearningRule()).setMaxError(0.00000001);
         MomentumBackpropagation learningRule = (MomentumBackpropagation) network.getLearningRule();
         learningRule.setLearningRate(0.1);
         learningRule.setMomentum(0.9);
    }
    protected double toSeconds(long then, long now){
        return ( (double) (now-then) )/1000.0;
    }
    
    public NeuralNetwork getNetwork(){
        return network;
    }
    
    public void train(DataSet ds, int nEpochs){
         for(int i = 0; i < nEpochs; i++){
             long then = System.currentTimeMillis();
             network.learn(ds);
             long now  = System.currentTimeMillis();
             
             if((i)%2==0){
                 int    epoch = ((LMS) network.getLearningRule()).getCurrentIteration();
                 double error = ( (LMS) network.getLearningRule()).getTotalNetworkError();
                 double time = toSeconds(then,now);
                 System.out.printf(">*< %8d => error = %18.12f , time = %12.2f seconds\n",i+1,error,time);
             }
         }
    }
    
}
