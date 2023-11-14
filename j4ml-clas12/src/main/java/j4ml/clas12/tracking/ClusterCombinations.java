/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.tracking;

import j4np.physics.Vector3;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class ClusterCombinations {
    
    
    public List<Vector3>  vectors = new ArrayList<>();
    public List<Vector3>  vertex  = new ArrayList<>();
    
    //private final int BYTES_PER_ROW = 14*4;
    private final int BYTES_PER_ROW = 20*4;
    private ByteBuffer   cBuffer = null;
    private int        activeRow = 0;
    
    public ClusterCombinations(int size){
        allocate(size);
        reset();
    }
    
    public ClusterCombinations(){
        allocate(450000);
        reset();
    }
    
    public ClusterCombinations setRow(int ar){
        activeRow = ar;
        return this;
    }
    
    public List<Vector3>  getVectors(){ return this.vectors;}
    public List<Vector3>  getVertex(){ return this.vertex;}
    
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
    
    public void setSlope(int column, double slope){
        int offset = activeRow*BYTES_PER_ROW + 4 + 8 + 6*4 + 6*4 + column*4;
        cBuffer.putFloat(offset,(float) slope);
    }
    
    public double getSlope(int column){
        int offset = activeRow*BYTES_PER_ROW + 4 + 8 + 6*4 + 6*4 + column*4;
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
    
    public void getFeatures(float[] features, int row){
        setRow(row);
        //double[] features = new double[6];
        for(int i =0; i < 6; i++) features[i] = (float) ( getMean(i) / 112.0);
        //return features;
    }
    
    public double[] getFeatures(int row){
        setRow(row);
        double[] features = new double[6];
        for(int i =0; i < 6; i++) features[i] = getMean(i);
        return features;
    }
    
    public double[] getSlopes(int row){
        setRow(row);
        double[] features = new double[6];
        for(int i =0; i < 6; i++) features[i] = getSlope(i);
        return features;
    }
    
    public int countIds(int row){
        int counter = 0;
        setRow(row);
        for(int i = 0; i < 6; i++){
            if(this.getId(i)>0) counter++;
        }
        return counter;
    }
    
    public double distance(int row, double[] means){
        double distance = 0.0;
        setRow(row);
        for(int i = 0; i < means.length; i++){
            double value = this.getMean(i);
            distance += Math.abs(means[i] - value);
        }
        return distance;
    }
    
    public int bestMatch(int[] cid, double[] means){
        int index = -1;
        double bestDistance = 112.0;
        for(int row = 0; row < getSize(); row++){
            if(countMatches(row,cid)==5){
                double distance = distance(row,means);
                if(distance<bestDistance){
                    bestDistance = distance; index = row;
                }
            }
        }
        return index;
    }
    
    public int countMatches(int row, int[] cid){
        int counter = 0;
        this.setRow(row);
        for(int i = 0; i < cid.length; i++){
            if(cid[i]==this.getId(i)) counter++;
        }
        return counter;
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
    
    public int   find(int[] ids, boolean complete){
        int size = getSize();
        int index = -1;
        for(int i = 0; i < size; i++){
            this.setRow(i);
            boolean flag = true;
            for(int r = 0; r < 6; r++){
                /*if(ids[0]==getId(0)&&ids[1]==getId(1)
                        &&ids[2]==getId(2)&&ids[3]==getId(3)
                        &&ids[4]==getId(4)&&ids[5]==getId(5)) return i;*/
                if(ids[r]>0){
                    if(ids[r]!=getId(r)) flag = false;
                }
                
            }
            if(flag==true) return i;
        }
        return index;
    }
    
    public final void add(int[] ids, double[] means){
        if(getSize()>440000){
            System.out.println("no more : too many combinations......");
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
    
    
    public final void add(int[] ids, double[] means, double[] slopes){
        if(getSize()>440000){
            System.out.println("no more : too many combinations......");
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
                setSlope(i,slopes[i]);
                //System.out.println(" setting slope " + i + " = " + slopes[i] + " , read back = " + getSlope(i));
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
    
    public String getRowString(int row){
        StringBuilder str = new StringBuilder();
        setRow(row);
        int status = getStatus();        
        double probability = getProbability();
        str.append(String.format("%3d , %8.6f : (", status,probability));
        for(int k = 0; k < 6; k++){ str.append(String.format("%4d ", getId(k)));}
        str.append(" ) ( ");
        for(int k = 0; k < 6; k++){
            str.append(String.format("%6.2f ", getMean(k)));
        }
        str.append(" ) ( ");
        for(int k = 0; k < 6; k++){
            str.append(String.format("%6.2f ", getSlope(k)));
        }
        str.append(" )");
        return str.toString();
    }
    
    public String getString(boolean supress){
        StringBuilder str = new StringBuilder();
        int size = cBuffer.getInt(0);
        str.append("combinations: " + size).append("\n");
        str.append("----------------------\n");
        for(int i = 0; i < size; i++){
            String rowString = getRowString(i);
            if(supress==false){
                str.append(rowString).append("\n");
            } else {
                int status = setRow(i).getStatus();
                if(status!=0){
                    str.append(rowString).append("\n");
                }
            }
                       
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
                str.append(" ) ( ");
                for(int k = 0; k < 6; k++){
                    str.append(String.format("%6.2f ", getSlope(k)));
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
    
    
    private void analyzeCharge(int charge){
        int index = getMaxItem(charge);
        int  size = this.getSize();
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
                setRow(index).setStatus(charge*10+charge);
                index = getMaxItem(charge);
                if(index<0) break;
                if(setRow(index).getProbability()<0.5) break;
            }
        }
    }
    
    private void analyzeChargeNuevo(int charge){
        

                
        for(int i = 0; i < this.getSize(); i++){
            setRow(i);
            int      type = getStatus();
            double   prob = getProbability();
            
            if(prob>0.5){
                setStatus(charge*10+charge);
            } else {
                setStatus(-getStatus());
            }
            //if(getStatus()==type&&getProbability()>max){
            //    max = getProbability();
            //    index = i;
            //}
        }
        /*
        int index = getMaxItem(charge);
        int  size = this.getSize();
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
                setRow(index).setStatus(charge*10+charge);
                index = getMaxItem(charge);
                if(index<0) break;
                if(setRow(index).getProbability()<0.5) break;
            }
        }*/
    }
    
    public void copyTo(ClusterCombinations comb, List<Integer> statusList){
        int nrows = this.getSize();
        for(int i = 0; i < nrows; i++){
            int status = setRow(i).getStatus();
            if(statusList.contains(status)==true){
                int[] ids = this.getLabels(i);
                double[] featurs = this.getFeatures(i);
                double[] slopes = this.getSlopes(i);
                comb.add(ids, featurs,slopes);
                int size = comb.getSize();
                comb.setRow(size-1).setProbability(setRow(i).getProbability());
                comb.setRow(size-1).setStatus(status);
            }
        }
    }
    
    public void removeMatches(ClusterCombinations comb){
        int nrows = getSize();
        int nrowsMatch = comb.getSize();
        for(int i = 0; i < nrows; i++){
            int[] ids = getLabels(i);
            for(int r = 0; r < nrowsMatch; r++){
                comb.setRow(r);
                if(comb.contains(ids)==true){
                    setRow(i).setStatus(0);           
                }
            }
        }
    }
    
    public void analyze(){
        
        //int  size = this.getSize();
        int indexPositive = getMaxItem(2);
        int indexNegative = getMaxItem(1);
        
        
        double probabilityPositive = 0.0;
        double probabilityNegative = 0.0;
        
        if(indexPositive>=0) probabilityPositive = setRow(indexPositive).getProbability();
        if(indexNegative>=0) probabilityNegative = setRow(indexNegative).getProbability();
        if(probabilityPositive>probabilityNegative){
            analyzeChargeNuevo(2);
            analyzeChargeNuevo(1);
        } else {
            analyzeChargeNuevo(1);
            analyzeChargeNuevo(2);
        }
        /*if(probabilityPositive>probabilityNegative){
            analyzeCharge(2);
            analyzeCharge(1);
        } else {
            analyzeCharge(1);
            analyzeCharge(2);
        }*/
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
