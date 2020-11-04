/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.denoise;

import j4ml.networks.AutoEncoder;
import j4ml.readers.TextFileWriter;
import j4ml.utils.DataSetUtils;
import j4ml.visualization.DataImageMaker;
import java.util.List;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author gavalian
 */
public class LayerDenoiseEncoder {
    
    public static DataSet createData(int length){
        
        INDArray  input = Nd4j.zeros(new int[]{ length, 24*6 } );
        INDArray output = Nd4j.zeros(new int[]{ length, 24*6 } );
        
        List<LayerData8> outData = LayerData8.generateSet(length);
        List<LayerData8> inData  = LayerData8.nosify(outData, 10);

        for(int i = 0; i < length; i++){
            DataSetUtils.putRow( input, i, inData.get(i).flat());
            DataSetUtils.putRow(output, i, outData.get(i).flat());
            
            /*double[] flat = outData.get(i).flat();
            double[] flatin = inData.get(i).flat();
            String dataString = DataSetUtils.doubleArrayString(flat,"%.1f ",112);
            String dataStringIN = DataSetUtils.doubleArrayString(flatin,"%.1f ",112);
            System.out.println(">>>\n" + dataString);
            System.out.println(">>> INPUT\n" + dataStringIN);*/
        }
        return new DataSet(input,output);
    }
    
    public static void evaluate(MultiLayerNetwork network, DataSet test, double threshold){
        INDArray   input = test.getFeatures();
        INDArray desired = test.getLabels();
        long        size = input.size(0);
        
        long start_time = System.currentTimeMillis();
        INDArray output = network.output(input);
        long end_time   = System.currentTimeMillis();
        
        System.err.println("\n\n>>>>> Network Performance:");
        System.out.printf(">>>>> evaluate time = %d ms , samples = %d\n",(end_time-start_time),size);
        
        TextFileWriter writer = new TextFileWriter();
        writer.open("network_performance.txt");
        
        for(int i = 0; i < size; i++){
            
            int[] result = DataSetUtils.compare(desired, output, i, threshold);
            String resultString = DataSetUtils.intArrayString(result, "%8d");
            System.err.println(resultString);
            double[] inImage = DataSetUtils.getRow(input, i);
            double[] desImage = DataSetUtils.getRow(desired, i);
            double[] outImage = DataSetUtils.getRow(output, i);
            
            DataSetUtils.threshold(outImage, 0.5);
            
            writer.writeString(resultString);
            if(i<50){
                DataImageMaker.saveImage(inImage, 24, 6, String.format("image_input_%d.png", i),4,8);
                DataImageMaker.saveImage(desImage, 24, 6, String.format("image_desired_%d.png", i),4,8);
                DataImageMaker.saveImage(outImage, 24, 6, String.format("image_output_%d.png", i),4,8);
            }
            
            String outStringDes = DataSetUtils.doubleArrayString(desImage, "%6.2f ", 24);
            System.out.println(">> DESIRED \n"+outStringDes);
            String outString = DataSetUtils.doubleArrayString(outImage, "%6.2f ", 24);
            System.out.println(">> OUTPUT \n"+outString);
            
        }        
        
        writer.close();
    }
    
    public static void main(String[] args){
        
        AutoEncoder encoder = new AutoEncoder(24*6,new int[]{96, 48,24});
        DataSet    trainData = LayerDenoiseEncoder.createData(10000);
        DataSet     testData = LayerDenoiseEncoder.createData(250);

        System.out.println(trainData);

        encoder.setMaxIterations(100);
        encoder.train(trainData, 50);
        
        LayerDenoiseEncoder.evaluate(encoder.getNetwork(), testData, 0.5);
    }
}
