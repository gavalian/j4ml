/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.denoise;

import j4ml.networks.MeanLossFunction;
import java.util.List;
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
import org.nd4j.linalg.lossfunctions.ILossFunction;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 *
 * @author gavalian
 */
public class AutoTrainer {
    
    MultiLayerNetwork network = null;
    
    public AutoTrainer(){
        
    }
    
    public void createNetwork(){
        
        ILossFunction lossFunction = new MeanLossFunction();
        NeuralNetConfiguration.Builder builder = 
                new NeuralNetConfiguration.Builder()
                        //.seed(12345)
                        .weightInit(WeightInit.XAVIER)
                        //.updater(new Nesterovs(0.1, 0.9))
                        .updater(new AdaGrad(0.05))
                        //.updater(new Adam(0.001))
                        .activation(Activation.SIGMOID)
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .l2(0.0001);
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();
        listBuilder.layer(0, new DenseLayer.Builder().nIn(112*6).nOut(112*3)
                .weightInit(WeightInit.XAVIER)
                .build());
        listBuilder.layer(1, new DenseLayer.Builder().nIn(112*3).nOut(112)
                .weightInit(WeightInit.XAVIER)
                .build());
        listBuilder.layer(2, new DenseLayer.Builder().nIn(112).nOut(112*3)
                .weightInit(WeightInit.XAVIER)
                .build());
        listBuilder.layer(3, new OutputLayer.Builder().nIn(112*3).nOut(112*6)
                .activation(Activation.SIGMOID)
                //.weightInit(WeightInit.XAVIER)
                .lossFunction(LossFunctions.LossFunction.MSE)
                //.lossFunction(lossFunction)
                .build()
        );//.backpropType(BackpropType.Standard);
        
        network = new MultiLayerNetwork(listBuilder.build());
        network.init();
    }
    
    public DataSet createDataSet(List<LayerData> inputsList){
        List<LayerData> outputsList = LayerData.nosify(inputsList, 25);
        
        int length = inputsList.size();
        INDArray  input = Nd4j.zeros(new int[]{ length, 112*6 } );
        INDArray output = Nd4j.zeros(new int[]{ length, 112*6 } );
        for(int i = 0; i < length; i++){
            double[] inputArray = inputsList.get(i).flat();
            double[] outputArray = outputsList.get(i).flat();
            System.out.printf("IN %d OUT %d\n",inputsList.get(i).getCount(),outputsList.get(i).getCount());
            for(int k = 0; k < inputArray.length; k++){
                input.putScalar(new int[]{i,k}, inputArray[k]);
                output.putScalar(new int[]{i,k}, outputArray[k]);
            }
        }
        return new DataSet(output,input);
    }
    
    public void evaluate(DataSet test){
        INDArray output = network.output(test.getFeatures());
        
        long size = output.size(0);
        for(int i = 0; i < size; i++)
        {
            int c_in = 0;
            int c_out = 0;
            long length = output.size(1);
            for(int k = 0; k < length; k++){
                double d_in = test.getFeatures().getDouble(new int[]{i,k});
                System.out.printf("%4.6f ",d_in);
                if(d_in>0.5) c_in++;
            }
            System.out.println();
            for(int k = 0; k < length; k++){
                double d_out = output.getDouble(new int[]{i,k});
                System.out.printf("%4.6f ",d_out);
                if(d_out>0.5) c_out++;
            }
            
            System.out.println();
            System.out.println("IN " + c_in + " OUT " + c_out);
        }
    }
    
    public void train(DataSet ds, int epochs){
        System.out.println(network.summary());
        for(int i = 0; i < epochs ;i++){
            long start_time = System.currentTimeMillis();
            for(int d = 0; d < 50; d++){
                network.fit(ds);
            }
            long end_time = System.currentTimeMillis();
            long processing_time = end_time - start_time;
            System.out.printf("iter # %8d , score = %16.10f , time = %d msec\n",
                    i+1,network.score(), processing_time);
        }
    }
    
    
    public static void main(String[] args){
        List<LayerData> train = LayerData.generateSet(2000);
        
        List<LayerData> test = LayerData.generateSet(5);
        AutoTrainer autonn = new AutoTrainer();
        
        
        DataSet dsTrain = autonn.createDataSet(train);
        DataSet dsTest  = autonn.createDataSet(test);
        
        System.out.println(dsTrain);
        
        autonn.createNetwork();
        autonn.train(dsTrain,25);
        
        autonn.evaluate(dsTest);
    }
}
