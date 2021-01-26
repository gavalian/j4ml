/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.extract;

import j4ml.data.clas12.ChainDataExtractor;
import j4ml.data.clas12.DataExtractor;
import j4ml.data.clas12.TrackExtraction;
import j4ml.data.clas12.TrackObject;
import java.util.Arrays;
import java.util.List;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;
import org.jlab.jnp.hipo4.operations.BankExpression;

/**
 *
 * @author gavalian
 */
public class TrackParameterExtractor extends DataExtractor {
    TrackObject    track = new TrackObject();
    
    public TrackParameterExtractor(String filename){
        output(filename);
    }
    
    @Override
    public void init(HipoChain chain){
        
        track.init(chain);
        
        this.openChannel(1, "track_parameters_full.csv");
        
        open();
    }
    
     @Override
    public void process(Event event){
        
        
        for(int s = 1; s <= 1; s++){                 
            track.read(event);
            List<Integer> tracksList = track.getTracksList(s);
            for(Integer trkIndex : tracksList){
                String params = track.getTrackDescriptionString(trkIndex);
                String features = track.getTrackClustersString(trkIndex);
                this.writeChannel(1, params +","+features);
            }
        }
    }
    
    public static void main(String[] args){
        String filename = "/Users/gavalian/Work/DataSpace/raw/out_ai_005038.00165-00169.hipo";
        ChainDataExtractor ce = new ChainDataExtractor(Arrays.asList(filename));
        TrackParameterExtractor ext = new TrackParameterExtractor("track_parameters.csv");
        ce.addExtractor(ext);
        //ce.setLimit(1000);
        ce.process();
    }
}
