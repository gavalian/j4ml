/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.network2;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;

/**
 *
 * @author gavalian
 */
public class NetworkTrackClassifier {
    
    private NeuralNetwork            network = null;
    
    public NetworkTrackClassifier(){
        
    }
    
    public final void load(String filename){
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
    
     public void save(String filename){
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String name =  filename + "-"+ dateFormat.format(date) + ".nnet";
        network.save(name);                
        //network.
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
               // NetworkTrackEncoder.FixerEvaluator eval = new NetworkTrackEncoder.FixerEvaluator();
                //eval.evaluate(network, test);
                System.out.printf( "[train] >> %8d / %8d  , error = %18.14f\n",//, test mean = %12.8f , rms = %12.8f\n",
                        i, nEpochs, error);//, eval.evaluationMean,eval.evaluationRMS);
            }
        }
    }
    
    public final double[] evaluate(double[] features){
        network.setInput(features);
        network.calculate();
        return network.getOutput();
    }
    
    public final int getOutputClass(double[] output){
        int   index = 0;
        double  max = 0.0;
        for(int i = 0; i < output.length; i++){
            if(output[i]>max){ max = output[i]; index = i;}
        }
        return index;
    }
}
