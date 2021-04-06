/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.data;

import java.util.ArrayList;
import java.util.List;
import org.jlab.jnp.hipo4.data.DataType;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.Node;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class DataSetReader {
    
    public static List<float[]>  readHipo(String filename, int max){
        List<float[]>  dataList = new ArrayList<>();
        HipoReader reader = new HipoReader();
        reader.open(filename);
        Event event = new Event();
        int counter = 0;
        while(reader.hasNext()==true){
            /* Node   descNode = new Node(1001,1,DataType.SHORT,6);
             Node   chi2Node = new Node(1001,2,DataType.FLOAT,6);
             Node  meansNode = new Node(1001,4,DataType.FLOAT,6);
             Node vectorNode = new Node(1001,6,DataType.FLOAT,20);
             Node vertexNode = new Node(1001,7,DataType.FLOAT,20);*/
            reader.nextEvent(event);
            //event.scanShow();
            Node params = event.read(1001,1);
            Node   chi2 = event.read(1001,2);
            Node vertex = event.read(1001,7);
            Node  means = event.read(1001,4);
            //if(params.getShort(1)<0&&
            if(chi2.getFloat(0)<2.0
                    &&vertex.getFloat(2)>-15&&vertex.getFloat(2)<5){
                float[] data = new float[6];
                for(int i = 0; i < 6; i++) data[i] = (float) (means.getFloat(i)/112.0);
                dataList.add(data);
                counter++;
            }
            if(counter>=max) break;
        }
        return dataList;
    }
    
    public static float[] getCopy(float[] array){
       float[] result = new float[array.length];
       for(int i = 0; i < result.length; i++) result[i] = array[i];
       return result;
    }
    
    public static String getString(float[] array){
        StringBuilder str = new StringBuilder();
        str.append("[");
        for(int i = 0; i < array.length; i++)
            str.append(String.format("%7.4f ", array[i]));
        str.append("]");
        return str.toString();
    }
    
    public static String getString(float[] array, int start, int length,  String delim){
        StringBuilder str = new StringBuilder();
        //str.append("[");
        for(int i = 0; i < length; i++)
        {
            if(i!=0) str.append(delim);
            str.append(String.format("%.12f", array[i+start]));
        }
        //str.append("]");
        return str.toString();
    }
    
    public static String getString(float[] array, String delim){
        StringBuilder str = new StringBuilder();
        //str.append("[");
        for(int i = 0; i < array.length; i++)
        {
            if(i!=0) str.append(delim);
            str.append(String.format("%.12f", array[i]));
        }
        //str.append("]");
        return str.toString();
    }
    public static List<float[]> chunk(List<float[]> source, double start, double fraction){
        int size = source.size();
        List<float[]> result = new ArrayList<>();
        int startPosition = (int) (start*size);
        int dataFraction  = (int) (fraction*size);
        for(int i = 0; i < dataFraction; i++){
            result.add(source.get(startPosition+i));
        }
        return result;
    }
}
