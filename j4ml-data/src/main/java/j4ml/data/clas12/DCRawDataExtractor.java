/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.clas12;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;

/**
 *
 * @author gavalian
 */
public class DCRawDataExtractor extends DataExtractor {
    
    private Bank rawTDC = null;
    private Bank clustersBank = null;
    private Map<Integer,Double> tdcMap = new HashMap<>();
    //private String[] hitRows = new String[]{"Hit1_ID"};
    
    public DCRawDataExtractor(String filename){
        output(filename);
    }
    
    @Override
    public void init(HipoChain chain){
        rawTDC = new Bank(chain.getSchemaFactory().getSchema("DC::tdc"));
        clustersBank = new Bank( chain.getSchemaFactory().getSchema("HitBasedTrkg::HBClusters"));
        this.open();
    }
    
    private void rawMap(Bank bank){
        int nrows = bank.getRows();
        for(int row = 0; row < nrows; row++){
            int sector = bank.getInt("sector", row);
            if(sector==1){
                int layer = bank.getInt("layer", row);
                //if(layer>0&&layer<=6){
                    int component = bank.getInt("component", row);
                    int index = 1 + (layer-1)*112 + (component-1);
                    tdcMap.put(index, 1.0);
                //}
            }
        }
    }
    
    private void clustersMap(Bank bank, Bank tdc){
        int nrows = bank.getRows();
        for(int row = 0; row < nrows; row++){
            int sector = bank.getInt("sector", row);
            if(sector==1){
                for(int i = 0; i < 12; i++){
                    int index     = bank.getInt(i+4, row) - 1;
                    if(index>=0){
                        int layer     = tdc.getInt("layer",index);
                        //if(layer>0&&layer<=6){
                            int component = tdc.getInt("component", index);
                            int key = 1 + (layer-1)*112 + (component-1);
                            tdcMap.put(key, 1.0);
                        //}
                    }
                }
            }
        }
    }
    
    @Override
    public void process(Event event){        
        event.read(rawTDC);
        event.read(clustersBank);
                        
        tdcMap.clear();
        clustersMap(clustersBank,rawTDC);
        if(tdcMap.size()<15) return;
        String outputMapClusters = this.mapToValues(tdcMap);
        tdcMap.clear();
        rawMap(rawTDC);                
        String outputMapRaw = this.mapToValues(tdcMap);
        
        this.outputLines.clear();
        outputLines.add("1 " + outputMapClusters);
        outputLines.add("0 " + outputMapRaw);
        
        //System.out.println("1 -> " + outputMapClusters);
        //System.out.println("0 -> " + outputMapRaw);
        write();
    }
}
