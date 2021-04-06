/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.network2;

import org.jlab.jnp.hipo4.data.DataType;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Node;
import org.jlab.jnp.hipo4.io.HipoReader;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

/**
 *
 * @author gavalian
 */
public class TrackEncoderTrainer {
    
    private double chi2cut = 1.6;
    private double[] vertexcut = new double[]{-20.0,10.0};
    //private double[] vertexCut = new double[]{-20.0,10.0};
    
    public DataSet readDataSet(String filename, int max){
        DataSet ds = new DataSet(6,6);
        
        HipoReader reader = new HipoReader();
        reader.open(filename);
        
        Event event = new Event();
        
        Node   descNode = new Node(1001,1,DataType.SHORT,6);
        Node   chi2Node = new Node(1001,2,DataType.FLOAT,6);
        Node  meansNode = new Node(1001,4,DataType.FLOAT,6);
        Node vectorNode = new Node(1001,6,DataType.FLOAT,20);
        Node vertexNode = new Node(1001,7,DataType.FLOAT,20);
        
        int counter = 0;
        
        int position = 0;
        
        while(reader.hasNext()&&counter<max){
            counter++;
            reader.nextEvent(event);

            position  = event.scan(1001,1);
            descNode  = event.read(descNode, position);
            
            position  = event.scan(1001,2);
            chi2Node  = event.read(chi2Node, position);
            
            position  = event.scan(1001,4);
            meansNode = event.read(meansNode, position);
            
            position   = event.scan(1001,6);
            vectorNode = event.read(vectorNode, position);
            
            position   = event.scan(1001,7);
            vertexNode = event.read(vertexNode, position);
            
            
            //System.out.println("position = " + position + " data = " + meansNode.getFloat()[0] + " , " + meansNode.getFloat()[1]);
            //System.out.println(meansNode.getDataString());
            //System.out.println(vectorNode.getDataString());
            //System.out.println(vertexNode.getDataString());
            //System.out.println(descNode.getDataString());
            //System.out.println(chi2Node.getDataString());
            
            if(chi2Node.getFloat(0)<chi2cut&&
                    vertexNode.getFloat(2)>vertexcut[0]
                    &&vertexNode.getFloat(2)<vertexcut[1]){
                for(int k = 0; k < 6; k++){
                    double[] output = new double[6];
                    for(int i = 0; i < 6; i++) output[i] = meansNode.getFloat(i)/112.0;
                    double[] input = new double[6];
                    for(int i = 0; i < 6; i++) input[i] = meansNode.getFloat(i)/112.0;
                    input[k] = 0.0;
                    ds.add(new DataSetRow(input,output));
                }
            }
        }
        return ds;
    }
    
    public void runTraining(String filename){
        
        NetworkTrackEncoder encoder = new NetworkTrackEncoder();
        encoder.init(new int[]{6,12,12,6,12,12,6});
        
        DataSet ds = readDataSet(filename, 125000);        
        ds.shuffle();
        DataSet[] trset = ds.split(0.8,0.2);
        encoder.train(trset[0], trset[1], 60);        
        encoder.save("trackFixer");
        encoder.evaluate(trset[1], "trackFixerEvaluate");
    }
    
    public static void main(String[] args){
        
        TrackEncoderTrainer trainer = new TrackEncoderTrainer();
        trainer.runTraining("/Users/gavalian/Work/DataSpace/raw/extract_output.hipo");
        //NetworkTrackEncoder encoder = new NetworkTrackEncoder();
        //encoder.init();
        
        //TrackEncoderTrainer.readDataSet("/Users/gavalian/Work/DataSpace/raw/extract_output.hipo", 20);
    }
}
