/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.clas12;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;

/**
 *
 * @author gavalian
 */
public class DataExtractor {
    
    protected String outputFile = "default.txt";
    protected List<String> outputLines = new ArrayList<String>();
    private BufferedWriter writer = null;
    
    public void init(HipoChain reader){
        
    }
    
    protected void output(String filename){
        this.outputFile = filename;
    }
    
    protected void write(){     
        for(String item : outputLines){
            try {
                writer.write(item);
                writer.write("\n");
            } catch (IOException ex) {
                Logger.getLogger(DataExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public String mapToValues(Map<Integer,Double> map){
        StringBuilder str = new StringBuilder();
        List<Integer> iKeys = new ArrayList<Integer>(map.keySet());
        Collections.sort(iKeys);
        for(int i = 0; i < iKeys.size(); i++){
//        for(Map.Entry<Integer,Double> entry : map.entrySet()){
            str.append(String.format("%d:%.4f ", iKeys.get(i),map.get(iKeys.get(i))));
        }
        return str.toString();
    }
    
    public void process(Event event){
        
    }
    
    protected void open(){
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFile), "utf-8"));
            //writer.write("Something");
        } catch (IOException ex) {
            // report
        }
    }
    
    public void close(){
        if(writer != null){
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(DataExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
