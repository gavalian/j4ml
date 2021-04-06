/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.network2;

import j4ml.clas12.tracking.ClusterCombinations;
import java.util.List;

/**
 *
 * @author gavalian
 */
public abstract class Clas12TrackAI {
    
    public Clas12TrackAI(){ }
    
    public abstract void init(List<String> networkFiles);
    public abstract void evaluate(ClusterCombinations comb);    
    public abstract void fix(ClusterCombinations comb);
    
}
