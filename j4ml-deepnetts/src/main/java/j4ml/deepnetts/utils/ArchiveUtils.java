/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.Zip4jUtil;

import net.lingala.zip4j.model.ZipParameters;

/**
 *
 * @author gavalian
 */
public class ArchiveUtils {
    
    public List<String> getList(String file){
        List<String> dirs = new ArrayList<>();
        ZipFile zip = new ZipFile(file);
        ZipParameters pars = new ZipParameters();
        pars.setOverrideExistingFilesInZip(true);
        
        return dirs;
    }
    
    public static InputStream createFromList(List<String> dataFile) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();        
        int nsize = dataFile.size();
        for(int i = 0; i < nsize; i++){
            baos.write(dataFile.get(i).getBytes());
            baos.write("\n".getBytes());
        }        
        return new ByteArrayInputStream(baos.toByteArray());
    }
    
    public static boolean removeFile(String zipfile, String file){
        try {
            ZipFile zip = new ZipFile(zipfile);
            zip.removeFile(file);
        } catch (ZipException ex) {
            Logger.getLogger(ArchiveUtils.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    public static boolean hasFile(String zipfile, String file){
        try {
            ZipFile zip = new ZipFile(zipfile);
            List<FileHeader> headers = zip.getFileHeaders();
            for(int i = 0; i < headers.size(); i++){
                String name = headers.get(i).getFileName();
                if(name.compareTo(file)==0) return true;
                //System.out.println("name : " + name + " has = " + name.compareTo(file));
            }
        } catch (ZipException ex) {
            Logger.getLogger(ArchiveUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public static void list(String zipfile, String pattern){
        List<String>  files = new ArrayList<>();
        try {
            ZipFile zip = new ZipFile(zipfile);
            List<FileHeader> headers = zip.getFileHeaders();
            for(int i = 0; i < headers.size(); i++){
                String name = headers.get(i).getFileName();
                files.add(name);
                //System.out.println("name : " + name + " has = " + name.compareTo(file));
            }
        } catch (ZipException ex) {
            Logger.getLogger(ArchiveUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Collections.sort(files);

        System.out.println("- LIST OF THE FILES IN THE ARCHIVE");
        System.out.println("-------------");
        for(int i = 0; i < files.size(); i++){
            System.out.printf(": %s\n",files.get(i));
        }
        System.out.println("-------------\n\n");
    }
    
    public static void addInputStream(String zipfile, Integer run, String outputName, List<String> dataFile){
        String directory = String.format("network/%s/%s", run.toString(),outputName);
        System.out.println("[exporting] -> " + directory);
        
        try {
            InputStream stream = ArchiveUtils.createFromList(dataFile);
            ZipFile zip = new ZipFile(zipfile);
            ZipParameters pars = new ZipParameters();
            pars.setOverrideExistingFilesInZip(true);
            pars.setFileNameInZip(directory);
            zip.addStream(stream, pars);
        } catch (IOException ex) {
            Logger.getLogger(ArchiveUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void addInputStream(String zipfile, String outputName, List<String> dataFile){
        String directory = String.format("%s",outputName);
        System.out.println("[exporting] -> " + directory);
        
        try {
            InputStream stream = ArchiveUtils.createFromList(dataFile);
            ZipFile zip = new ZipFile(zipfile);
            ZipParameters pars = new ZipParameters();
            pars.setOverrideExistingFilesInZip(true);
            pars.setFileNameInZip(directory);
            zip.addStream(stream, pars);
        } catch (IOException ex) {
            Logger.getLogger(ArchiveUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void addFile(String zipfile, String directory, String datafile){
        try {
            List<String> dirs = new ArrayList<>();
            ZipFile zip = new ZipFile(zipfile);            
            ZipParameters pars = new ZipParameters();
            pars.setFileNameInZip("network/5014/"+datafile);
            pars.setOverrideExistingFilesInZip(true);
            zip.addFile(datafile,pars);
        } catch (ZipException ex) {
            Logger.getLogger(ArchiveUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
       //ArchiveUtils.addFile("ejmlclas12.network","/network/default","trackParameters.network");
       //List<String> data = Arrays.asList("# data description--","12,4","2.5,3.6,4.7,5.8,6.1");
       //ArchiveUtils.addInputStream("ejmlclas12.network",5028,"stream.network",data);
       
       boolean flag = ArchiveUtils.hasFile("ejmlclas12.network", "network/5038/default/trackClassifier.network");
       System.out.println("existance = " + flag);
       ArchiveUtils.removeFile("ejmlclas12.network", "network/5038/default/trackClassifier.network");
       flag = ArchiveUtils.hasFile("ejmlclas12.network", "network/5038/default/trackClassifier.network");
       System.out.println("existance = " + flag);
    }
}
