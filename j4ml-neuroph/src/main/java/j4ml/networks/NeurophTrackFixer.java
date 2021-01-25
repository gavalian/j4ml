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
public class NeurophTrackFixer {
    
    private NeuralNetwork  network = null;
    
    public NeurophTrackFixer(){
        
    }
    
    public final void load(String filename){
        network = NeuralNetwork.createFromFile(filename);
    }
    
    public final double[] evaluate(double[] features){
        network.setInput(features);
        network.calculate();
        return network.getOutput();
    }
    
}
