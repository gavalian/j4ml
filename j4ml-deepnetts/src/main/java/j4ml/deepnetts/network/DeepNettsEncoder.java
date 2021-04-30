/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.network;

import deepnetts.data.TabularDataSet;
import deepnetts.net.FeedForwardNetwork;
import deepnetts.net.NeuralNetwork;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.loss.LossType;
import deepnetts.net.train.BackpropagationTrainer;
import deepnetts.net.train.opt.OptimizerType;
import deepnetts.util.FileIO;
import j4ml.deepnetts.data.DataSetReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.visrec.ml.data.DataSet;
import org.jlab.jnp.readers.TextFileWriter;

/**
 *
 * @author gavalian
 */
public class DeepNettsEncoder {
    
    FeedForwardNetwork neuralNet = null;
    BackpropagationTrainer trainer  = null;
        
    public DeepNettsEncoder(){}
    
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
    
    public void train(DataSet ds, int nEpochs){
        
        initTrainer();
        trainer.setMaxEpochs(nEpochs);        
        trainer.setCheckpointEpochs(100);
        
        trainer.train(ds);
        
        System.out.println("accuracy = " + trainer.getTrainingAccuracy());
        System.out.println("loss = " + trainer.getTrainingLoss());
    }
    
    public static float[] getVector(String vecString, int vecSize){
        float[] vector = new float[vecSize];
        String[] tokens = vecString.split("\\s+");
        for(int i = 1; i < tokens.length; i++){
            String[] pair = tokens[i].split(":");
            int item = Integer.parseInt(pair[0]);
            float value = Float.parseFloat(pair[1]);
            vector[item-1] = value;
        }
        return vector;
    }
    
    public  FeedForwardNetwork getNetwork(){ return this.neuralNet; }
    
    
    public static List<float[]> readDataArray(String filename, int max, int skip){
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        int counter = 0;
        
        for(int s = 0; s < skip ; s++){
            List<String> lines = reader.readLines(1);
        }
        List<float[]> dataList = new ArrayList<>();
        while(true){
            List<String> lines = reader.readLines(1);
            if(lines.size()<1) break;
            if(lines.get(0).length()<3) break;
            //System.out.println("-> " + lines.get(0));
            float[] input = DeepNettsClassifier.getVector(lines.get(0),6);
            String[] tokens = lines.get(0).split("\\s+");
            Integer  itemClass = Integer.parseInt(tokens[0]);
            if(itemClass>0) dataList.add(input);
        }
        return dataList;
    }
    
