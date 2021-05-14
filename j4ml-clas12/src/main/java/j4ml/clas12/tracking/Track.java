/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.tracking;

import j4np.physics.Vector3;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jlab.jnp.hipo4.data.Bank;

/**
 *
 * @author gavalian
 */
public class Track {
    
    public Vector3 vector = new Vector3();
    public Vector3 vertex = new Vector3();
    public double  chi2 = 1000.0;
    public int     charge = 0;
    public int     sector = 0;
    public int[]  clusters = new    int[6];
    public double[]  means = new double[6];
    public double[]  slopes = new double[6];
    
    
    public Track(){
        
    }
    
    public boolean complete(){
        for(int i = 0; i < clusters.length; i++)
            if(clusters[i]<=0) return false;
        return true;
    }
    
    public int clusterCount(){
        int count = 0; 
        for(int i = 0; i < clusters.length; i++){
            if(clusters[i]>0) count++;
        }
        return count;
    }
    
    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append(String.format("%3d %4d %8.2f ",sector, charge,chi2));
        str.append(String.format(": %9.4f %9.4f %9.4f ",vector.mag(), 
                Math.toDegrees(vector.theta()),
                Math.toDegrees(vector.phi())
                ));
        str.append(String.format(": %8.4f %8.4f %8.4f ==>  ",
                vertex.x(), vertex.y(), vertex.z()
                ));
        for(int i = 0; i < 6; i++) str.append(String.format("%6.3f ", means[i]));
        for(int i = 0; i < 6; i++) str.append(String.format("%4d ", clusters[i]));
        return str.toString();
    }
    
    public static List<Track> read(Bank tracks, Bank clusters){
        Map<Integer,Integer> map = clusters.getMap("id");
        List<Track> list = new ArrayList<>();
        int nrows = tracks.getRows();
        for(int i = 0; i < nrows; i++){
            Track trk = new Track();
            trk.vector.setXYZ(tracks.getFloat("p0_x",  i),
                    tracks.getFloat("p0_y",  i),tracks.getFloat("p0_z",  i));
            trk.vertex.setXYZ(tracks.getFloat("Vtx0_x",  i),
                    tracks.getFloat("Vtx0_y",  i),tracks.getFloat("Vtx0_z",  i));
            trk.charge = tracks.getInt("q", i);
            trk.sector = tracks.getInt("sector", i);
            int ndf = tracks.getInt("ndf", i);
            trk.chi2 = tracks.getFloat("chi2", i)/ndf;
            
            int[] ids = tracks.getIntArray(6, "Cluster1_ID", i);
            for(int c = 0; c < 6; c++){
                trk.clusters[c] = ids[c];
                if(ids[c]>=0) {
                    int index = map.get(ids[c]);
                    trk.means[c] = clusters.getFloat("avgWire", index);
                    trk.slopes[c] = clusters.getFloat("fitSlope", index);
                }
            }
            list.add(trk);
        }
        return list;
    }
    
    public float[] getFeatures(){
        float[] features = new float[6];
        for(int i = 0; i < 6; i++) features[i] = (float) (means[i]/112.0);
        return features;
    }
    
    public int matchCount(int[] cid){
        int counter = 0;
        for(int i = 0; i < clusters.length; i++){
            if(clusters[i]>0){
                if(clusters[i]==cid[i]) counter++;
            }
        }
        return counter;
    }
    
    public boolean match(Track trk){
        if( Math.abs(trk.vector.mag()-this.vector.mag())/this.vector.mag() > 0.1) return false;
        if( Math.abs(this.vector.theta()-trk.vector.theta())*57.29>10.0) return false;
        if( Math.abs(this.vector.phi()-trk.vector.phi())*57.29>10.0) return false;
        //if( Math.abs(this.vertex.z()-trk.vertex.z())>4.0) return false;
        return true;
    }
    
    public int findMatch(List<Track> tracks){
        for(int i = 0; i < tracks.size(); i++){
            if(this.match(tracks.get(i))==true) return i;
        }
        return -1;  
    }
    
    public boolean equals(int[] segments){
        for(int i = 0; i < this.clusters.length; i++){
            if(segments[i]>0){
                if(clusters[i]!=segments[i]) return false;
            }
        }
        return true;
    }
    
    public static boolean contains(List<Track> trkList, int[] segments){
        for(int i = 0; i < trkList.size(); i++){
            if(trkList.get(i).equals(segments)==true) return true;
        }
        return false;
    }
    
    public boolean isValid(){
        if(vertex.z()<-15||vertex.z()>5) return false;
        if(this.chi2>10) return false;
        if(vector.mag()<0.5||vector.mag()>10) return false;
        return true;
    }
    
    public static List<Track> getValid(List<Track> tracks){
        List<Track> valid = new ArrayList<>();
        for(int i = 0; i < tracks.size(); i++){
            if(tracks.get(i).isValid()){
                valid.add(tracks.get(i));
            }
        }
        return valid;
    }
    
    public static List<Track> getComplete(List<Track> tracks){
        List<Track> valid = new ArrayList<>();
        for(int i = 0; i < tracks.size(); i++){
            if(tracks.get(i).isValid()){
                if(tracks.get(i).clusterCount()==6) valid.add(tracks.get(i));
            }
        }
        return valid;
    }
    
    public static List<Track> getCompleteWithMissing(List<Track> tracks){
        List<Track> valid = new ArrayList<>();
        for(int i = 0; i < tracks.size(); i++){
            //if(tracks.get(i).clusterCount()==5) System.out.println("Hy ? ");
            if(tracks.get(i).isValid()){
                if(tracks.get(i).clusterCount()==5) valid.add(tracks.get(i));
            }
        }
        return valid;
    }
    
}
