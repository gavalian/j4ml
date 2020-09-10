/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.readers;

import java.util.List;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author gavalian
 */
public class LibSVMReader {
    
    protected int numberOfClasses  = 2;
    protected int numberOfOutputs  = 3;
    protected int numberOfFeatures = 6;
    protected boolean isRegression = true;
    private   String  inputFileName = "";
    protected   int[]     useOutputColumns = null;
    //private   double[]  outputNormalizeShift 
    
    public LibSVMReader(){}
    
    public static LibSVMReader regression(int nout, int nfeatures){
        LibSVMReader loader = new LibSVMReader();
        loader.isRegression = true;
        loader.numberOfOutputs = nout;
        loader.useOutputColumns = new int[nout];
        for(int i = 0; i < nout; i++) loader.useOutputColumns[i] = i;
        return loader;
    }
    
    public void setColumns(int[] usecol){
        this.useOutputColumns = usecol;
    }

    public static LibSVMReader classification(int nclasses, int nfeatures){
        LibSVMReader loader = new LibSVMReader();
        loader.isRegression = false;
        loader.numberOfClasses = nclasses;
        loader.numberOfFeatures = nfeatures;
        return loader;
    }
    
    public DataSet readClassification(List<String> data){
        int nEntries = data.size();
        INDArray  input = Nd4j.zeros(new int[]{ nEntries, numberOfFeatures } );
        INDArray labels = Nd4j.zeros(new int[]{ nEntries, numberOfClasses } );
        
        int[] indexIn  = new int[2];
        int[] indexOut = new int[2];
        
        for(int row = 0; row < nEntries; row++){
            String[] tokens = data.get(row).split("\\s+");
            
            int labelClass = Integer.parseInt(tokens[0]);
            indexOut[0] = row; indexOut[1] = labelClass;            
            labels.putScalar(indexOut, 1.0);
            
            indexIn[0] = row;
            for(int item = 1; item < tokens.length; item++){
                String[] pair = tokens[item].split(":");
                if(pair.length==2){
                    int    index = Integer.parseInt(  pair[0]);
                    double value = Double.parseDouble(pair[1]);
                    indexIn[1] = (index-1);
                    if(indexIn[1]>=this.numberOfFeatures||indexIn[1]<0){
                        System.out.println("[libsvm::error] index " + indexIn[1] + " is out of range of [0,"
                        + this.numberOfFeatures +"]");
                    } else {
                        input.putScalar(indexIn, value);
                    }
                }
            }
        }
        return new DataSet(input,labels);
    }
    
    public DataSet readClassification(String filename){
        
        int    nEntries = getFileLineCount(filename);
        INDArray  input = Nd4j.zeros(new int[]{ nEntries, numberOfFeatures } );
        INDArray labels = Nd4j.zeros(new int[]{ nEntries, numberOfClasses } );
        
        int[] indexIn  = new int[2];
        int[] indexOut = new int[2];
        
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        int entry = 0;
        while(reader.readNext()==true){
            String line = reader.getString();
            String[] tokens = line.split("\\s+");
            
            indexOut[0] = entry;
            int lableClass = Integer.parseInt(tokens[0]);            
            indexOut[1] = lableClass;
            labels.putScalar(indexOut, 1.0);
                        
            indexIn[0] = entry;            
            for(int i = 1; i < tokens.length; i++){
                String[] pair = tokens[i].split(":");
                if(pair.length==2){
                    int index = Integer.parseInt(pair[0]);
                    double value = Double.parseDouble(pair[1]);
                    indexIn[1] = (index-1);
                    input.putScalar(indexIn, value);
                }
            }            
            entry++;
        }
            
        return new DataSet(input,labels);
    }
    
    public DataSet readRegression(String filename){
        
        int    nEntries = getFileLineCount(filename);
        int    nOutput  = useOutputColumns.length;
        INDArray  input = Nd4j.zeros(new int[]{ nEntries, numberOfFeatures } );
        //INDArray labels = Nd4j.zeros(new int[]{ nEntries, numberOfOutputs } );
        INDArray labels = Nd4j.zeros(new int[]{ nEntries, nOutput } );
        
        int[]  indexIn = new int[2];
        int[] indexOut = new int[2];
        
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        int entry = 0;
        while(reader.readNext()==true){
            String line = reader.getString();
            String[] tokens = line.split("\\s+");
            
            indexOut[0] = entry;
            /*for(int i = 0; i < numberOfOutputs; i++){
                double value = Double.parseDouble(tokens[i]);
                indexOut[1] = i;
                labels.putScalar(indexOut, value);
            }*/
            for(int i = 0; i < nOutput; i++){
                double value = Double.parseDouble(tokens[this.useOutputColumns[i]]);
                indexOut[1] = i;
                labels.putScalar(indexOut, value);
            }
            indexIn[0] = entry;
            
            for(int i = numberOfOutputs; i < tokens.length; i++){
                String[] pair = tokens[i].split(":");
                if(pair.length==2){
                    int index = Integer.parseInt(pair[0]);
                    double value = Double.parseDouble(pair[1]);
                    indexIn[1] = (index-1);
                    input.putScalar(indexIn, value);
                }
            }            
            entry++;
        }
            
        return new DataSet(input,labels);
    }
        
    private int getFileLineCount(String filename){
        TextFileReader reader = new TextFileReader();
        reader.open(filename);
        int lineCount = 0;
        while(reader.readNext()==true){
            lineCount++;
        }
        System.out.printf("[LibSVM] >>> file : %s , lines = %d\n",filename,lineCount);        
        return lineCount;
    }
    
    public static void main(String[] args){
        String filename = "/Users/gavalian/Work/Software/project-8a.0.0/WorkBench/study/dc_train_regression.txt";
        LibSVMReader loader = LibSVMReader.regression(3, 6);
        DataSet ds = loader.readRegression(filename);
        System.out.println(ds);
        filename = "/Users/gavalian/Work/Software/project-8a.0.0/WorkBench/study/dc_tracks_train_positive.lsvm";
        
        LibSVMReader loader2 = LibSVMReader.classification(2, 6);
        DataSet dsc = loader2.readClassification(filename);
        System.out.println(dsc);
        
    }
}
