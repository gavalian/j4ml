/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import java.util.List;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

/**
 *
 * @author gavalian
 */
public class RunTrackClassifier {
    
    public static double[] getVector(String vecString, int vecSize){
        double[] vector = new double[vecSize];
        String[] tokens = vecString.split("\\s+");
        for(int i = 1; i < tokens.length; i++){
            String[] pair = tokens[i].split(":");
            int item = Integer.parseInt(pair[0]);
            double value = Double.parseDouble(pair[1]);
            vector[item-1] = value;
        }
        return vector;
    }
    
    public static DataSet readDataset(String filename, int max, int skip){
        DataSet  dataset = new DataSet(6,3);

        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        int counter = 0;
        
        for(int s = 0; s < skip ; s++){
            List<String> lines = reader.readLines(1);
        }
        
        while(true){
            List<String> lines = reader.readLines(1);
            if(lines.size()<1) break;
            if(lines.get(0).length()<3) break;
            //System.out.println("-> " + lines.get(0));
            double[] input = RunTrackClassifier.getVector(lines.get(0),6);
            double[] output = new double[]{0.0,0.0,0.0};
            String[] tokens = lines.get(0).split("\\s+");
            Integer  itemClass = Integer.parseInt(tokens[0]);
            output[itemClass]  = 1.0;
            dataset.add(new DataSetRow(input,output));
            counter++;
            if(counter>=max) break;
        }
        System.out.printf("[READ-DATASET] data set imported, rows = %d\n",counter);
        return dataset;
    }
    
    public static void main(String[] args){
        
        
        if(args.length<2) {
            System.out.println(">>>> please provide parameters\n\n");
        }
        
        int nEpochs = Integer.parseInt(args[0]);
        
        int nLayers = args.length-1;
        int[] layerConfig = new int[nLayers];
        
        for(int i = 0; i < nLayers; i++){
            layerConfig[i] = Integer.parseInt(args[i+1]);
        }
        
        String filename     = "dc_combined_sample_70k.lsvm";
        String filenameTest = "dc_combined_sample_70k_test.lsvm";
        NeurophMLPClassifier classifier = new NeurophMLPClassifier();
        
        int nsamples = 100000;
        //classifier.init(new int[]{6,24,24,3});
        classifier.init(layerConfig);
        classifier.configure();
        DataSet trainingSet = RunTrackClassifier.readDataset(filename,nsamples,0);
        DataSet testingSet  = RunTrackClassifier.readDataset(filenameTest, 40000,0);
        trainingSet.shuffle();
        classifier.train(trainingSet,testingSet, nEpochs);
        
        classifier.save("trackClassifier.nnet");
        //classifier.train(trainingSet, nEpochs);
        
        //classifier.evaluate(trainingSet);
        
        //classifier.evaluate(testingSet);
    }
}
