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
import java.util.List;
import java.util.Random;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author gavalian
 */
public class DriftChamberDenoise {
    
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
    
    public static DataSet readDataSetNoisify(String filename, int length, int noise){
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
        
        Random rand = new Random();
        
        for(int i = 0; i < length; i++){
            for(int r = 0; r < noise; r++){
                int row = rand.nextInt(112*36);
                input.putScalar(new int[]{i,row}, 1.0);
            }
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

        
        int nEpochs = 150;
        int nEntries = 30000;
        
        if(args.length>0){
            nEpochs = Integer.parseInt(args[0]);
        }
        
        if(args.length>1){
            nEntries = Integer.parseInt(args[1]);
        }
        
        //DataSet trainData = DriftChamberDenoise.readDataSet("driftchamber_tracks.lsvm", nEntries);
        //DataSet  testData = DriftChamberDenoise.readDataSet("driftchamber_tracks_test.lsvm", 2500);
        
        DataSet trainData = DriftChamberDenoise.readDataSetNoisify("driftchamber_tracks.lsvm", nEntries,0);
        DataSet  testData = DriftChamberDenoise.readDataSetNoisify("driftchamber_tracks_test.lsvm", 2500,0);
        
        System.out.println(trainData);
        
        System.out.println("---- TRAINING DATA SIZE : " + trainData.getFeatures().size(0));
        System.out.println("---- TESTING  DATA SIZE : " + testData.getFeatures().size(0));
        
        AutoEncoder encoder = new AutoEncoder(112*36,new int[]{2*112,112,56});
        encoder.setMaxIterations(10);
        encoder.train(trainData, nEpochs);
        
        DriftChamberDenoise.evaluate(encoder.getNetwork(), trainData, "dc_train_test_%d.png", 0.5);
        DriftChamberDenoise.evaluate(encoder.getNetwork(), testData, "dc_test_test_%d.png",0.5);
        
        DriftChamberDenoise.evaluate(encoder.getNetwork(), testData, "dc_test_test_0p15_%d.png",0.15);
        DriftChamberDenoise.evaluate(encoder.getNetwork(), testData, "dc_test_test_0p35_%d.png",0.35);
    }
}
