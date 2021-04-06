/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.network2;

import java.util.List;
import org.jlab.jnp.utils.options.OptionParser;
import org.jlab.jnp.utils.options.OptionStore;

/**
 *
 * @author gavalian
 */
public class Clas12NetworkTrainer {
    public static void main(String[] args){
        OptionStore options = new OptionStore();
        options.addCommand("-extract", "extract training data set from a cooked file");
        
        options.addCommand("-train", "train neural network from given input data set");
        options.getOptionParser("-train").addRequired("-type", "network type (1-classifier, 2-fixer, 3-parameters)");
        
       options.parse(args);
       
       if(options.getCommand().compareTo("-train")==0){
           int  type = options.getOptionParser("-train").getOption("-type").intValue();
           List<String>     inputFiles = options.getOptionParser("-train").getInputList();
           if(type==2){
               TrackEncoderTrainer trainer = new TrackEncoderTrainer();
               trainer.runTraining(inputFiles.get(0));               
           }
           
       }
       
    }
}
