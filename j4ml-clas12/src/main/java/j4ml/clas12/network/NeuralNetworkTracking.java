/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.network;

import j4ml.clas12.tracking.ClusterCombinations;
import java.util.List;

/**
 *
 * @author gavalian
 */
public abstract class NeuralNetworkTracking {
    
    public abstract void init(List<String> fileList);        
    
    public abstract void classify(ClusterCombinations comb);
    public abstract void fix(ClusterCombinations comb);
    public abstract void evaluate(ClusterCombinations comb);
}