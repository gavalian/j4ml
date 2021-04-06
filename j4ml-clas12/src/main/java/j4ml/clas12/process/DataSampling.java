/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.process;

import j4np.physics.Vector3;
import org.jlab.jnp.hipo4.data.DataType;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Node;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.jlab.jnp.hipo4.io.HipoWriter;

/**
 *
 * @author gavalian
 */
public class DataSampling {
    
    double[] bins = null;
    int[] counter = null;
    
    public DataSampling(){
        
        double step = 12.0/120.0;
        bins = new double[120]; 
        counter = new int[120];
        for(int i = 0; i < 120; i++){
            bins[i] = i*step;
        }
    }
    
    public void fill(double value){
        int bin = getBin(value);
        if(bin>=0){
            counter[bin] = counter[bin]+1;
        }
    }
    
    public int getBin(double value){
        for(int i = 0; i < bins.length-1; i++){
            if(value>bins[i]&&value<bins[i+1]) return i;
        }
        return -1;
    }
    
    public int getBinContent(int bin){
        return this.counter[bin];
    }
    
    public void show(){
        for(int i = 0; i < bins.length; i++){
            System.out.printf("%5d : %12.5f %8d\n",i,bins[i],counter[i]);
        }
    }
    
    public static void main(String[] args){
        HipoReader reader = new HipoReader();
        reader.open("/Users/gavalian/Work/DataSpace/raw/extract_output.hipo");
        DataSampling sampling = new DataSampling();
        
        HipoWriter writer = new HipoWriter();
        writer.open("extract_output_balanced.hipo");
        
        Event event = new Event();
        Node  vectorNode = new Node(1001,5,DataType.FLOAT,20);
        int counter = 0;
        while(reader.hasNext()){//&& counter<10000000){
            counter++;
            reader.nextEvent(event);
            int position  = event.scan(1001, 6);
            vectorNode    = event.read(vectorNode, position);
            System.out.println("data >> " + vectorNode.getDataString());
            float[]  data = vectorNode.getFloat();
            Vector3  vector = new Vector3(data[0],data[1],data[2]);
            sampling.fill(vector.mag());
            
            int bin = sampling.getBin(vector.mag());
            if(bin>=0){
                if(sampling.getBinContent(bin)<50000){
                    writer.addEvent(event);
                }
            }
            //System.out.println(" momentum = " + vector.mag());
        }
        
        sampling.show();
        writer.close();
    }
}
