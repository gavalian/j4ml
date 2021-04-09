package j4ml.clas12.ejml;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Andru Quiroga (aqui-CNU)
 * Tweaks and Adjustments by Gagik Gavalian (Jlab)
 */

public class EJMLModelEvaluator {

    private SimpleMatrix[] LAYERS = null;
    private SimpleMatrix[] BIASES = null;
    private int inputSize;


    public EJMLModelEvaluator(String filePath) {
        try { buildMatrices(filePath);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private EJMLModelEvaluator() {
        
    }
    
    public static EJMLModelEvaluator create(List<String> lines){
        EJMLModelEvaluator instance = new EJMLModelEvaluator();
        instance.buildMatriciesFromList(lines);
        return instance;
    }
    
    private static SimpleMatrix elementwiseApplyReLU(SimpleMatrix input) {  // Credits to stanfordnlp
        SimpleMatrix output = new SimpleMatrix(input);
        for (int i = 0; i < output.numRows(); ++i)
            for (int j = 0; j < output.numCols(); ++j)
                output.set(i, j, Math.max(0, output.get(i, j)));

        return output;
    }

    private static SimpleMatrix elementwiseApplySigmoid(SimpleMatrix input) {  // Credits to stanfordnlp
        SimpleMatrix output = new SimpleMatrix(input);
        for (int i = 0; i < output.numRows(); ++i)
            for (int j = 0; j < output.numCols(); ++j)
                output.set(i, j, 1.0 / (1.0 + Math.exp(-output.get(i, j))));

        return output;
    }

    private static SimpleMatrix ApplySoftmax(SimpleMatrix input) {   // Credits to stanfordnlp
        SimpleMatrix output = new SimpleMatrix(input);
        for (int i = 0; i < output.numRows(); ++i)
            for (int j = 0; j < output.numCols(); ++j)
                output.set(i, j, Math.exp(output.get(i, j)));

        return output.scale(1.0 / output.elementSum());
    }

    private void buildMatriciesFromList(List<String> lines) {
        List<SimpleMatrix> allLayers = new ArrayList<>();
        List<SimpleMatrix> allBiases = new ArrayList<>();
        int counter = lines.size();
        int index   = 0;
        int layer = 0;
        //System.out.println("---> loading network list size = " + lines.size());
        while(index<counter){
            String line = lines.get(index);
            String[] size = line.split(",");
            int size_in  = Integer.parseInt(size[0]);
            int size_out = Integer.parseInt(size[1]);
            //System.out.println(" layer " + layer + " size  in = " + size_in + " , out = " + size_out);
            index++;
            allLayers.add(EJMLModelEvaluator.makeWeightMatrixFromList(lines, index, size_in, size_out));
            index += size_in;
            allBiases.add(EJMLModelEvaluator.makeBiasesFromList(lines,index, size_out));
            index++;
            
            layer++;
        }
        
        BIASES = new SimpleMatrix[allBiases.size()];
        BIASES = allBiases.toArray(BIASES);
        
        LAYERS = new SimpleMatrix[allLayers.size()];
        LAYERS = allLayers.toArray(LAYERS);
        
        inputSize = LAYERS[0].numRows();
    }
    
    public String summary(){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < this.LAYERS.length; i++){
            if(i!=0) str.append(",");
            str.append(LAYERS[i].numRows());
        }
        return str.toString();
    }
    
    private void buildMatrices(String path) throws IOException {
        
        List<SimpleMatrix> allLayers = new ArrayList<>();
        List<SimpleMatrix> allBiases = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            int counter = 0;
            while (line != null) {
                String[] ssize = line.split(",");
                int size_in = Integer.parseInt(ssize[0]);
                int size_out = Integer.parseInt(ssize[1]);
                //System.out.println(" processing layer " + counter + " [ "
                //        + size_in + " , " + size_out + "]"
                //);
                counter++;
                allLayers.add(EJMLModelEvaluator.makeWeightMatrix(br, size_in, size_out));
                allBiases.add(EJMLModelEvaluator.makeBiases(br, size_out));
                line = br.readLine();
            }

            BIASES = new SimpleMatrix[allBiases.size()];
            BIASES = allBiases.toArray(BIASES);

            LAYERS = new SimpleMatrix[allLayers.size()];
            LAYERS = allLayers.toArray(LAYERS);

            inputSize = LAYERS[0].numRows();
        }
    }

