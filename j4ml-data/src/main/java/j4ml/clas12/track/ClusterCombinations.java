/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.track;

import java.nio.ByteBuffer;

/**
 *
 * @author gavalian
 */
public class ClusterCombinations {
    
    private final int BYTES_PER_ROW = 14*4;
    private ByteBuffer   cBuffer = null;
    private int        activeRow = 0;
    
    public ClusterCombinations(int size){
        allocate(size);
        reset();
    }
    
    public ClusterCombinations(){
        allocate(35000);
        reset();
    }
    
    public ClusterCombinations setRow(int ar){
        activeRow = ar;
        return this;
    }
    
    public int getStatus(){
      int offset = activeRow*BYTES_PER_ROW + 4;
      return cBuffer.getInt(offset);
    }
    
    public double getProbability(){
      int offset = activeRow*BYTES_PER_ROW + 4 + 4;
      return cBuffer.getFloat(offset);
    }
    
    public void setProbability(double p){
      int offset = activeRow*BYTES_PER_ROW + 4 + 4;
      cBuffer.putFloat(offset, (float) p);
    }
    
    public void setStatus(int status){
        int offset = activeRow*BYTES_PER_ROW + 4;
        cBuffer.putInt(offset,status);
    }
    
    public void setId(int column, int id){
        int offset = activeRow*BYTES_PER_ROW + 4 + 8 + column*4;
        cBuffer.putInt(offset, id);
    }
    
    public int getId(int column){
        int offset = activeRow*BYTES_PER_ROW + 4 + 8 + column*4;
        return cBuffer.getInt(offset);
    }
    
    public void setMean(int column, double mean){
        int offset = activeRow*BYTES_PER_ROW + 4 + 8 + 6*4 + column*4;
        cBuffer.putFloat(offset,(float) mean);
    }
    
    public double getMean(int column){
        int offset = activeRow*BYTES_PER_ROW + 4 + 8 + 6*4 + column*4;
        return cBuffer.getFloat(offset);        
    }
    
    public final void allocate(int entries){
        int size = 4 + entries*BYTES_PER_ROW; 
        byte[] array = new byte[size];
        cBuffer = ByteBuffer.wrap(array);        
    }
    
    public int getSize(){
        return cBuffer.getInt(0);
    }
    
    
    public double[] getFeatures(int row){
        setRow(row);
        double[] features = new double[6];
        for(int i =0; i < 6; i++) features[i] = getMean(i);
        return features;
    }
    
    public int[] getLabels(int row){
        setRow(row);
        int[] labels = new int[6];
        for(int i =0; i < 6; i++) labels[i] = this.getId(i);
        return labels;
    }
    
    public void setProbabilities(double[] prob){
        if(prob.length!=getSize()){
            System.err.println("combinations:: error , the array size does not match the sample size");
            return;
        }
        for(int i = 0; i < prob.length; i++){
            setRow(i).setProbability(prob[i]);
        }
    }
    
   
    public int partition(int arr[], int begin, int end){
        double pivot = setRow(end).getProbability();
                //arr[end];
        int i = (begin-1);
        
        for (int j = begin; j < end; j++) {
            if (setRow(j).getProbability() <= pivot) {
                i++;
                
                int swapTemp = arr[i];
                arr[i] = arr[j];
                arr[j] = swapTemp;
            }
        }
        
        int swapTemp = arr[i+1];
        arr[i+1] = arr[end];
        arr[end] = swapTemp;
        
        return i+1;
    }
    
    public void quickSort(int[] index, int begin, int end){
        if (begin < end) {
            int partitionIndex = partition(index, begin, end);
            quickSort(index, begin, partitionIndex-1);
            quickSort(index, partitionIndex+1, end);
        }
    }
    
    public int[] sortQuick(){
        int[] index = new int[this.getSize()];
        for(int i = 0; i < index.length; i++) index[i] = i;
        quickSort(index,0,index.length-1);
        return index;
    }
    
    public int[] sort(){
        int[] index = new int[this.getSize()];
        for(int i = 0; i < index.length; i++) index[i] = i;
        int n = index.length;
        int temp;
        
        for(int i=0;i<n;i++) {
            for(int j=0;j<n-i-1;j++){
                int offset = j*BYTES_PER_ROW + 4 + 4;
                //if(cBuffer.getFloat(offset)>cBuffer.getFloat(offset+BYTES_PER_ROW)){
                /*System.out.printf("f = %.4f %.4f, s = %.4f %.4f",
                        cBuffer.getFloat(offset),cBuffer.getFloat(offset+BYTES_PER_ROW),
                        this.setRow(index[j]).getProbability(),this.setRow(index[j+1]).getProbability());*/
                if(this.setRow(index[j]).getProbability()<=this.setRow(index[j+1]).getProbability()){
                    
                    temp = index[j];
                    index[j] = index[j+1];
                    index[j+1] = temp;
                }
                /*if(a[j]>a[j+1]){
                    temp=a[j];  
                    a[j]=a[j+1];  
                    a[j+1]=temp;               
                } */   
            } 
        } 
        return index;
    }
    
