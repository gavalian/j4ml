/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.networks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gavalian
 */
public class TextFileWriter {
    
    private BufferedWriter writer = null;
    private String         format = "%9.4f";
    
    public TextFileWriter(){
        
    }
    
    public void open(String filename){
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), "utf-8"));
            //writer.write("Something");
        } catch (IOException ex) {
            // report
        }
    }
    
    public void writeDouble(double[] array){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            str.append(String.format(format, array[i]));
        }
        writeString(str.toString());
    }
    
    public void writeFloat(float[] array){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < array.length; i++){
            str.append(String.format(format, array[i]));
        }
        writeString(str.toString());
    }
    
    public void writeString(String str){
        try {
            writer.write(str);
            writer.write("\n");
        } catch (IOException ex) {
            Logger.getLogger(TextFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void close(){
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(TextFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
