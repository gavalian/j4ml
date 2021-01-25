/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author gavalian
 */
public class DataIterator {
        private ByteBuffer indexSet = null;

    public DataIterator(){
        allocate(100);
    }
    
    public DataIterator(int maxIndex){
        allocate(maxIndex);
    }
    
    private void allocate(int maxIndex){
        byte[] indexBytes = new byte[(maxIndex+1)*4];
        indexSet = ByteBuffer.wrap(indexBytes);
        indexSet.order(ByteOrder.LITTLE_ENDIAN);        
        indexSet.putInt(0, 0);        
    }
    
    public void reset(){
       indexSet.putInt(0, 0);
    }
    
    public int  count(){
        return indexSet.getInt(0);
    }
    
    public void addRange(int min, int max){
        for(int i = min; i < max; i++) addIndex(i);        
    }
    
    public boolean contains(int index){
        for(int i = 0; i < count(); i++){
            if(getIndex(i)==index) return true;
        }
        return false;
    }
            
    public void addIndex(int index){
        int ncount = count();
        ncount++;
        if(ncount>=indexSet.capacity()/4){
            System.out.println("BankIndexSet:: error : you reached maximum of " + indexSet.capacity()/4);
            return;
        }
        int offset = (ncount)*4;
        indexSet.putInt(offset, index);
        indexSet.putInt(0, ncount);
    }
    
    @Override
    public String toString(){
        int ncount = count();
        StringBuilder str = new StringBuilder();
        str.append(String.format("%5d : ", ncount));
        for(int i = 0; i < ncount; i++){
            str.append(String.format("%5d ", indexSet.getInt((i+1)*4)));
        }
        return str.toString();
    }
    
    public int getIndex(int order){
       return indexSet.getInt((order+1)*4);
    }
    
    public void show(){
        System.out.println(toString());
    }
}
