/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.clas12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;

/**
 *
 * @author gavalian
 */
public class ChainDataExtractor {
    
    private HipoChain  chain = null;
    private List<DataExtractor>  extractors = new ArrayList<DataExtractor>();
    private int  limit = -1;
    
    public ChainDataExtractor(List<String> dataFiles){
        chain = new HipoChain();
        chain.addFiles(dataFiles);
        chain.open();
    }
    
    
    public void addExtractor(DataExtractor ext){
        extractors.add(ext);
    }
    
    public void setLimit(int l){
        limit = l;
    }
    
    public void process(){
        for(DataExtractor ext: extractors){
            ext.init(chain);
        }
        
        Event event = new Event();
        int counter = 0;
        while(chain.hasNext()==true){
            chain.nextEvent(event);
            for(DataExtractor ext : extractors){
                ext.process(event);
            }
            counter++;
            if((limit>0)&&counter>=limit) break;
        }

        for(DataExtractor ext: extractors){
            ext.close();
        }

    }
    
    public static void main(String[] args){
        String filename = "/Users/gavalian/Work/DataSpace/ai/clustersonly/cl_out_clas_005038.evio.00105-00109.hipo";
        ChainDataExtractor ce = new ChainDataExtractor(Arrays.asList(filename));
        DCRawDataExtractor ext = new DCRawDataExtractor("dc_raw_tdc.lsvm");
        ce.addExtractor(ext);
        ce.setLimit(1000);
        ce.process();
    }
    
}
