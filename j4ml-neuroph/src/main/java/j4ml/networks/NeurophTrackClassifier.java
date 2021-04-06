/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import java.util.List;
import org.neuroph.core.Connection;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.Weight;

/**
 *
 * @author gavalian
 */
public class NeurophTrackClassifier {
    
    private NeuralNetwork            network = null;
    public NeurophTrackClassifier(){
        
    }
    
    public final void load(String filename){
        network = NeuralNetwork.createFromFile(filename);
    }
    
    public final double[] evaluate(double[] features){
        network.setInput(features);
        network.calculate();
        return network.getOutput();
    }
    
    public final int getOutputClass(double[] output){
        int   index = 0;
        double  max = 0.0;
        for(int i = 0; i < output.length; i++){
            if(output[i]>max){ max = output[i]; index = i;}
        }
        return index;
    }
    
    public void print(){
        List<Layer> layers = network.getLayers();
        System.out.println(" layers size = " + layers.size());
        int counter = 0;
        for(Layer layer : layers){
            List<Neuron>  neurons = layer.getNeurons();
            System.out.println("layer # " + counter + " neurons = " + neurons.size());
            for(Neuron neuron : neurons){
                System.out.println(neuron);
                Weight[] weights = neuron.getWeights();
                List<Connection> connections = neuron.getInputConnections();
                System.out.println("\t\t weight length = " + weights.length 
                        + "  connections = " + connections.size());
                for(Weight w : weights){
                    System.out.println(" \t\t   >> " + w);
                }
            }
            counter++;
        }
    }
    
    public static void main(String[] args){
       NeurophTrackClassifier c = new  NeurophTrackClassifier();
       c.load("trackClassifier.nnet");
       c.print();
    }
}
