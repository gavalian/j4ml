/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.examples;

import java.util.Random;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 *
 * @author gavalian
 */
public class RegressionExample {
    public static DataSet createDataSet(int length){
        INDArray  input = Nd4j.zeros(new int[]{ length, 4 } );
        INDArray output = Nd4j.zeros(new int[]{ length, 4 } );
        Random rand = new Random();
        
        double[] data = new double[4];
        double[] result = new double[4];
        
        for(int i = 0; i < length; i++){
             for(int k = 0; k < 4; k++){
                 data[k] = rand.nextFloat();
                 if(data[k]>0.5){
                     data[k] = 1.0;
                 } else { data[k] = 0.0;}
                 input.putScalar(new int[]{i,k}, data[k]);
             }
             result[0] = data[0]*data[1];
             result[1] = data[3]*data[1]*data[2];
             result[2] = data[3]*data[2];
             result[3] = data[0]*data[3];
             output.putScalar(new int[]{i,0}, result[0]);
             output.putScalar(new int[]{i,1}, result[1]);
             output.putScalar(new int[]{i,2}, result[2]);
             output.putScalar(new int[]{i,3}, result[3]);
        }
        
        return new DataSet(input,output);
    }
    
    public static void evaluate(MultiLayerNetwork network, DataSet test){
        INDArray output = network.output(test.getFeatures());
        
        long length = output.size(0);
        for(int i = 0; i < length ; i++){
            double n1 = output.getDouble(new int[]{i,0});
            double n2 = output.getDouble(new int[]{i,1});
            double n3 = output.getDouble(new int[]{i,2});
            double n4 = output.getDouble(new int[]{i,3});
            double r1 = test.getLabels().getDouble(new int[]{i,0});
            double r2 = test.getLabels().getDouble(new int[]{i,1});
            double r3 = test.getLabels().getDouble(new int[]{i,2});
            double r4 = test.getLabels().getDouble(new int[]{i,3});
            System.out.printf("%12.6f %12.6f %12.6f %12.6f %12.6f %12.6f %12.6f %12.6f %12.6f %12.6f\n",
                    (r1-n1),(r2-n2),r1,r2,r3,r4,n1,n2,n3,n4);
        }
    }
    
    public static MultiLayerNetwork createNetwork(){
        
        NeuralNetConfiguration.Builder builder = 
                new NeuralNetConfiguration.Builder()
                        //.seed(12345)
                        .weightInit(WeightInit.XAVIER)
                        .updater(new Nesterovs(0.1, 0.9))
                        //.updater(new AdaGrad(0.05))
                        //.updater(new Adam(0.01))
                        .activation(Activation.TANH)
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .l2(0.0001);
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();
        listBuilder.layer(0, new DenseLayer.Builder().nIn(4).nOut(2)
                .weightInit(WeightInit.XAVIER)
                .build());
        /*listBuilder.layer(1, new DenseLayer.Builder().nIn(8).nOut(4)
                .weightInit(WeightInit.XAVIER)
                .build());*/
         /*listBuilder.layer(2, new DenseLayer.Builder().nIn(4).nOut(4)
                .weightInit(WeightInit.XAVIER)
                .build());*/
        listBuilder.layer(1, new OutputLayer.Builder().nIn(2).nOut(4)
                .activation(Activation.IDENTITY)
                //.weightInit(WeightInit.XAVIER)
                .lossFunction(LossFunctions.LossFunction.MSE)
                //.lossFunction(lossFunction)
                .build()
        ).backpropType(BackpropType.Standard);
        
        
        MultiLayerNetwork network = new MultiLayerNetwork(listBuilder.build());
        network.init();
        System.out.println(network.summary());
        return network;
    }
    
    public static void main(String[] args){
        
        DataSet dstrain = RegressionExample.createDataSet(80000);
        DataSet dstest  = RegressionExample.createDataSet(200);
        
        
        System.out.println(dstrain);
        MultiLayerNetwork network = RegressionExample.createNetwork();
        
        int epochs = 200;
        for(int i = 0; i < epochs ;i++){
            long start_time = System.currentTimeMillis();
            for(int d = 0; d < 50; d++){
                network.fit(dstrain);
            }
            long end_time = System.currentTimeMillis();
            long processing_time = end_time - start_time;
            System.out.printf("iter # %8d , score = %16.10f , time = %d msec\n",
                    i+1,network.score(), processing_time);
        }
        
        RegressionExample.evaluate(network, dstest);
    }
}
