/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.visualization;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 *
 * @author gavalian
 */
public class DataImageMaker {
    private int xDim = 0;
    private int yDim = 0;
    private BufferedImage dataImage = null;
    private Color         emptyColor = new Color(55,55,64);
    private Color         fullColor  = new Color(255,105,81);
    
    public DataImageMaker(int x, int y){
        xDim = x; yDim = y;
    }
    
    private void createImageBuffer(){
        dataImage =
                new BufferedImage(xDim, yDim,
                        BufferedImage.TYPE_INT_ARGB);
        
    }
    
    public void createImage(INDArray array, int dim, int index){
        long length = array.size(dim);
        if(length!=xDim*yDim){
            System.out.println("error: dimentions do not match size = " + length
            + " dim = " + (xDim*yDim));
            return;
        }
        
        createImageBuffer();
        
        int  counter = 0;
        int[]   indx = new int[]{index,0};
        
        for(int y = 0; y < yDim; y++){
            for(int x = 0; x < xDim; x++){
                indx[1] = counter;
                double value = array.getDouble(indx);
                /*
                if(value>0){
                    dataImage.setRGB(x, y, 
                            getColor(fullColor.getRed(),fullColor.getGreen(), fullColor.getBlue())
                    );
                } else {
                    dataImage.setRGB(x, y, 
                            getColor(emptyColor.getRed(),emptyColor.getGreen(), emptyColor.getBlue())
                    );
                }*/
                if(value>0){
                    if(value>1.0) value = 1.0;
                    int intensity = (int) (255*value);
                    dataImage.setRGB(x, y, 
                            getColor(intensity,intensity,intensity)
                    );
                } else {
                   dataImage.setRGB(x, y, 
                            getColor(emptyColor.getRed(),emptyColor.getGreen(), emptyColor.getBlue())
                    ); 
                }
                counter++;
            }
        }
    }
    
    public void magnify(int xmag, int ymag){
        BufferedImage img = new BufferedImage(xDim*xmag, yDim*ymag,
                        BufferedImage.TYPE_INT_ARGB);
        
        for(int x = 0; x < xDim; x++){
            for(int y = 0; y < yDim; y++){
                int color = dataImage.getRGB(x, y);
                for(int xm = x*xmag; xm < (x+1)*xmag; xm++){
                    for(int ym = y*ymag; ym < (y+1)*ymag; ym++){
                        img.setRGB(xm, ym, color);
                    }
                }
            }
        }
        
        dataImage = img;
    }
    public static BufferedImage magnifyImage(BufferedImage image, int xmag, int ymag){
        
        int xDim = image.getWidth();
        int yDim = image.getHeight();
        BufferedImage img = new BufferedImage(xDim*xmag, yDim*ymag,
                        BufferedImage.TYPE_INT_ARGB);
        for(int x = 0; x < xDim; x++){
            for(int y = 0; y < yDim; y++){
                int color = image.getRGB(x, y);
                for(int xm = x*xmag; xm < (x+1)*xmag; xm++){
                    for(int ym = y*ymag; ym < (y+1)*ymag; ym++){
                        img.setRGB(xm, ym, color);
                    }
                }
            }
        }
        return img;
    }
    
    public void save(String filename){        
        try {
            File outputfile = new File(filename);
            ImageIO.write(dataImage, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(DataImageMaker.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    public int getColor(int r, int g, int b){
        int a = 255;
        int col = (a << 24) | (r << 16) | (g << 8) | b;
        return col;
    }
    public static int color(int r, int g, int b){
        int a = 255;
        int col = (a << 24) | (r << 16) | (g << 8) | b;
        return col;
    }
    public static BufferedImage makeImage(double[] data, int x, int y){
        BufferedImage dataImage =
                new BufferedImage(x, y,
                        BufferedImage.TYPE_INT_ARGB);
        int xc = 0; int yc = 0;
        for(int i = 0; i < data.length; i++){
            int cv = (int) (255*data[i]);
            int color = DataImageMaker.color(cv,cv,cv);
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
        BufferedImage dataImage = DataImageMaker.makeImage(data, x, y);
        try {
            File outputfile = new File(filename);
            ImageIO.write(dataImage, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(DataImageMaker.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    public static void writeImage(BufferedImage image, double[] data, int xoffset, int yoffset, int x, int y){
        int xc = 0; int yc = 0;
        for(int i = 0; i < data.length; i++){
            int cv = (int) (255*data[i]);
            int color = DataImageMaker.color(cv,cv,cv);
            image.setRGB(xoffset + xc, yoffset + yc, color);
            xc++;
            if(xc>=x){
                xc = 0;
                yc++;
            }
        }
    }
    
    public static void saveImage(double[] data, int x, int y, String filename, int xmag, int ymag){
        BufferedImage dataImage = DataImageMaker.makeImage(data, x, y);
        BufferedImage  newImage = DataImageMaker.magnifyImage(dataImage,xmag,ymag);
        try {
            File outputfile = new File(filename);
            ImageIO.write(newImage, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(DataImageMaker.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    public static void saveImage(double[] input, double[] desired, double[] output, int x, int y , String filename){        
        BufferedImage dataImage =
                new BufferedImage(x*3, y,
                        BufferedImage.TYPE_INT_ARGB);
        DataImageMaker.writeImage(dataImage, input, 0, 0, x, y);
        DataImageMaker.writeImage(dataImage, desired, x, 0, x, y);
        DataImageMaker.writeImage(dataImage, output, 2*x, 0, x, y);
        try {
            File outputfile = new File(filename);
            ImageIO.write(dataImage, "png", outputfile);
        } catch (IOException ex) {
            Logger.getLogger(DataImageMaker.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
}
