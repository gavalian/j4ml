/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.network;

import deepnetts.data.TabularDataSet;
import deepnetts.net.FeedForwardNetwork;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.loss.LossType;
import deepnetts.net.train.BackpropagationTrainer;
import deepnetts.net.train.opt.OptimizerType;
import j4ml.deepnetts.data.DataSetReader;
import java.util.ArrayList;
import java.util.List;
import javax.visrec.ml.data.DataSet;

/**
 *
 * @author gavalian
 */
public class DeepNettsRegression {
    
    FeedForwardNetwork neuralNet = null;
    BackpropagationTrainer trainer  = null;
        
    public DeepNettsRegression(){}
    
     public void init(int[] layers){
        
        FeedForwardNetwork.Builder b = FeedForwardNetwork.builder();
        b.addInputLayer(layers[0]);
        for(int i = 1; i < layers.length-1; i++){
            b.addFullyConnectedLayer(layers[i], ActivationType.RELU);
        }
        b.addOutputLayer(layers[layers.length-1], ActivationType.SIGMOID)
                .lossFunction(LossType.MEAN_SQUARED_ERROR)
                .randomSeed(456);
        
        neuralNet = b.build();
        
        System.out.println(neuralNet);
    }
    
    public void initTrainer(){

        trainer = neuralNet.getTrainer();
        trainer.setMaxError(0.0000004f);
        trainer.setLearningRate(0.1f);
        trainer.setMomentum(0.9f);
        //trainer.setL1Regularization(0.0001f);
        //trainer.setOptimizer(OptimizerType.MOMENTUM);
        trainer.setOptimizer(OptimizerType.SGD);
        trainer.setMaxEpochs(200);
    }
    
    public FeedForwardNetwork getNetwork(){ return neuralNet;}
    
    public void train(DataSet ds, int nEpochs){
        
        initTrainer();
        trainer.setMaxEpochs(nEpochs);        
        trainer.setCheckpointEpochs(100);
        
        trainer.train(ds);
        
        System.out.println("accuracy = " + trainer.getTrainingAccuracy());
        System.out.println("loss = " + trainer.getTrainingLoss());
    }
    
    public List<String>  getNetworkStream(){
        List<String> stream = new ArrayList<String>();
         int nLayers = neuralNet.getLayers().size();
        for(int i = 1; i < nLayers; i++){
            System.out.println(neuralNet.getLayers().get(i-1).getWidth()
                    +","+neuralNet.getLayers().get(i).getWidth());
            stream.add(neuralNet.getLayers().get(i-1).getWidth()
                    +","+neuralNet.getLayers().get(i).getWidth());
            //System.out.println(DataSetReader.getString(neuralNet.getLayers().get(i).getWeights().getValues(),","));
            float[] weigths = neuralNet.getLayers().get(i).getWeights().getValues();
            int     width   = neuralNet.getLayers().get(i).getWidth();
            int     widthP  = neuralNet.getLayers().get(i-1).getWidth();
            for(int k = 0; k < widthP; k++){
                System.out.println(DataSetReader.getString(neuralNet.getLayers().get(i).getWeights().getValues(),
                        k*width, width,","));
                stream.add(DataSetReader.getString(neuralNet.getLayers().get(i).getWeights().getValues(),
                        k*width, width,","));
            }
            //System.out.println(DataSetReader.getString(neuralNet.getLayers().get(i).getBiases(),","));
            stream.add(DataSetReader.getString(neuralNet.getLayers().get(i).getBiases(),","));
        }
        return stream;
    }
    
    public static DataSet createDataSet(List<float[]> list){
        TabularDataSet  dataset = new TabularDataSet(6,1);
        dataset.setColumnNames(new String[]{
            "a","b","c","d","e","f", 
            "l"});
        for(int i = 0; i < list.size(); i++){
         for(int r = 0; r < 6 ; r++){
             float[] corruptInput = DataSetReader.getCopy(list.get(i));
             float[] output = new float[1];//DataSetReader.getCopy(list.get(i));
             output[0] = corruptInput[r];
             corruptInput[r] = 0.0f;
             if(i<50){
                 System.out.println(DataSetReader.getString(corruptInput) 
                         + " => " + DataSetReader.getString(output));
             }
             dataset.add(new TabularDataSet.Item(corruptInput, output)); 
         }
        }
        return dataset;
    }
    
    
    public static void main(String[] args){        
        List<float[]> trainList = 
                DataSetReader.readHipo("/Users/gavalian/Work/DataSpace/raw/extract_output.hipo", 200000);
        DeepNettsRegression regression = new DeepNettsRegression();
        regression.init(new int[]{6,12,6,1});
        
        List<float[]> trSet = DataSetReader.chunk(trainList, 0.0, 0.8);
        List<float[]> tsSet = DataSetReader.chunk(trainList, 0.8, 0.2);
        
        DataSet ds = DeepNettsRegression.createDataSet(trSet);
        regression.train(ds, 120);
        
        RegressionMetrics m = new RegressionMetrics();
        m.evaluate(regression.getNetwork(), tsSet);
    }
   
}
