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
                if(value>0){
                    dataImage.setRGB(x, y, 
                            getColor(fullColor.getRed(),fullColor.getGreen(), fullColor.getBlue())
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
}
