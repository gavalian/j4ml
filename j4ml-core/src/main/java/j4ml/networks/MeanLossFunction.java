/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import org.nd4j.linalg.activations.IActivation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.primitives.Pair;

/**
 *
 * @author gavalian
 */
public class MeanLossFunction implements ILossFunction {

    @Override
    public double computeScore(INDArray labels, INDArray preOut, IActivation ia, INDArray mask, boolean bln) {
        long dim1 = labels.size(0);
        double mse = 0.0;
        
        for(int i = 0; i < dim1; i++){
            long dim2 = labels.size(1);
            int[] index = new int[]{i,0};
            int counter = 0;
            for(int f = 0; f < dim2; f++){
                index[1] = f;
                double valueLabel = labels.getDouble(index);
                double valuePre   = labels.getDouble(index);
                if(valueLabel>0.0){
                    counter++;
                    mse += (valuePre-valueLabel)*(valuePre-valueLabel);
                }

            }
        }
        if(bln==true) mse /= dim1;
        return mse;
    }

    
    @Override
    public INDArray computeScoreArray(INDArray inda, INDArray inda1, IActivation ia, INDArray inda2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public INDArray computeGradient(INDArray inda, INDArray inda1, IActivation ia, INDArray inda2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Pair<Double, INDArray> computeGradientAndScore(INDArray inda, INDArray inda1, IActivation ia, INDArray inda2, boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String name() {
        return "MSEPOS";
    }
    
}
