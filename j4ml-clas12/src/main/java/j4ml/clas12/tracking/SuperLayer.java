/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.tracking;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 *
 * @author gavalian
 */
public class SuperLayer {
    
    private ByteBuffer buffer = null;
    private ByteBuffer bufferZero = null;
    private Random     rand = new Random();
    
    public SuperLayer(){
        buffer = ByteBuffer.allocate(36*112*2);
        bufferZero = ByteBuffer.allocate(36*112*2);
    }
    
    public void random(){
        for(int i = 0; i < 120; i++){
            int index = rand.nextInt(36*112);
            short value = (short) rand.nextInt(32000);
            buffer.putShort(i*2, value);
        }
    }
    
    public void reset(){
        System.arraycopy(bufferZero.array(), 0, buffer.array(), 0, 36*112*2);
    }
    
    public void resetLoop(){
        for(int i = 0; i < 36*112; i++){
            buffer.putShort(i*2, (short) 0);
        }
    }
    
    public void test(int niter){
        long total = 0L;
        this.random();
        long then = System.currentTimeMillis();
        for(int i = 0; i < niter;i ++){            
            this.resetLoop();            
        }
        long now = System.currentTimeMillis();
        total += (now - then);
        double average = ((double) total )/niter;
        System.out.printf("iter = %d, time = %d, average = %.4f\n",niter,total,average);
    }
    
    public static void main(String[] args){
        SuperLayer sl = new SuperLayer();        
        sl.test(2000000);
    }
}