    private static SimpleMatrix makeBiases(BufferedReader br, int size) throws IOException {
        String[] sBiases = br.readLine().split(",");
        float[] biases = new float[size];
        for (int i = 0; i < size; i++)
            biases[i] = Float.parseFloat(sBiases[i]);

        return new SimpleMatrix(new float[][]{biases});
    }

    private static SimpleMatrix makeWeightMatrix(BufferedReader br, int numITensors, int numOTensors) throws IOException {
        float[][] Layer = new float[numITensors][numOTensors];
        for (int i = 0; i < numITensors; i++){
            String[] ss = br.readLine().split(",");
            //System.out.println(" size = " + ss.length);
            for (int j = 0; j < numOTensors; j++)
                Layer[i][j] = Float.parseFloat(ss[j]);
        }

        return new SimpleMatrix(Layer);
    }

    private static SimpleMatrix makeBiasesFromList(List<String> lines, int start, int size) {
        String[] sBiases = lines.get(start).split(",");
        float[] biases = new float[size];
        for (int i = 0; i < size; i++)
            biases[i] = Float.parseFloat(sBiases[i]);
        return new SimpleMatrix(new float[][]{biases});
    }
    
    private static SimpleMatrix makeWeightMatrixFromList(List<String> lines, int start, int numITensors, int numOTensors) {
        float[][] Layer = new float[numITensors][numOTensors];
        for (int i = 0; i < numITensors; i++){
            String[] ss = lines.get(i+start).split(",");
            //System.out.println(" size = " + ss.length);
            for (int j = 0; j < numOTensors; j++)
                Layer[i][j] = Float.parseFloat(ss[j]);
        }
        return new SimpleMatrix(Layer);
    }
    
    
    public int getInputSize(){
        return inputSize;
    }

    public void feedForward(float[] input, float[] results) {
        assert input.length == inputSize;

        SimpleMatrix matrix = new SimpleMatrix(new float[][] {input});
        for (int i = 0; i < LAYERS.length; i++) {
            if (i == LAYERS.length - 1)
                matrix = elementwiseApplySigmoid(matrix.mult(LAYERS[i]).plus(BIASES[i]));
            else
                matrix = elementwiseApplyReLU(matrix.mult(LAYERS[i]).plus(BIASES[i]));
        }

        for (int i = 0; i < matrix.numCols(); i++)
            results[i] = (float) matrix.get(0, i);
    }
    
    public void feedForwardSoftmax(float[] input, float[] results) {
        assert input.length == inputSize;

        SimpleMatrix matrix = new SimpleMatrix(new float[][] {input});
        for (int i = 0; i < LAYERS.length; i++) {
            if (i == LAYERS.length - 1)
                matrix = ApplySoftmax(matrix.mult(LAYERS[i]).plus(BIASES[i]));
            else
                matrix = elementwiseApplyReLU(matrix.mult(LAYERS[i]).plus(BIASES[i]));
        }

        for (int i = 0; i < matrix.numCols(); i++)
            results[i] = (float) matrix.get(0, i);
    }

    public static void main(String[] args) {
        //EJMLModelEvaluator model = new EJMLModelEvaluator("model.csv");
        EJMLModelEvaluator model = new EJMLModelEvaluator("etc/ejml/trackClassifierModel.csv");
        float[] in = new float[]{0.1161f, 0.0000f, 0.1403f, 0.1607f, 0.2604f, 0.2783f};
        float[] out = new float[3];
        model.feedForward(in, out);
        System.out.println(Arrays.toString(out));
    }
}
