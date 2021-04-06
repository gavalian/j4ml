/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;

/**
 *
 * @author gavalian
 */
public class NeurophMLPClassifier {
    
    private NeuralNetwork            network = null;
    private int            printoutFrequency = 1;
    private String         networkTitle = "";
    
    public NeurophMLPClassifier(){
        
    }
    
    public void init(int[] layersSize){
        network = new MultiLayerPerceptron(layersSize);
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < layersSize.length; i++) str.append(String.format("%dx", layersSize[i]));
        networkTitle = str.toString();
    }
    
    public void configure(){
        ((LMS) network.getLearningRule()).setMaxIterations(10);
        ((LMS) network.getLearningRule()).setMaxError(0.00000001);
        MomentumBackpropagation learningRule = (MomentumBackpropagation) network.getLearningRule();
        learningRule.setLearningRate(0.1);
        learningRule.setMomentum(0.8);
    }
    
    protected double toSeconds(long then, long now){
        return ( (double) (now-then) )/1000.0;
    }
    
    public void evaluate(DataSet ds){
        
        int size = ds.getRows().size();
        
        long then = System.currentTimeMillis();
        //int[][] confusionMatrix = new int[2][2];
        
        Evaluator eval = new Evaluator(3);
        eval.setTitle(networkTitle);
        for(int i = 0; i < size; i++){
            network.setInput(ds.getRows().get(i).getInput());
            network.calculate();
            double[] output = ds.getRows().get(i).getDesiredOutput();
            double[] result = network.getOutput();
            eval.evaluate(output, result);
        }
        
        long now = System.currentTimeMillis();
        double time = toSeconds(then,now);
        System.out.printf("[AI] EVALUATE => samples = %8d , time = %14d ms, unit time = %14.8f ms\n",size,(now-then),( (double) (now-then))/size);
        System.out.println("confusion matrix");
        System.out.println(eval.getMatrixString());
        System.out.println(eval.getEfficiencyString());
    }
    public void save(String filename){
        network.save(filename);
    }
    public void train(DataSet ds, int nEpochs){
        for(int i = 0; i < nEpochs; i++){
            long then = System.currentTimeMillis();
            network.learn(ds);
            long now  = System.currentTimeMillis();
            
            if((i)%this.printoutFrequency==0){
                int    epoch = ((LMS) network.getLearningRule()).getCurrentIteration();
                double error = ( (LMS) network.getLearningRule()).getTotalNetworkError();
                double time = toSeconds(then,now);
                System.out.printf(">*< %8d => error = %18.12f , time = %12.2f seconds\n",i+1,error,time);
            }
        }
    }
    
    public Evaluator getEvaluator(DataSet ds){
        int size = ds.getRows().size();
         Evaluator eval = new Evaluator(3);
         for(int i = 0; i < size; i++){
            network.setInput(ds.getRows().get(i).getInput());
            network.calculate();
            double[] output = ds.getRows().get(i).getDesiredOutput();
            double[] result = network.getOutput();
            eval.evaluate(output, result);
        }
         
        return eval;
    }
    
    public void train(DataSet train, DataSet test, int nEpochs){
        for(int i = 0; i < nEpochs; i++){
            long then = System.currentTimeMillis();
            network.learn(train);
            long now  = System.currentTimeMillis();
            
            if((i)%this.printoutFrequency==0){
                int    epoch = ((LMS) network.getLearningRule()).getCurrentIteration();
                double error = ( (LMS) network.getLearningRule()).getTotalNetworkError();
                double time = toSeconds(then,now);
                Evaluator eval = getEvaluator(test);
                String title = String.format(">*< %8d/%8d => error = %18.12f , time = %12.2f sec, test error = %18.12f ",
                        i+1,nEpochs,error,time, eval.getError());
                StringBuilder str = new StringBuilder();
                double[] eff = eval.getEfficiency();
                for(int r = 0; r < eff.length; r++){
                    str.append(String.format("%6.4f ", eff[r]));
                }
                System.out.println(title + str.toString());
            }
        }
    }
    
    public static class Evaluator {
        private int          nOutput = 1;
        private int[][]       matrix = null;
        private int[]      desiredCount = null;
        private int[]      resultCount  = null;
        private double     threshold = 0.5;
        private String     title     = "evaluator";
        private double     error     = 0.0;
        private int        counter   = 0;
        
        public Evaluator(int nout){
            nOutput = nout;
            matrix = new int[nOutput][nOutput];
            desiredCount = new int[nOutput];
            resultCount  = new int[nOutput];
        }
        
        public double getError(){
            return error/counter;
        }
        
        public double[] getEfficiency(){
            double[] eff = new double[nOutput];
            for(int i = 0; i < eff.length; i++){
                eff[i] = 0.0;
                if(desiredCount[i]!=0) eff[i] = ( (double) (resultCount[i]) )/desiredCount[i];
            }
            return eff;
        }
        
        public void setTitle(String t){title = t;}
        
        public int getHighest(double[] array, double th){
            int    index = -1;
            double   max = 0.0;
            for(int i = 0; i < array.length; i++){
                if(array[i]>max&&array[i]>th){max = array[i]; index = i;}
            }
            return index;
        }
        
        public void evaluate(double[] desiredOutput, double[] result){
            counter++;
            for(int i = 0; i < result.length; i++){
                 error += (result[i]-desiredOutput[i])*(result[i]-desiredOutput[i]);
            }
            
            if(desiredOutput.length!=nOutput||result.length!=nOutput){
                System.out.println("ERROR : mismatch in output size...");
                return;
            }
            
            int     index = getHighest(desiredOutput,0.5);            
            
            if(index>=0){
                desiredCount[index]++;
                int rindex = getHighest(result,0.5);
                if(rindex==index) resultCount[index]++;
                if(rindex>=0){
                    matrix[rindex][index]++;
                } 
            }            
        }
        
        public String getEfficiencyString(){
            StringBuilder str = new StringBuilder();
            str.append(String.format("[EFFICIENCY : %s] ",title));
            double[] efficiency = new double[nOutput];
            for(int i = 0; i < nOutput; i++){
                efficiency[i] = 0.0;
                if(desiredCount[i]!=0) efficiency[i] = ( (double) (resultCount[i]) )/desiredCount[i];
            }            
            for(int i = 0; i < nOutput; i++){ str.append(String.format("%12d ",resultCount[i]));}
            for(int i = 0; i < nOutput; i++){ str.append(String.format("%12d ",desiredCount[i]));}
            for(int i = 0; i < nOutput; i++){ str.append(String.format("%6.4f ",efficiency[i]));}
            return str.toString();
        }
        
        public String getMatrixString(){
            StringBuilder str = new StringBuilder();
            for(int r = 0; r < nOutput; r++){
                for(int c = 0; c < nOutput; c++){
                    str.append(String.format("%9d ", matrix[r][c]));
                }
                str.append("\n");
            }
            return str.toString();
        }
    }
}
