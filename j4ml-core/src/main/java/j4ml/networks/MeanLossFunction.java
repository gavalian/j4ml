/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import java.util.List;
import org.nd4j.autodiff.samediff.SDVariable;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.lossfunctions.impl.LossL2;


/**
 *
 * @author gavalian
 */
public class MeanLossFunction extends LossL2 {

    public MeanLossFunction() {
        
    }
    
    public MeanLossFunction(INDArray weights) {
        super(weights);
    }

     
   @Override
    public double computeScore(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask,
                    boolean average) {

        double score = super.computeScore(labels, preOutput, activationFn, mask, average);
        //score /= (labels.size(1));
        return score;
    }

    @Override
    public INDArray computeScoreArray(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask) {
        INDArray scoreArr = super.computeScoreArray(labels, preOutput, activationFn, mask);
        
        //return scoreArr.divi(labels.size(1));
        return scoreArr;
    }

    @Override
    public INDArray computeGradient(INDArray labels, INDArray preOutput, IActivation activationFn, INDArray mask) {
        INDArray gradients = super.computeGradient(labels, preOutput, activationFn, mask);
        //return gradients.divi(labels.size(1));
        return gradients;
    }
    
    public double getNDF(INDArray labels, int n){
        long size = labels.size(1);
        int count = 0;
        for(int i = 0; i < size; i++){
            double value = labels.getDouble(new int[]{n,i});
            if(value>0.5) count++;
        }
        return (double) count;
    }
    
    /**
     * The opName of this function
     *
     * @return
     */
    @Override
    public String name() {
        return toString();
    }


    @Override
    public String toString() {
        if (weights == null)
            return "LossGLE()";
        return "LossGLE(weights=" + weights + ")";
    }
    
}
