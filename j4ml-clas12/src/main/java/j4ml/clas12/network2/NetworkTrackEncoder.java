/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.network2;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.jlab.jnp.readers.TextFileWriter;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;

/**
 *
 * @author gavalian
 */
public class NetworkTrackEncoder {
    
    NeuralNetwork network = null;
    
    public NetworkTrackEncoder(){
        
    }
    
    public void load(String filename){
        network = NeuralNetwork.createFromFile(filename);
    }
    
    public void init(int[] layers){
        network = new MultiLayerPerceptron(layers);        
        //((LMS) network.getLearningRule())
        ((LMS) network.getLearningRule()).setMaxIterations(10);
        ((LMS) network.getLearningRule()).setMaxError(0.00000001);
        
        
        System.out.println(network);
        
        System.out.println("network created");
    }
    
    public void init(){
        init(new int[]{6,12,6,12,6});
    }
    
    public void save(String filename){
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String name =  filename + "-"+ dateFormat.format(date) + ".nnet";
        network.save(name);                
    }
    
    public void train(DataSet train, DataSet test, int nEpochs){
        
        int nrowsTrain = train.getRows().size();
        int nrowsTest  = test.getRows().size();
        
        System.out.println("[network training] >>> data set size, train = " + nrowsTrain + " , test = " + nrowsTest);
        System.out.println("[network training] >>> n epochs = " + nEpochs);
        
        for(int i = 0; i < nEpochs; i++){
            network.learn(train);
            if(i%2==0){
                int    epoch = ((LMS) network.getLearningRule()).getCurrentIteration();
                double error = ( (LMS) network.getLearningRule()).getTotalNetworkError();
                FixerEvaluator eval = new FixerEvaluator();
                eval.evaluate(network, test);
                System.out.printf( "[train] >> %8d / %8d  , error = %18.14f , test mean = %12.8f , rms = %12.8f\n",
                        i, nEpochs, error, eval.evaluationMean,eval.evaluationRMS);
            }
        }
    }
    
    public double[] evaluate(double[] features){
        network.setInput(features);
        network.calculate();
        return network.getOutput();
    }
    
    public void evaluate(DataSet test, String filename){
        FixerEvaluator eval = new FixerEvaluator();
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String name =  filename + "-"+ dateFormat.format(date) + ".txt";
        
        eval.evaluate(network, test, name);
        
    }
    
    public class FixerEvaluator {
        
        double evaluationMean = 0.0;
        double  evaluationRMS = 0.0;
        
        public FixerEvaluator(){
            
        }
        
        public int getCorruptedRow(double[] data){
            for(int i = 0; i < data.length; i++){
                if(data[i]<0.000000000001) return i;
            }
            return -1;
        }
        
        
        public void evaluate(NeuralNetwork net, DataSet set, String outFile){
                    
            TextFileWriter writer = new TextFileWriter();
            writer.open(outFile);
            int nrows = set.getRows().size();
            for(int i = 0; i < nrows; i++){
                double[] input = set.getRows().get(i).getInput();
                double[] desired = set.getRows().get(i).getDesiredOutput();
                
                network.setInput(set.getRows().get(i).getInput());                
                network.calculate();
                double[]  output = network.getOutput();
                
                int row = getCorruptedRow(input);
                writer.writeString(String.format("%d %12.6f %12.6f", row, desired[row],output[row]));
            }
            writer.close();
        }
        
        public void evaluate(NeuralNetwork net, DataSet set){
            double summ = 0.0;
            int counter = 0;
            int nrows = set.getRows().size();
            for(int i = 0; i < nrows; i++){
                double[] input = set.getRows().get(i).getInput();
                double[] desired = set.getRows().get(i).getDesiredOutput();
                
                network.setInput(set.getRows().get(i).getInput());                
                network.calculate();
                double[]  output = network.getOutput();
                
                int row = getCorruptedRow(input);
                if(row>=0){
                    summ += desired[row] - output[row];
                    counter++;
                } else {
                    System.out.println("[*** error ***] >> the row does not have corrupted element");
                }
            }
            evaluationMean = summ/counter;
            summ = 0.0; counter = 0;
            for(int i = 0; i < nrows; i++){
                double[] input = set.getRows().get(i).getInput();
                double[] desired = set.getRows().get(i).getDesiredOutput();
                
                network.setInput(set.getRows().get(i).getInput());                
                network.calculate();
                double[]  output = network.getOutput();
                
                int row = getCorruptedRow(input);
                if(row>=0){
                    double value = desired[row] - output[row];
                    summ += (value-evaluationMean)*(value-evaluationMean);
                    counter++;
                } else {
                    System.out.println("[*** error ***] >> the row does not have corrupted element");
                }
            }
            evaluationRMS = Math.sqrt(summ/counter);
                        
        }
    }
}
