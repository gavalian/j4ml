/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.examples;

import j4ml.utils.DataSetUtils;
import j4ml.visualization.DataImageMaker;
import java.util.Random;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
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
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 *
 * @author gavalian
 */
public class AutoEncoderSimple {
    
    public static String getString(double[][] data){
        StringBuilder str = new StringBuilder();

        int counter = 0;
        for(int k = 0; k < 6; k++){
            for(int i = 0; i < 10; i++){
                if(counter>0) str.append(",");
                counter++;
                str.append(String.format("%.2f", data[k][i]));
            }
        }
        return str.toString();
    }
    
    public static void nosify(double[][] data, int count){
        Random rand = new Random();
        for(int j = 0; j < count; j++){
            int x = rand.nextInt(6);
            int y = rand.nextInt(100);
            data[x][y] = 1.0;
        }
    }
    
    public static double[][] getRandomData(){
        int noise = 10;
        Random rand = new Random();
        int index = rand.nextInt(100);
            double[][] data = new double[6][100];
            for(int k = 0; k < 6; k++){
                data[k][index] = 1.0;
            }
            return data;
    }
    
    public static DataSet getRandomDataSet(int length){
         
        INDArray  input = Nd4j.zeros(new int[]{ length, 600 } );
        INDArray output = Nd4j.zeros(new int[]{ length, 600 } );
        for(int i = 0; i < length; i++){
            double[][] data = AutoEncoderSimple.getRandomData();
            int count = 0;

            for(int x = 0; x < 6; x++){
                for(int y = 0; y < 100; y++){
                    output.putScalar(new int[]{i,count}, data[x][y]);
                    count++;
                }
            }
            
            AutoEncoderSimple.nosify(data, 20);
            count = 0;

            for(int x = 0; x < 6; x++){
                for(int y = 0; y < 100; y++){
                    input.putScalar(new int[]{i,count}, data[x][y]);
                    count++;
                }
            }
        }
        return new DataSet(input,output);
    }
    
    /*public static void produceDataSet(int size){
        int noise = 10;
        Random rand = new Random();
        for(int i = 0; i < size; i++){
            int index = rand.nextInt(10);
            double[][] data = new double[6][10];
            for(int k = 0; k < 6; k++){
                data[k][index] = 1.0;
            }
            
            for(int j = 0; j < noise; j++){
                int x = rand.nextInt(6);
                int y = rand.nextInt(10);
                data[x][y] = 1.0;
            }
            String dataString = AutoEncoderSimple.getString(data);
            System.out.println(dataString);
        }
    }*/
    
    
    public static void showArray(INDArray array){
        for(int i = 0; i < array.size(0); i++){
            for(int k = 0; k < array.size(1); k++){
                System.out.printf("%4.2f ", array.getDouble(new int[]{i,k}));
            }
            System.out.println();
        }
    }
    public static void main(String[] args){
        //AutoEncoderSimple.produceDataSet(20);
        DataSet ds = AutoEncoderSimple.getRandomDataSet(500);
        System.out.println(ds.toString());
        
        NeuralNetConfiguration.Builder builder = 
                new NeuralNetConfiguration.Builder()
                        //.seed(12345)
                        .weightInit(WeightInit.XAVIER)
                        .updater(new AdaGrad(0.15))
                        .activation(Activation.SIGMOID)
                        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                        .l2(0.0001);
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();
        listBuilder.layer(0, new DenseLayer.Builder().nIn(600).nOut(300)
                .weightInit(WeightInit.XAVIER)
                .build());
        listBuilder.layer(1, new DenseLayer.Builder().nIn(300).nOut(100)
                .weightInit(WeightInit.XAVIER)
                .build());
        listBuilder.layer(2, new DenseLayer.Builder().nIn(100).nOut(300)
                .weightInit(WeightInit.XAVIER)
                .build());
        listBuilder.layer(3, new OutputLayer.Builder().nIn(300).nOut(600)
                //.activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .lossFunction(LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY)
                //.lossFunction(LossFunctions.LossFunction.MCXENT)
                .build());
        
        MultiLayerNetwork network = new MultiLayerNetwork(listBuilder.build());
        network.init();
        System.out.println(network.summary());
        for(int i = 0; i < 250 ;i++){
            long start_time = System.currentTimeMillis();
            for(int d = 0; d < 100; d++){
                network.fit(ds);
            }
            long end_time = System.currentTimeMillis();
            long processing_time = end_time - start_time;
            System.out.printf("iter # %8d , score = %16.10f , time = %d msec\n",
                    i+1,network.score(), processing_time);
        }
        
        DataSet test = AutoEncoderSimple.getRandomDataSet(20);
        
        INDArray output = network.output(test.getFeatures());

        System.out.println("---------> INFERENCE");
        AutoEncoderSimple.showArray(test.getFeatures());
        System.out.println("---------> OUTPUT");
        AutoEncoderSimple.showArray(output);
        
        for(int i = 0; i < 20; i++){
            double[]   input = DataSetUtils.getRow(test.getFeatures(), i);
            double[] outdata = DataSetUtils.getRow(output, i);
            DataSetUtils.normalize(outdata);
            DataImageMaker.saveImage(input, 100,6, "img_input_"+ i + ".png");
            DataImageMaker.saveImage(outdata, 100,6, "img_infered_"+i+".png");
        }
        
    }
}
