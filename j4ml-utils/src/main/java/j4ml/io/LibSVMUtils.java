/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author gavalian
 */
public class LibSVMUtils {
    
    
    public LibSVMUtils(){}
    
    public static double[] toArray(String lsvmString, int size){
        double[] array = new double[size];
        String[] tokens = lsvmString.split("\\s+");
        for(int i = 0; i < tokens.length; i++){
            if(tokens[i].contains(":")==true){
                String[]  pair = tokens[i].split(":");
                int      index = Integer.parseInt(pair[0]);
                double   value = Double.parseDouble(pair[1]);
                if((index-1)>=size||(index-1)<0){
                    //System.out.println("libsvm : error >> index " + index + " is larger than array size of "+ size);
                } else {
                    array[index-1] = value;
                }
            }
        }
        return array;
    }
    
    public static String toLSVMString(double[] array, double threshold){
       StringBuilder str = new StringBuilder();
       for(int i = 0; i < array.length; i++){
           if(array[i]>threshold) str.append(String.format("%d:%.2f ", i+1,array[i]));
       }
       return str.toString();
    }
    
    public static int    count(double[] a, double threshold){
        int count = 0;
        for(int i = 0; i < a.length; i++){
            if(a[i]>threshold) count++;
        }
        return count;
    }
    
    public static double coincidence(double[] a, double[] b, double threshold){
        int coincidence = 0;
        int count = 0;
        for(int i = 0; i < a.length; i++){
            if(a[i]>threshold){
                count++;
                if(b[i]>threshold) coincidence++;
            }
        }
        return ((double) coincidence)/count;
    }
    
    public static double noise(double[] a, double[] b, double threshold){
        int noise = 0;
        int count = 0;
        for(int i = 0; i < a.length; i++){
            if(b[i]>threshold&&a[i]<0){
                noise++;
            }
            if(b[i]>0) count++;
        }
        //System.out.println( noise + " " + count);
        return ((double) noise)/count;
    }
    
    public double[][] reshape(double[] a, int rows, int columns){
        double[][] result = new double[rows][columns];
        for(int i = 0; i < a.length; i++){
            
        }
        return result;
    }
    
    public static int color(int r, int g, int b){
        int a = 255;
        int col = (a << 24) | (r << 16) | (g << 8) | b;
        return col;
    }
    
    
    public static BufferedImage image(double[] data, int x, int y){
        BufferedImage dataImage =
                new BufferedImage(x, y,
                        BufferedImage.TYPE_INT_ARGB);
        int xc = 0; int yc = 0;
        for(int i = 0; i < data.length; i++){
            int cv = (int) (255*data[i]);
            int color = LibSVMUtils.color(cv,cv,cv);
            dataImage.setRGB(xc, yc, color);
            xc++;
            if(xc>=x){
                xc = 0;
                yc++;
            }
        }
        return dataImage;
    }
    
    public static void saveImage(double[] data, int x, int y, String filename){
        BufferedImage dataImage = LibSVMUtils.image(data, x, y);
        try {
            File outputfile = new File(filename);
            ImageIO.write(dataImage, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(LibSVMUtils.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    public static void main(String[] args){
       
        
    }
}
