/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
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
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

/**
 *
 * @author gavalian
 */
public class MLPRegression {
    private int nInputs       = 0;
    private int nOutputs = 0;
    private int[] hiddenLayers = null;
    private int   iterationPerEpoch = 50;
    private MultiLayerNetwork network = null;
    public static final int  MLP_RELU =1;
    public static final int  MLP_TANH =2;
    public static final int  MLP_IDENTITY =3;
    
    private int HIDDEN_LAYER_TYPES = 1;
    
    public MLPRegression(int nIn, int nOut, int[] layers){
        nInputs = nIn;
        nOutputs = nOut;
        hiddenLayers = layers;
    }
    
    public MLPRegression(int nIn, int nOut, int[] layers, int type){
        nInputs = nIn;
        nOutputs = nOut;
        hiddenLayers = layers;
        this.HIDDEN_LAYER_TYPES = type;
    }
    
    public MultiLayerConfiguration createConfiguration(){
        MultiLayerConfiguration conf;
        NeuralNetConfiguration.Builder builder
                = new NeuralNetConfiguration.Builder()
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .updater(new Adam(0.001));
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();
        
        
          
        listBuilder.layer(new DenseLayer.Builder().nIn(nInputs)
                .nOut(hiddenLayers[0]).weightInit(WeightInit.XAVIER)
                .dropOut(0.5).activation(Activation.RELU).build());
        
        
        for(int i = 0; i < hiddenLayers.length - 1; i++){
                listBuilder.layer(new DenseLayer.Builder().nIn(hiddenLayers[i]).
                        nOut(hiddenLayers[i+1]).weightInit(WeightInit.XAVIER)
                        .dropOut(0.5).activation(Activation.RELU).build());
        }
        /*if(this.HIDDEN_LAYER_TYPES==MLPRegression.MLP_TANH){
            listBuilder.layer(new OutputLayer.Builder(LossFunction.MSE)
                    .activation(Activation.TANH)
                    .nIn(hiddenLayers[hiddenLayers.length - 1]).nOut(nOutputs).build());
        } else {
         listBuilder.layer(new OutputLayer.Builder(LossFunction.MSE).dropOut(0.5)
                    .activation(Activation.RELU)
                 .nIn(hiddenLayers[hiddenLayers.length - 1]).nOut(nOutputs).build());   
        }*/
        
        listBuilder.layer(new OutputLayer.Builder(LossFunction.MSE).dropOut(0.5)
                    .activation(Activation.IDENTITY)
                 .nIn(hiddenLayers[hiddenLayers.length - 1]).nOut(nOutputs).build());
        //System.err.println("FINAL ACTIVATION FUNCTION : LINEAR - ALL linear");
        return listBuilder.build();
    }
    
    public MultiLayerNetwork createNetwork(){
        MultiLayerConfiguration config = createConfiguration();
        //System.out.println(config.toJson());
        MultiLayerNetwork network = new MultiLayerNetwork(config);
        
        return network;
    }
       
    public void train(DataSet trainData, DataSet testData, int nEpochs){
        network = createNetwork();
        network.init();
        System.err.println(network.summary());
         for(int i = 0; i < nEpochs; i++){
             long start_epoch = System.currentTimeMillis();
            for(int iter = 0; iter < iterationPerEpoch; iter++){
                network.fit(trainData);
            }
            long end_epoch = System.currentTimeMillis();
            
            double time_epoch = ((double) (end_epoch-start_epoch))/1000.0;
            
            System.out.printf("iter # %8d/%8d >>>> time = %9.4f sec, score = %12.9f\n",
                    i+1,nEpochs,time_epoch,
                    network.score());
        }
    }
    
    public void train(DataSet trainData, int nEpochs){
        network = createNetwork();
        network.init();
         for(int i = 0; i < nEpochs; i++){
            for(int iter = 0; iter < iterationPerEpoch; iter++){
                network.fit(trainData);
            }
            System.out.printf("iter # %8d , score = %12.6f\n",i,network.score());
        }
    }
    
    public void evaluate(DataSet ds, String filename){
        
        INDArray  input = ds.getFeatures();
        INDArray output = ds.getLabels();
        
        INDArray  inferred = network.output(input);
        
        long nRows = inferred.size(0);
        long nColumns = inferred.size(1);
        
        for(int row = 0; row < nRows; row++){
            StringBuilder str = new StringBuilder();
            for(int column = 0; column < nColumns; column++){
                double valueTrue = output.getDouble(new int[]{row,column});
                double valueRef  = inferred.getDouble(new int[]{row,column});
                str.append(String.format("%8.5f %8.5f ",valueTrue,valueRef));
            }
            System.out.println(str.toString());
        }
    }
    
}
