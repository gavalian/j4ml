/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.ejml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author gavalian
 */
public class ZipReader {
    
    public List<String> read(String zipFileName, String filename){
        byte[] buffer = new byte[2048];
        
        List<String> entryLines = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(zipFileName);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream stream = new ZipInputStream(bis)) {
            
            ZipEntry entry;
            
            while ((entry = stream.getNextEntry()) != null) {
                System.out.println("entry -> " + entry.getName() + " , type = " + entry.isDirectory());
                if(entry.isDirectory()==false&&entry.getName().contains(filename)){
                    int nread = stream.read(buffer);
                    while(nread>0){
                        String line = new String(buffer,0,nread);
                        //System.out.println(line);
                        entryLines.add(line);
                        nread = stream.read(buffer);
                    }
                    //entry.
                }
                
            }       
        } catch (Exception e){
            
        }
        return entryLines;
    }
    
    protected String[] splitFilePath(String path){
        int position = path.lastIndexOf("/");
        if(position>0){
            String pathTo = path.substring(0, position);
            String file   = path.substring(position+1);
            return new String[]{pathTo,file};
        }
        return null;
    }
    public Set<String> getFileList(String zipFileName, String filter){
        Set<String> entryLines = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(zipFileName);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream stream = new ZipInputStream(bis)) {
            
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                String[] path = this.splitFilePath(entry.getName());
                boolean match = Pattern.matches(filter, path[0]);                
                //System.out.println("entry -> [" + path[0] + "] [" + path[1] + 
                //        " ], type = " + entry.isDirectory() + ", match = " + match);
                if(match==true){                                        
                    entryLines.add(entry.getName());
                }
            }
        } catch (Exception e){
        }
        return entryLines;
    }
    public Set<String> getDirectoryList(String zipFileName, String filter){
        
        Set<String> entryLines = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(zipFileName);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream stream = new ZipInputStream(bis)) {
            
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                String[] path = this.splitFilePath(entry.getName());
                boolean match = Pattern.matches(filter, path[0]);
                
                //System.out.println("entry -> [" + path[0] + "] [" + path[1] + 
                //        " ], type = " + entry.isDirectory() + ", match = " + match);
                if(match==true){                                        
                    entryLines.add(entry.getName());
                }
            }
        } catch (Exception e){
        }
        return entryLines;
    }
    
    public List<String> readTxt(String zipFileName, String filename){
        List<String> entryLines = new ArrayList<>();
        try {
            byte[] buffer = new byte[2048];
            
            ZipFile zipFile = new ZipFile(zipFileName);
            ZipEntry entry  = zipFile.getEntry(filename);
            
            InputStream input = zipFile.getInputStream(entry);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            String line = null;
            while( (line = br.readLine()) != null){
                entryLines.add(line);
            }
                        
            /*int nread = entry.read(buffer);
            while(nread>0){
            String line = new String(buffer,0,nread);
            System.out.println(line);
            nread = stream.read(buffer);
            }
            
            } catch (IOException ex) {
            Logger.getLogger(ZipReader.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        } catch (IOException ex) {
            Logger.getLogger(ZipReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entryLines;
    }
    
    public void writeTxt(String zipFileName, List<String> lines){
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File(zipFileName)));
            ZipEntry        entry = new ZipEntry("network/5038/trackParametersPositive.network");
            out.putNextEntry(entry);
            for(String line : lines){
                out.write(line.getBytes());
            }
            out.closeEntry();
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ZipReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ZipReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        
        ZipReader reader = new ZipReader();
        /*List<String> lines = reader.read("etc/ejml/ejmlData.zip","5038/trackClassifier.network");
        System.out.println(" size = " + lines.size());
        for(int i = 0; i < lines.size(); i++){
            System.out.println(i + " : " + lines.get(i));
        }*/
        
        /*List<String> lines2 = reader.readTxt("etc/ejml/ejmlData.zip","network/5038/trackClassifier.network");
        System.out.println(" size = " + lines2.size());
        for(int i = 0; i < lines2.size(); i++){
            System.out.println(i + " : " + lines2.get(i));
        }
        List<String> lines = new ArrayList<>();
        
        lines.add("address:");
        lines.add("phone:");
        lines.add("email:");        
        reader.writeTxt("etc/ejml/ejmlData.zip",lines);*/
        
        Set<String> list = reader.getDirectoryList("../j4ml-deepnetts/ejmlclas12.network", ".*/.*/default");
        
        for(String item : list){
            System.out.println(" ->  " + item);
        }
    }
}
