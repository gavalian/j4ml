package j4ml.trigger.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.coda.hipo.DataUtils;
import org.jlab.coda.jevio.BaseStructure;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioReader;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gavalian
 */
public class DataProvider {
    
    EvioReader  reader = null;
    EvioEvent    event = null;
    
    public DataProvider(){
        
    }
    
    public void open(String url){
        try {
            reader = new EvioReader(url, false, true);
        } catch (EvioException ex) {
            Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean next(){
        try {
            event = reader.parseNextEvent();
            if(event==null) return false;
        } catch (IOException ex) {
            Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (EvioException ex) {
            Logger.getLogger(DataProvider.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    
    public void show(){
        List<BaseStructure> list = event.getChildrenList();
        for(BaseStructure item : list){
            int tag = item.getHeader().getTag();
            int num = item.getHeader().getNumber();
            
            System.out.printf("tag = %5d, num = %5d, length = %d\n",
                    item.getHeader().getTag(), item.getHeader().getNumber(),
                    item.getHeader().getLength()
                    );
            if(tag==201&&num==0){
                byte[] data = item.getRawBytes();
                ByteBuffer buffer = ByteBuffer.wrap(data);
                buffer.order(item.getByteOrder());
                int superlayer = buffer.get(8*4);
                int count      = buffer.get(8*4+1);
                int position   = 8*4+2;
                for(int i = 0; i < count; i++){
                    short origin = buffer.getShort(position);
                    position += 2;
                    short end = buffer.getShort(position);
                    position += 2;
                    System.out.printf("--> %6d, %6d\n",origin,end);
                }
                System.out.printf("super-layer %3d, count = %d\n",superlayer,count);
                String dataString = DataUtils.getStringArray(buffer, 10, 100);
                System.out.println(dataString);
            }
        }
    }
    
    public static void main(String[] args){
        DataProvider provider = new DataProvider();
        //provider.open("/Users/gavalian/Work/DataSpace/evio/mc_output.evio");
        provider.open("/Users/gavalian/Work/DataSpace/evio/segm1.evio");
        int counter = 0;
        
        while(provider.next()==true){
            counter++;
            System.out.println("event # " + counter);
            provider.show();
        }
        System.out.printf("events = %d\n",counter);
    }
}
