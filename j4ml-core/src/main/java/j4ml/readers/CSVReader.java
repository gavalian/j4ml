/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.readers;

import java.util.ArrayList;
import java.util.List;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author gavalian
 */
public class CSVReader  {
    
    private String   csvDelim = ",";
    private int      dataLabelPosition   = 0;
    private int      dataLabelCount      = 2;
    private int      inputSize           = 0;
    private int      inputStartPosition  = 0;
    private int      outputSize          = 0;
    private int      outputStartPosition = 0;
    private boolean  isLabeledData       = true;
    private String    dataFilename       = "";
    
    public CSVReader(int inputs, int outputs){
        this.setInputOutput(inputs, outputs);
    }
    
    public CSVReader(String filename,int inputs, int outputs){
        dataFilename = filename;
        this.setInputOutput(inputs, outputs);
    }
    
    public CSVReader(String filename){
        dataFilename = filename;
    }
    
    public final CSVReader setInputOutput(int inputs, int outputs){
        inputSize  = inputs;
        outputSize = outputs;
        inputStartPosition  = 0;
        outputStartPosition = inputs;
        isLabeledData = false;
        return this;
    }
    
    public final CSVReader setInputs(int inputs){ 
        inputSize = inputs; //inputStartPosition = 0;
        return this;
    }
    public final CSVReader setInputsPos(int inputspos){ 
        this.inputStartPosition = inputspos;
        return this;
    }
    
    public final CSVReader setOutputs(int outputs){ 
        outputSize = outputs; outputStartPosition = inputStartPosition + inputSize; return this;
    }
    
    public final CSVReader setClasses(int nclasses){ dataLabelCount = nclasses; isLabeledData = true; return this;}
    public final CSVReader setClassesPos(int nclassespos){ dataLabelPosition = nclassespos; return this;}
    
    
    public final DataSet  load(String filename, int maxInputs){

        int delimCount = inputStartPosition + inputSize + outputSize;        
        List<String> dataStrings = readAsList(filename,maxInputs,delimCount);

     
        System.out.println("[CSVDataLoader] file : " + filename + " , number of entries loaded : " + dataStrings.size());
        //return null;
        
        int ncount = dataStrings.size();
        
        INDArray input  = Nd4j.zeros( new int[] { ncount, inputSize  } );
        INDArray output =  Nd4j.zeros( new int[] { ncount, outputSize });
        
        /*if(this.isLabeledData==true){
            Nd4j.zeros( new int[] { ncount, dataLabelCount } );            
        } else {
            Nd4j.zeros( new int[] { ncount, outputSize } );
        }*/
        
        int[] index = new int[]{0,0};
        for(int i = 0; i < ncount; i++){
            String[] tokens = dataStrings.get(i).split("\\s+");
            for(int k = 0; k < inputSize; k++){
                double value = Double.parseDouble(tokens[inputStartPosition+k]);
                index[0] = i; index[1] = k;
                input.putScalar(index, value);
            }           
            for(int j =0; j < outputSize; j++){
                double value = Double.parseDouble(tokens[outputStartPosition+j]);
                index[0] = i; index[1] = j;
                output.putScalar(index, value);
            }
        }       
        return new DataSet(input,output);
    }
    
    public final DataSet  loadClasses(String filename, int maxInputs){
           int delimCount = inputStartPosition + inputSize + outputSize;        
        List<String> dataStrings = readAsList(filename,maxInputs,delimCount);

     
        System.out.println("[CSVDataLoader] file : " + filename + " , number of entries loaded : " + dataStrings.size());
        System.out.println("[CSVDataLoader] number of classes : " + this.dataLabelCount);
        //return null;
        
        int ncount = dataStrings.size();
        
        INDArray input  = Nd4j.zeros( new int[] { ncount, inputSize  } );
        INDArray output =  Nd4j.zeros( new int[] { ncount, this.dataLabelCount });
        
        /*if(this.isLabeledData==true){
            Nd4j.zeros( new int[] { ncount, dataLabelCount } );            
        } else {
            Nd4j.zeros( new int[] { ncount, outputSize } );
        }*/
        
        int[] index = new int[]{0,0};
        for(int i = 0; i < ncount; i++){
            String[] tokens = dataStrings.get(i).split("\\s+");
            for(int k = 0; k < inputSize; k++){
                double value = Double.parseDouble(tokens[inputStartPosition+k]);
                index[0] = i; index[1] = k;
                input.putScalar(index, value);
            }
            //int dataClass = Integer.parseInt(tokens[this.dataLabelPosition]);
            int dataClass = (int) Double.parseDouble(tokens[this.dataLabelPosition]);
            
            for(int j =0; j < dataLabelCount; j++){
                //double value = Double.parseDouble(tokens[outputStartPosition+j]);
                index[0] = i; index[1] = j;
                if(j!=dataClass){
                    output.putScalar(index, 0.0);
                } else {
                    output.putScalar(index, 1.0);
                }
            }
        }
        return new DataSet(input,output);
    }
    
    private List<String> readAsList(String filename, int maxEntries, int minElements){
        List<String> dataStrings = new ArrayList<String>();
        TextFileReader reader = new TextFileReader();
        reader.setSeparator(csvDelim);        
        reader.open(filename);
        int counter = 0;
        while(reader.readNext()==true){
            if(reader.entrySize()>=minElements){
                String line = reader.getString();
                dataStrings.add(line);
                counter++;
            }
            if(maxEntries>0&&counter>=maxEntries) break;
        }
        return dataStrings;
    }
    
    /*public DataSet load(){
        
    }*/
    
    public static void main(String[] args){
        
        CSVReader loader = new CSVReader(6,1);
        
        DataSet ds = loader.load("model_data.csv", -1);
        System.out.println(ds);        
    }

   
}
