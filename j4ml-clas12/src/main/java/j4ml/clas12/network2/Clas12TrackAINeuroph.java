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
public class Clas12TrackAINeuroph extends Clas12TrackAI {
    
    private NetworkTrackClassifier  classifier = new NetworkTrackClassifier();
    private NetworkTrackClassifier       fixer = new NetworkTrackClassifier();
    
    @Override
    public void init(List<String> networkFiles) {
        classifier.load(networkFiles.get(0));
        fixer.load(networkFiles.get(1));
    }

    private int getMax(double[] labels){
        double max = labels[0];
        int  index = 0;
        for(int i = 1; i < labels.length; i++){
            if(labels[i]>max){max = labels[i];index = i;}
        }
        return index;
    }
    
    @Override
    public void evaluate(ClusterCombinations comb) {
        int nrows = comb.getSize();
        for(int i = 0; i < nrows; i++){
            double[]  input = comb.getFeatures(i);
            for(int r = 0; r < 6; r++) input[r] = input[r]/112.0;
            double[] output = classifier.evaluate(input);
            int      label  = getMax(output);
            comb.setRow(i);
            comb.setStatus(label);
            comb.setProbability(output[label]);            
        }
    }
    
    private int findMissing(int[] cids){
        for(int i = 0; i < cids.length; i++){
            if(cids[i]<=0) return i;
        }
        return -1;
    }
    
    @Override
    public void fix(ClusterCombinations comb) {
        int nrows = comb.getSize();
        for(int i = 0; i < nrows; i++){
            double[]  input = comb.getFeatures(i);
            int[]     labels = comb.getLabels(i);
            for(int r = 0; r < 6; r++) input[r] = input[r]/112.0;
            double[] output = fixer.evaluate(input);
            int   missing   = findMissing(labels);
            comb.setRow(i).setMean(missing, output[missing]*112.0);            
        }
    }
    
}
