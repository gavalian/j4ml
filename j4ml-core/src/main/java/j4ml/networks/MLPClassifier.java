/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 *
 * @author gavalian
 */
public class MLPClassifier {
    
    private int nInputs       = 0;
    private int outputClasses = 0;
    private int[] hiddenLayers = null;
    private int   iterationPerEpoch = 100;
    private String modelFileName    = null;
    
    
    
    public MLPClassifier(int nIn, int[] layers, int nClasses){
        nInputs = nIn;
        outputClasses = nClasses;
        hiddenLayers = layers;
    }
    
    public MLPClassifier setOutput(String file){
        this.modelFileName = file;
        return this;
    }
    
    public MLPClassifier setIterations(int iter){
        this.iterationPerEpoch = iter;
        return this;
    }
    
    public void saveModel(MultiLayerNetwork network,String filename){
        System.out.println("[MLP Classifier] --> saving file : " + filename);
        try {
            network.save(new File(filename));
        } catch (IOException ex) {
            Logger.getLogger(MLPClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public MultiLayerConfiguration createConfiguration(){
        
        MultiLayerConfiguration conf;
        
        NeuralNetConfiguration.Builder builder
                = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001));
        
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();
        
        listBuilder.layer(new DenseLayer.Builder().nIn(nInputs).nOut(hiddenLayers[0]).weightInit(WeightInit.XAVIER).activation(Activation.RELU).build());
        for(int i = 0; i < hiddenLayers.length - 1; i++){
            listBuilder.layer(new DenseLayer.Builder().nIn(hiddenLayers[i])
                    .nOut(hiddenLayers[i+1]).weightInit(WeightInit.XAVIER)
                    .dropOut(0.5)
                    .activation(Activation.RELU).build());
        }
        
        listBuilder.layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(hiddenLayers[hiddenLayers.length - 1]).nOut(outputClasses).build());
        
        return listBuilder.backpropType(BackpropType.Standard).build();
    }
    
    public void train(DataSet dsTrain, DataSet dsTest, int epoch){
        
        MultiLayerNetwork network = createNetwork();
        network.init();
        long start_time = System.currentTimeMillis();
        
        System.out.println(network.summary());
        //System.out.println(dsTrain);
        for(int i = 0; i < epoch; i++){
            for(int iter = 0; iter < iterationPerEpoch; iter++){
                long elapsed_time = System.currentTimeMillis() - start_time;
                network.fit(dsTrain);
                //System.out.printf("time - %5d >>> score = %16.10f\r",elapsed_time,network.score());
                //System.err.println("");
            }
            System.out.printf("iter # %8d , score = %16.10f\n",i+1,network.score());
        }
        
        
        
        if(dsTest!=null){
            
            INDArray lables   = dsTest.getLabels();
            INDArray features = dsTest.getFeatures();
            Evaluation eval = new Evaluation(outputClasses);
            long evalStartTime = System.currentTimeMillis();
            INDArray predicted = network.output(features,false);
            long evalEndTime   = System.currentTimeMillis();
        
            int columns = lables.columns();
            System.out.printf("ELAPSED TIME = %d msec\n",evalEndTime - evalStartTime);
            /*for(int i = 0; i < columns; i++){
                System.out.printf("column %5d, size : %8d\n",i,lables.size(i));
            }*/
            System.out.println(predicted.toString());
            
            eval.eval(lables, predicted);
            
            System.out.println(eval.stats());
        }
        if(this.modelFileName!=null){
            this.saveModel(network, modelFileName);
        }
    }
    
    public MultiLayerNetwork createNetwork(){
        MultiLayerConfiguration config = createConfiguration();
        //System.out.println(config.toJson());
        MultiLayerNetwork network = new MultiLayerNetwork(config);
        
        return network;
    }
    
    public static MultiLayerNetwork loadNetwork(String file){
        MultiLayerNetwork nnet = null;
        try {
            nnet = MultiLayerNetwork.load(new File(file), false);
        } catch (IOException ex) {
            Logger.getLogger(MLPClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nnet;
    }

}
