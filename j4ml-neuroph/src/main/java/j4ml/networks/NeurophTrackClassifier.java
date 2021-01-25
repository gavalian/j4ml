/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import org.neuroph.core.NeuralNetwork;

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
    
}
