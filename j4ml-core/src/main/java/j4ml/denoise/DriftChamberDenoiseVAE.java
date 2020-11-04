/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.denoise;

import j4ml.networks.AutoEncoder;
import j4ml.readers.LibSVMReader;
import j4ml.readers.TextFileReader;
import j4ml.readers.TextFileWriter;
import j4ml.utils.DataSetUtils;
import j4ml.visualization.DataImageMaker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.variational.BernoulliReconstructionDistribution;
import org.deeplearning4j.nn.conf.layers.variational.VariationalAutoencoder;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.SamplingDataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 *
 * @author gavalian
 */
public class DriftChamberDenoiseVAE {
    
    public static DataSet readDataSet(String filename, int length){
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        INDArray  input = Nd4j.zeros(new int[]{ length, 112*36 } );
        INDArray output = Nd4j.zeros(new int[]{ length, 112*36 } );
        
        
        for(int i = 0; i < length; i++){
            List<String> lines = reader.readLines(2);
            double[] desired = LibSVMReader.getDataRow(lines.get(0),112*36);
            double[] initial = LibSVMReader.getDataRow(lines.get(1),112*36);
            DataSetUtils.putRow( input, i, initial);
            DataSetUtils.putRow(output, i, desired);            
        }
        
        return new DataSet(input,output);
    }
    
    
    public static void evaluate(MultiLayerNetwork network, DataSet test, String prefix, double threshold){
        INDArray   input = test.getFeatures();
        INDArray desired = test.getLabels();
        
        INDArray output = network.output(input);
        
        String outFile = "performance.txt";
        TextFileWriter writer = new TextFileWriter();
        writer.open(outFile);
        long        size = input.size(0);
        for(int i = 0; i < size; i++){
            int[] results = DataSetUtils.compare(desired, output, i, threshold);
                String dataString = DataSetUtils.intArrayString(results, "%5d ");
                writer.writeString(dataString);
            if(i<50){
                double[] inImage = DataSetUtils.getRow(input, i);
                double[] desImage = DataSetUtils.getRow(desired, i);
                double[] outImage = DataSetUtils.getRow(output, i);
                //DataSetUtils.normalize(outImage);
                DataSetUtils.threshold(outImage, threshold);

                /*DataImageMaker.saveImage(inImage, 112, 36, String.format("dc_image_input_%d.png", i),4,8);
                DataImageMaker.saveImage(desImage, 112, 36, String.format("dc_image_desired_%d.png", i),4,8);
                DataImageMaker.saveImage(outImage, 112, 36, String.format("dc_image_output_%d.png", i),4,8);
                */

              
                DataImageMaker.saveImage(inImage, desImage, outImage, 112, 36, String.format(prefix,i));
                //System.out.println(DataSetUtils.doubleArrayString(outImage, "%6.3f ", 112));
            }
        }
        writer.close();
        
    }
    
    public static void main(String[] args){

        
        int nEpochs  = 25;
        int nEntries = 5000;
        int reconstructionNumSamples = 16;
        
        if(args.length>0){
            nEpochs = Integer.parseInt(args[0]);
        }
        
        if(args.length>1){
            nEntries = Integer.parseInt(args[1]);
        }
        DataSet trainData = DriftChamberDenoiseVAE.readDataSet("driftchamber_tracks.lsvm", nEntries);
        DataSet  testData = DriftChamberDenoiseVAE.readDataSet("driftchamber_tracks_test.lsvm", 500);
        System.out.println(trainData);
        
        System.out.println("---- TRAINING DATA SIZE : " + trainData.getFeatures().size(0));
        System.out.println("---- TESTING  DATA SIZE : " + testData.getFeatures().size(0));
        
        
        /*
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .optimizationAlgo(OptimizationAlgorithm.LINE_GRADIENT_DESCENT)
                .list()
                .layer(0, new RBM.Builder().nIn(numRows * numColumns).nOut(1000).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
                .layer(1, new RBM.Builder().nIn(1000).nOut(500).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
                .layer(2, new RBM.Builder().nIn(500).nOut(250).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
                .layer(3, new RBM.Builder().nIn(250).nOut(100).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
                .layer(4, new RBM.Builder().nIn(100).nOut(30).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build()) //encoding stops
                .layer(5, new RBM.Builder().nIn(30).nOut(100).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build()) //decoding starts
                .layer(6, new RBM.Builder().nIn(100).nOut(250).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
                .layer(7, new RBM.Builder().nIn(250).nOut(500).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
                .layer(8, new RBM.Builder().nIn(500).nOut(1000).lossFunction(LossFunctions.LossFunction.KL_DIVERGENCE).build())
                .layer(9, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.SIGMOID).nIn(1000).nOut(numRows*numColumns).build())
                .pretrain(true).backprop(true)
                .build();
        
        */
        
        NeuralNetConfiguration.Builder builder
                = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001));
        
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();
                                              
        listBuilder.layer(new org.deeplearning4j.nn.conf.layers.AutoEncoder.Builder()
                .nIn(36*112).nOut(56)
                .corruptionLevel(0.6)
                .lossFunction(LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY).build());
        listBuilder.layer(new OutputLayer.Builder().nIn(56).nOut(36*112)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.SIGMOID)
                .lossFunction(LossFunctions.LossFunction.MSE).build());
        MultiLayerConfiguration conf = listBuilder.build();
                
                
        
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();
        System.err.println(network.summary());
        
         for(int i = 0; i < nEpochs; i++){
            long start_time = System.currentTimeMillis();
            for(int iter = 0; iter < 2; iter++){
                //long elapsed_time = System.currentTimeMillis() - start_time;
                network.fit(trainData);
                //System.out.printf("time - %5d >>> score = %16.10f\r",elapsed_time,network.score());
                //System.err.println("");
            }
            long end_time = System.currentTimeMillis();
            double elapsed_seconds = ((double) (end_time-start_time))/1000.0;
            System.out.printf("iter # %8d/%d , score = %16.10f -- %8.3f seconds\n",
                    i+1,nEpochs,network.score(),elapsed_seconds);
        }
         DriftChamberDenoise.evaluate(network, testData, "vae_test_test_%d.png",0.5);   
    }
}