    public static DataSet readDataset(String filename, int max, int skip){
        TabularDataSet  dataset = new TabularDataSet(6,6);

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
            float[] input = DeepNettsClassifier.getVector(lines.get(0),6);
            String[] tokens = lines.get(0).split("\\s+");
            Integer  itemClass = Integer.parseInt(tokens[0]);
            if(itemClass>0){
                for(int k = 0; k < 6; k++){
                    float[] corruptInput = new float[6];
                    float[] output = new float[6];
                    for(int r = 0; r < 6; r++){
                        corruptInput[r] = input[r];
                        output[r] = input[r];
                    }
                    corruptInput[k] = 0;
                    dataset.add(new TabularDataSet.Item(corruptInput, output)); 
                    counter++;
                }
                //counter++;
            }
            /*float[] output = new float[]{0.0f,0.0f,0.0f};
            String[] tokens = lines.get(0).split("\\s+");

            output[itemClass]  = 1.0f;*/
            //dataset.add(new DataSetRow(input,output));
            //dataset


            if(counter>=max) break;
        }
        System.out.printf("[READ-DATASET] data set imported, rows = %d\n",counter);
        //System.out.println(dataset);
        return dataset;
    }
    
    public static DataSet createDataset(List<float[]> list){
        TabularDataSet  dataset = new TabularDataSet(6,6);
        dataset.setColumnNames(new String[]{
            "a","b","c","d","e","f", 
            "l","m","n","o","p","q"});
        for(int i = 0; i < list.size(); i++){
         for(int r = 0; r < 6 ; r++){
             float[] corruptInput = DataSetReader.getCopy(list.get(i));
             float[] output = DataSetReader.getCopy(list.get(i));
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
    
    public void save(String filename){
        TextFileWriter writer = new TextFileWriter();
        writer.open(filename);
        
        System.out.println("-------- Neural Network -----"); 
        System.out.println(neuralNet.getLayers().size());
        int nLayers = neuralNet.getLayers().size();
        for(int i = 1; i < nLayers; i++){
            System.out.println(neuralNet.getLayers().get(i-1).getWidth()
                    +","+neuralNet.getLayers().get(i).getWidth());
            writer.writeString(neuralNet.getLayers().get(i-1).getWidth()
                    +","+neuralNet.getLayers().get(i).getWidth());
            //System.out.println(DataSetReader.getString(neuralNet.getLayers().get(i).getWeights().getValues(),","));
            float[] weigths = neuralNet.getLayers().get(i).getWeights().getValues();
            int     width   = neuralNet.getLayers().get(i).getWidth();
            int     widthP  = neuralNet.getLayers().get(i-1).getWidth();
            for(int k = 0; k < widthP; k++){
                System.out.println(DataSetReader.getString(neuralNet.getLayers().get(i).getWeights().getValues(),
                        k*width, width,","));
                writer.writeString(DataSetReader.getString(neuralNet.getLayers().get(i).getWeights().getValues(),
                        k*width, width,","));
            }
            System.out.println(DataSetReader.getString(neuralNet.getLayers().get(i).getBiases(),","));
            writer.writeString(DataSetReader.getString(neuralNet.getLayers().get(i).getBiases(),","));
        }
        System.out.println("-------- End of Neural Network -----");
        writer.close();
        
        /*
        System.out.println("-------- Neural Network -----");
        for(int i = 1; i < 7; i++){
            System.out.println(neuralNet.getLayers().get(i-1).getWidth()
                    +","+neuralNet.getLayers().get(i).getWidth());
            System.out.println(DataSetReader.getString(neuralNet.getLayers().get(i).getWeights().getValues(),","));
            System.out.println(DataSetReader.getString(neuralNet.getLayers().get(i).getBiases(),","));
        }
        System.out.println("-------- End of Neural Network -----");
        */
        
        /*try {
            FileIO.writeToFile(neuralNet, filename + ".deepnetts");
        } catch (IOException ex) {
            Logger.getLogger(DeepNettsClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            FileIO.writeToFileAsJson(neuralNet, filename + "_deepnetts.json");
        } catch (IOException ex) {
            Logger.getLogger(DeepNettsClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    public static void main(String[] args){
        //DataSet trainingSet = DeepNettsEncoder.readDataset("dc_combined_sample_70k.lsvm", 300000, 0);
        //List<float[]> testingSet = DeepNettsEncoder.readDataArray("dc_combined_sample_70k_test.lsvm", 40000, 0);
        //DataSet trainingSet = DeepNettsEncoder.readDataset("dc_negative_train.lsvm", 300000, 0);
        //List<float[]> testingSet = DeepNettsEncoder.readDataArray("dc_negative_test.lsvm", 40000, 0);
        
        List<float[]> trainList = DataSetReader.readHipo("/Users/gavalian/Work/DataSpace/raw/extract_output.hipo", 250000);
        
        List<float[]> trSet = DataSetReader.chunk(trainList, 0.0, 0.8);
        List<float[]> tsSet = DataSetReader.chunk(trainList, 0.8, 0.2);
        
        System.out.println(" data imported, size = " + trainList.size() + "  set size = " + trSet.size() + " test = " + tsSet.size());
        for(int i = 0; i < 20; i++){
            System.out.println(DataSetReader.getString(trainList.get(i)));
        }
        
        DataSet data = DeepNettsEncoder.createDataset(trSet);
        //DataSet[] chunks = data.split(0.8,0.2);
        data.shuffle();
        DeepNettsEncoder encoder = new DeepNettsEncoder();
        encoder.init(new int[]{6,12,12,6,12,12,6});
        encoder.train(data, 800);
        encoder.save("autoEncoderTrack");
        
        AutoEncoderMetrics metrics = new AutoEncoderMetrics();        
        metrics.evaluate(encoder.getNetwork(), tsSet);
        
        /*        
        System.out.println(trainingSet);
        trainingSet.setColumnNames(new String[]{
            "a","b","c","d","e","f", 
            "l","m","n","o","p","q"});
        trainingSet.shuffle();
        DeepNettsEncoder encoder = new DeepNettsEncoder();
        encoder.init(new int[]{6,12,12,12,6,12,12,12,6});
        encoder.train(trainingSet, 250);
        
        AutoEncoderMetrics metrics = new AutoEncoderMetrics();
        metrics.evaluate(encoder.getNetwork(), testingSet );*/
    }
}
