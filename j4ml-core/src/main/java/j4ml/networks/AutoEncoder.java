/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import java.util.Collections;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

/**
 *
 * @author gavalian
 */
public class AutoEncoder {
    private MultiLayerNetwork network = null;
    private int   iterationPerEpoch = 40;
    
    private Activation activation = Activation.TANH;
    private double     dropOut    = 0.0;
    
    public AutoEncoder(int inputSize, int[] hiddenLayers){
        network = createNetwork(inputSize, hiddenLayers);
        network.init();
    }
    
    public MultiLayerConfiguration createConfiguration(int inputSize, int[] hiddenLayers){
        MultiLayerConfiguration conf;
        NeuralNetConfiguration.Builder builder = 
                new NeuralNetConfiguration.Builder()
                        //.seed(12345)
                        .weightInit(WeightInit.XAVIER)
                        //.updater(new AdaGrad(0.15))
                        //.updater(new Nesterovs(0.1, 0.9))
                        .updater(new Adam(0.01))
                        .activation(activation)
                        .l2(0.0001)
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
                        //.l2(0.0001);
        
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();
        
        int counter = 0;
        listBuilder.layer(counter, new DenseLayer.Builder().nIn(inputSize).nOut(hiddenLayers[0])
                .weightInit(WeightInit.XAVIER)
                .activation(activation)
                .dropOut(dropOut)
                .build());
        
        counter++;
        
        for(int i = 1 ; i < hiddenLayers.length; i++){
            listBuilder.layer(counter, new DenseLayer.Builder().nIn(hiddenLayers[i-1])
                    .nOut(hiddenLayers[i]).weightInit(WeightInit.XAVIER)
                    .activation(activation)
                    .dropOut(dropOut)
                    //.activation(Activation.RELU)
                    //.dropOut(0.5)
                    .build());
            counter++;
        }
        
        for(int i = hiddenLayers.length-2; i >= 0 ; i--){
            listBuilder.layer(counter, new DenseLayer.Builder().nIn(hiddenLayers[i+1])
                    .nOut(hiddenLayers[i]).weightInit(WeightInit.XAVIER)
                    .activation(activation)
                    .dropOut(dropOut)
                    //.activation(Activation.RELU)
                    .build());
            counter++;
        }
        listBuilder.layer(counter, new OutputLayer.Builder().nIn(hiddenLayers[0]).nOut(inputSize)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.IDENTITY)
                .lossFunction(LossFunctions.LossFunction.MSE)
                //.lossFunction(LossFunction.RECONSTRUCTION_CROSSENTROPY)
                //.lossFunction(new MeanLossFunction())                
                .build());//.backpropType(BackpropType.Standard);
        
        
        /*listBuilder.layer(0, new DenseLayer.Builder().nIn(50).nOut(25)
            .build());
        listBuilder.layer(1, new OutputLayer.Builder().nIn(25).nOut(2)
            .lossFunction(LossFunctions.LossFunction.MSE).build());*/
        
        return listBuilder.build();
    }
    
    public void setMaxIterations(int iter){ this.iterationPerEpoch = iter;}
    
    public final MultiLayerNetwork createNetwork(int inputSize, int[] hiddenLayers){
        MultiLayerConfiguration config = createConfiguration(inputSize, hiddenLayers);
        //System.out.println(config.toJson());
        MultiLayerNetwork networkLocal = new MultiLayerNetwork(config);
        
        return networkLocal;
    }
    
    public MultiLayerConfiguration createConfiguration(){
       /* MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
       .seed(12345)
       .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
       .gradientNormalizationThreshold(1.0)
       
       //.momentumAfter(Collections.singletonMap(3, 0.9))
       .optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT)
       .list()
       .layer(0, new AutoEncoder.Builder().nIn(numRows * numColumns).nOut(500)
               .weightInit(WeightInit.XAVIER).lossFunction(LossFunction.RMSE_XENT)
               .corruptionLevel(0.3)
               .build())
            .layer(1, new AutoEncoder.Builder().nIn(500).nOut(250)
                    .weightInit(WeightInit.XAVIER).lossFunction(LossFunction.RMSE_XENT)
                    .corruptionLevel(0.3)

                    .build())
            .layer(2, new AutoEncoder.Builder().nIn(250).nOut(200)
                    .weightInit(WeightInit.XAVIER).lossFunction(LossFunction.RMSE_XENT)
                    .corruptionLevel(0.3)
                    .build())
            .layer(3, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD).activation("softmax")
                    .nIn(200).nOut(outputNum).build())
       .pretrain(true).backprop(false)
            .build();*/
       return null;
    }
    
    public final MultiLayerNetwork getNetwork(){return network;}
    
    public void train(DataSet trainingSet, int nEpochs){
        
         System.out.println(network.summary());
        //System.out.println(dsTrain);
        for(int i = 0; i < nEpochs; i++){
            long start_time = System.currentTimeMillis();
            for(int iter = 0; iter < iterationPerEpoch; iter++){
                //long elapsed_time = System.currentTimeMillis() - start_time;
                network.fit(trainingSet);
                //System.out.printf("time - %5d >>> score = %16.10f\r",elapsed_time,network.score());
                //System.err.println("");
            }
            long end_time = System.currentTimeMillis();
            double elapsed_seconds = ((double) (end_time-start_time))/1000.0;
            System.out.printf("iter # %8d/%d , score = %16.10f -- %8.3f seconds\n",
                    i+1,nEpochs,network.score(),elapsed_seconds);
        }
        
    }
    
    public static void main(String[] args){
        AutoEncoder encoder = new AutoEncoder(40, new int[]{30,20,10});
        
        MultiLayerNetwork network = encoder.createNetwork(40, new int[]{30,20,10});
        network.init();
        System.out.println(network.summary());
    }
}
