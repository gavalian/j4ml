/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.network2;

import j4np.physics.Vector3;
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
public class NetworkTrainer {
    
    public static double[] normalize(float[] data, double min, double max){
        double[] newData = new double[data.length];
        double normalization = max - min;
        for(int i = 0; i < data.length; i++){
            if(data[i]<min||data[i]>max) return null;
            newData[i] = ((double) (data[i] - min))/normalization; 
        }
        return newData;
    }
    public static double[] join(double[] a,double[] b){
        double[] result = new double[a.length+b.length];
        for(int i = 0; i < a.length; i++) result[i] = a[i];
        for(int i = 0; i < b.length; i++) result[i+a.length] = b[i];
        return result;        
    }
    
    public static DataSet readParametersSet(String filename, int maxcount){
        HipoReader reader = new HipoReader();
        reader.open(filename);
        DataSet data = new DataSet(12,4);
        
        int count = 0;
        Node meansNode = new Node(1001,4,DataType.FLOAT,6);
        Node slopesNode = new Node(1001,5,DataType.FLOAT,6);
        Node vectorNode = new Node(1001,6,DataType.FLOAT,6);
        Node vertexNode = new Node(1001,7,DataType.FLOAT,6);
        Node descNode = new Node(1001,1,DataType.SHORT,2);
        
        Event event = new Event();
        int counter = 0;
        while(reader.hasNext()==true){
            reader.nextEvent(event);
            int position = event.scan(1001,4);
            meansNode    = event.read(meansNode, position);
            
            position = event.scan(1001,5);
            slopesNode    = event.read(slopesNode, position);
            
            position   = event.scan(1001,6);
            vectorNode = event.read(vectorNode, position);
            
            position   = event.scan(1001,7);
            vertexNode = event.read(vertexNode, position);
            
            position   = event.scan(1001,1);
            descNode = event.read(descNode, position);
            
            double[] inputMean  = NetworkTrainer.normalize(meansNode.getFloat(), 0.0, 112.0);
            double[] inputSlope  = NetworkTrainer.normalize(slopesNode.getFloat(), -1.0, 1.0);
            
            float[]  vector = vectorNode.getFloat();
            float[]  vertex = vertexNode.getFloat();
            
            Vector3  v3 = new Vector3(vector[0],vector[1],vector[2]);
            //System.out.println(  v3.mag());
            double[] output = new double[4];
            short[]  description = descNode.getShort();
            if(v3.mag()<10.0&&vertex[2]>-20&&vertex[2]<30&&description[0]==1&&description[1]<0){
                output[0] = v3.mag()/10.0;
                output[1] = v3.theta()/(Math.PI*0.25);
                output[2] = (v3.phi()+Math.PI)/(Math.PI*2);
                output[3] = (vertex[2] + 20.0)/50.0;
                if(inputMean!=null&&inputSlope!=null&&output[1]<1.0){
                    double[] input = NetworkTrainer.join(inputMean,inputSlope);
                    data.add(new DataSetRow(input,output));
                    counter++;
                }
            }
            if(counter>=maxcount) break;
        }
        return data;
    }
    
    public static String getString(double[] params){
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < params.length; i++){
            str.append(String.format("%12.5f ", params[i]));
        }
        return str.toString();
    }
    public static void trainParameters(){
        DataSet ds = NetworkTrainer.readParametersSet("extract_output_balanced.hipo", 100000);
        System.err.println("LOADED Data SAMPLE : size = " + ds.getRows().size());
        NetworkTrackParameters network = new NetworkTrackParameters();
        network.create(new int[]{12,24,24,4});
        ds.shuffle();
        DataSet[] tr = ds.split(0.8,0.2);
        //System.out.println(tr[0]);
        network.train(tr[0], 50);
        
        int size = tr[1].getRows().size();
        for(int i = 0; i < size; i++){
            double[] input = tr[1].getRowAt(i).getInput();
            double[] desired = tr[1].getRowAt(i).getDesiredOutput();
            network.getNetwork().setInput(input);
            network.getNetwork().calculate();;
            double[] output = network.getNetwork().getOutput();
            System.out.println(NetworkTrainer.getString(desired) + " " + NetworkTrainer.getString(output));
        }
        
    }
    
    public static void main(String[] args){
        NetworkTrainer.trainParameters();
    }
}