    public int   find(int[] ids){
        int size = getSize();
        int index = -1;
        for(int i = 0; i < size; i++){
            this.setRow(i);
            if(ids[0]==getId(0)&&ids[1]==getId(1)
               &&ids[2]==getId(2)&&ids[3]==getId(3)
               &&ids[4]==getId(4)&&ids[5]==getId(5)) return i;
        }
        return index;
    }
    
    
    public final void add(int[] ids, double[] means){
        if(getSize()>34000){
            System.out.println("too many combinations......");
            return;
        }
        if(ids.length!=6||means.length!=6){
            System.out.println("cluster-combinations: error: adding failed");
        } else {
            int nrows = cBuffer.getInt(0);
            activeRow = nrows;
            nrows++;
            cBuffer.putInt(0, nrows);
            setStatus(0);
            //int offset = activeRow*BYTES_PER_ROW + 4 + 8 ;
            //int offsetf = activeRow*BYTES_PER_ROW + 4 + 8 + 6*4;
            //byte[] tempi = new byte[4*6];
            //float[] tempf = new float[6];
            
            //System.arraycopy(tempi, 0, cBuffer.array(), offset, 6*4);
            //System.arraycopy(tempi, 0, cBuffer.array(), offsetf, 6*4);
            for(int i = 0; i < 6 ; i ++){
                setId(i,ids[i]);
                setMean(i,means[i]);
            }
        }
    }
    
    public String toOrderedString(int[] order){
        StringBuilder str = new StringBuilder();
        int size = cBuffer.getInt(0);
        str.append("combinations: " + size).append("\n");
        str.append("----------------------\n");
        for(int i = 0; i < size; i++){
            setRow(order[i]);
            int status = getStatus();
            double probability = getProbability();
            str.append(String.format("%3d , %8.6f : (", status,probability));
            for(int k = 0; k < 6; k++){
                str.append(String.format("%4d ", getId(k)));
            }
            str.append(" ) ( ");
            for(int k = 0; k < 6; k++){
                str.append(String.format("%6.2f ", getMean(k)));
            }
            str.append(" )\n");
        }
        return str.toString();
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        int size = cBuffer.getInt(0);
        str.append("combinations: " + size).append("\n");
        str.append("----------------------\n");
        for(int i = 0; i < size; i++){
            setRow(i);
            int status = getStatus();
            if(status!=0){
                double probability = getProbability();
                str.append(String.format("%3d , %8.6f : (", status,probability));
                for(int k = 0; k < 6; k++){
                    str.append(String.format("%4d ", getId(k)));
                }
                str.append(" ) ( ");
                for(int k = 0; k < 6; k++){
                    str.append(String.format("%6.2f ", getMean(k)));
                }
                str.append(" )\n");
            }
        }
        return str.toString();
    }
    
    public final void reset(){
        cBuffer.putInt(0, 0);
    }
    
    public int  getMaxItem(int type){
        double   max = 0.0;
        int    index = -1;
        for(int i = 0; i < this.getSize(); i++){
            setRow(i);
            if(getStatus()==type&&getProbability()>max){
                max = getProbability();
                index = i;
            }
        }
        return index;
    }
    
    public boolean contains(int[] ids){
        for(int i = 0; i < ids.length; i++){
            if(this.getId(i)==ids[i]) return true;
        }
        return false;
    }
    
    public void analyze(){
        
        int  size = this.getSize();
        int index = getMaxItem(1);
        int counter = 0;
        
        if(index>=0){
            while(true){
                //System.out.printf("step = %4d, index = %4d , probability %8.6f\n",counter,index,setRow(index).getProbability());
                //System.out.println(this.toString());
                counter++;
                int[] ids = getLabels(index);
                for(int i = 0; i < size; i++){
                    setRow(i);
                    if(i!=index&&contains(ids)==true&&(getStatus()==1||getStatus()==2)){
                        setStatus(-getStatus());
                    }
                }
                setRow(index).setStatus(11);
                index = getMaxItem(1);
                if(index<0) break;
                if(setRow(index).getProbability()<0.5) break;
            }
        }
        
        index = getMaxItem(2);
        
        if(index>=0){
            while(true){
                //System.out.printf("step = %4d, index = %4d , probability %8.6f\n",counter,index,setRow(index).getProbability());
                //System.out.println(this.toString());
                counter++;
                int[] ids = getLabels(index);
                for(int i = 0; i < size; i++){
                    setRow(i);
                    if(i!=index&&contains(ids)==true&&(getStatus()==1||getStatus()==2)){
                        setStatus(-getStatus());
                    }
                }
                setRow(index).setStatus(22);
                index = getMaxItem(2);
                if(index<0) break;
                if(setRow(index).getProbability()<0.5) break;
            }
        }
    }
    
    public void getIterator(){
        
    }
    
    public static void main(String[] args){
        ClusterCombinations cc = new ClusterCombinations();
        cc.add(new int[]{1,2,3,4,5,6}, new double[]{1.2,2.3,3.4,4.5,5.6,6.7});
        cc.add(new int[]{11,12,13,14,15,16}, new double[]{1.2,2.3,3.4,4.5,5.6,6.7});
        cc.add(new int[]{21,22,23,24,25,26}, new double[]{1.2,2.3,3.4,4.5,5.6,6.7});
        System.out.println(cc);
        
        cc.reset();
        
        System.out.println(cc);
        
    }
}
