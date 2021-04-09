/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.ejml;

import j4ml.clas12.network.NeuralNetworkTracking;
import j4ml.clas12.tracking.ClusterCombinations;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class EJMLTrackNeuralNetwork extends NeuralNetworkTracking {
    
    private EJMLModelEvaluator networkClassifier = null;//new EJMLModelEvaluator();
    private EJMLModelEvaluator networkFixer = null;//new EJMLModelEvaluator();
    
    public EJMLTrackNeuralNetwork(){
        
    }

    private int getMaxIndex(float[] data){
        int index = 0;
        float max = 0.0f;
        for(int i = 1; i < data.length;i++)
            if(data[i]>max){
                max = data[i]; index = i;
            }
        return index;
    }
    
    @Override
    public void classify(ClusterCombinations comb) {
        int nrows = comb.getSize();
        float[] features = new float[6];
        float[] output   = new float[3];
        for(int i = 0; i < nrows; i++){
            //System.out.println("features -> " + Arrays.toString(features));
            comb.getFeatures(features, i);
            networkClassifier.feedForwardSoftmax(features,output);
            int index = getMaxIndex(output);
            float value = output[index];
            if(value>0.20){
                comb.setRow(i).setStatus(index);
                comb.setRow(i).setProbability(value);
            }
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fix(ClusterCombinations comb) {
        int nrows = comb.getSize();
        float[] features = new float[6];        
        float[] output   = new float[6];
        for(int i = 0; i < nrows; i++){
            int index = -1;
            comb.getFeatures(features, i);
            for(int k = 0; k < features.length; k++)
                if(features[k]<0.0001) index = k;
            
            if(index>=0){
                networkFixer.feedForward(features, output);
                comb.setRow(i).setMean(index, output[index]*112.0);
                
            }
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void evaluate(ClusterCombinations comb) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init(List<String> fileList) {
        System.out.println("[EJML-NN] >> reading classifier file : " + fileList.get(0));
        networkClassifier = new EJMLModelEvaluator(fileList.get(0));
        System.out.println("[EJML-NN] >> reading fixer  file : " + fileList.get(1));
        networkFixer = new EJMLModelEvaluator(fileList.get(1));
    }
}