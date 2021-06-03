/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.ejml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 *
 * @author gavalian
 */
public class ArchiveProvider {
    
    private ZipReader reader = new ZipReader();
    private String    flavor = "default";
    private String    archiveFile = "";
    
    public ArchiveProvider(String file){
        this.archiveFile = file;    
    }
    
    public void setFlavor(String __fl){
        flavor = __fl;
    }
    
    public List<String> getFile(String filename, int run){
        int adjustedRun = findEntry(run);
        String path = String.format("network/%d/%s/%s", adjustedRun,flavor,filename);
        System.out.println("[archive:provider] -> reading file : " + path);
        return reader.readTxt(this.archiveFile, path);
    }
    
    public boolean hasFileForRun(int run, String path){
        //ZipFile zipFile = null;
        //reader.
        int derivedRun = findEntry(run);
        String filename = String.format("network/%d/%s/%s", derivedRun, this.flavor, path);
        String filter   = String.format("network/%d/%s", derivedRun, this.flavor);
        Set<String>  fileList = reader.getFileList(this.archiveFile, filter);
        for(String entry : fileList){
            System.out.println( entry + " " + (entry.compareTo(filename)==0));
            if(entry.compareTo(filename)==0) return true;
        }
        return false;
    }
    
    public boolean hasFile(String filename, int run){
        int adjustedRun = findEntry(run);
        String path = String.format("network/%d/%s/%s", adjustedRun,flavor,filename);
        return true;
    }
        
    public Integer findEntry(int run){
        String         filter = String.format(".*/.*/%s", flavor);
        Set<String> directories = reader.getDirectoryList(archiveFile, filter);
        System.out.println("filter [" + filter + "]");
        System.out.println("---> directory size = " + directories.size());
        List<Integer> array = new ArrayList<>();
        for(String directory : directories){
            String[] tokens = directory.split("/");
            Integer   value = Integer.parseInt(tokens[1]);
            array.add(value);
            //System.out.println(value);
        }
        Collections.sort(array);
        for(int i = 0; i < array.size(); i++) System.out.printf("%4d : %6d\n",i,array.get(i));
        int index = Collections.binarySearch(array,run);
        System.out.println(" seach for "  + run + " , index =  " + index);

        if(index>=0)  return array.get(index);
        if(index==-1) return array.get(0);
        int trueIndex = Math.abs(index)-2;
        return array.get(trueIndex); 
    }
    
    public static void main(String[] args){
        
        ArchiveProvider provider = new ArchiveProvider("../j4ml-package/etc/ejmlclas12.network");
        int index = 0;
        
        
        provider.hasFileForRun(5038, "trackParametersPositive.network");
        /*
        index = provider.findEntry(5);
        System.out.println("RUN = " + index);
        index = provider.findEntry(1000);
        System.out.println("RUN = " + index);
        index = provider.findEntry(1200);
        System.out.println("RUN = " + index);        
        index = provider.findEntry(5038);
        System.out.println("RUN = " + index);
        index = provider.findEntry(5100);
        System.out.println("RUN = " + index);
        index = provider.findEntry(5800);
        System.out.println("RUN = " + index);
        index = provider.findEntry(6800);
        System.out.println("RUN = " + index);
        index = provider.findEntry(7600);
        System.out.println("RUN = " + index);
        index = provider.findEntry(8014);
        System.out.println("RUN = " + index);
        
        
        List<String> fileContent = provider.getFile("trackClassifier.network", 5060);
        System.out.println("lines read = " + fileContent.size());*/
    }
}
