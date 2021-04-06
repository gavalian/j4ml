/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.process;

import j4ml.clas12.network2.NetworkTrackClassifier;
import j4ml.clas12.network2.NetworkTrackParameters;
import j4ml.clas12.tracking.Track;
import j4np.physics.LorentzVector;
import j4np.physics.Vector3;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;

/**
 *
 * @author gavalian
 */
public class TrackAnalyzer {
    
    public static List<Track> readTracks(Bank tracks, Bank clusters){
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
            trk.chi2 = tracks.getFloat("chi2", i);
            int[] ids = tracks.getIntArray(6, "Cluster1_ID", i);
            for(int c = 0; c < 6; c++){
                trk.clusters[c] = ids[c];
                if(ids[c]>=0) {
                    int index = map.get(ids[c]);
                    trk.means[c] = clusters.getFloat("avgWire", index)/112.0;
                }
            }
            list.add(trk);
        }
        return list;
    }
    
    
    public static boolean quality(List<Track> list){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).complete()==false) return false;
            if(list.get(i).chi2>400.0) return false;
        }
        return true;
    }
    
    
    public static Vector3 getVector(NetworkTrackParameters net, Track trk){
        int sector = trk.sector;
        double[] results = net.evaluate(trk.means);
        Vector3 vec = new Vector3();
        double phi = results[2]*Math.PI*2.0-Math.PI + (sector-1)*Math.PI/3.0;
        //if(phi>Math.PI) phi = phi - Math.PI ;
        vec.setMagThetaPhi(results[0]*10.0, results[1]*Math.PI*0.5, phi);
        return vec;
    }
    
    public static List<LorentzVector> getMissing(List<Vector3> pvec, List<Vector3> nvec){

        List<LorentzVector> list = new ArrayList<>();
        
        LorentzVector   beam = new LorentzVector(0.0,0.0,10.5,10.5);
        LorentzVector target = new LorentzVector(0.0,0.0,0.0,0.938);
        beam.setPxPyPzM(0, 0, 10.5, 0.0005);
        target.setPxPyPzM(0, 0, 0, 0.938);
        
        LorentzVector electron = new LorentzVector();
        LorentzVector   proton = new LorentzVector();

        
        electron.setVectM(nvec.get(0), 0.0005);        
        proton.setVectM(pvec.get(0), 0.938);
        beam.add(target).sub(electron).sub(proton);
        
        list.add(LorentzVector.from(beam));
        return list;
    }
    
    public static void main(String[] args){
        
        String filename = "/Users/gavalian/Work/DataSpace/raw/exclusive_e_p.hipo";
        HipoChain chain = new HipoChain();
        
        chain.addFile(filename);
        chain.open();

        NetworkTrackParameters networkPos = new NetworkTrackParameters();
        networkPos.load("etc/trackParametersPositive.nnet");
        
        NetworkTrackParameters networkNeg = new NetworkTrackParameters();
        networkNeg.load("etc/trackParametersNegative.nnet");
        
        NetworkTrackClassifier classifier = new NetworkTrackClassifier();
        classifier.load("etc/trackClassifier.nnet");
        
        Bank tBank = chain.getBank("TimeBasedTrkg::TBTracks");
        Bank cBank = chain.getBank("TimeBasedTrkg::TBClusters");
        //Bank hBank = chain.getBank("HitBasedTrkg::HBClusters");
        
        Event event = new Event();        
        int counter = 0;
        //for(int i = 0; i < 10000; i++){
          while(chain.hasNext()){  
              counter++;
            chain.nextEvent(event);
            event.read(cBank);            
            event.read(tBank);
            
            List<Track>  list = TrackAnalyzer.readTracks(tBank,cBank);
            List<Vector3> positive = new ArrayList<>();
            List<Vector3> negative = new ArrayList<>();
            
            if(list.size()==2&&TrackAnalyzer.quality(list)==true){
                //System.out.println("event % " + counter);
                
                for(Track t : list){
                    //System.out.println(t);
                    
                    
                    if(t.charge>0){
                        Vector3  vec = TrackAnalyzer.getVector(networkPos, t);
                        //positive.add(vec);
                        positive.add(Vector3.from(t.vector));
                        //System.out.printf("infered positive >>> %12.5f %12.5f %12.5f\n",vec.mag(), vec.theta()*57.29,vec.phi()*57.29);
                    } else {
                        Vector3  vec = TrackAnalyzer.getVector(networkNeg, t);
                        
                        negative.add(vec);
                        //negative.add(Vector3.from(t.vector));
                        //System.out.printf("infered negative >>> %12.5f %12.5f %12.5f\n",vec.mag(), vec.theta()*57.29,vec.phi()*57.29);
                    }
                    
                                                  
//if(t.charge>0){
                      //  System.out.println(t);

                    //}
                }
                
                if(positive.size()==1&&negative.size()==1){
                        //System.out.println("SIZES = " + positive.size() + " : " + negative.size());
                        List<LorentzVector> missing = TrackAnalyzer.getMissing(positive, negative);
                        System.out.printf("%12.5f %12.5f %12.5f \n",
                                positive.get(0).mag(), negative.get(0).mag(),
                                missing.get(0).mass2()
                                );
                        
                }     
            }
            /*if(list.size()>0){
                System.out.println("event # " + i);           
                for(Track t : list){
                    if(t.chi2 < 300&&(t.sector==1||t.sector==3)&&t.charge>0){
                        System.out.println(t);
                        double[] result = network.evaluate(t.means);
                        double[] classes = classifier.evaluate(t.means);
                        System.out.printf(">>>>>>> %.4f %.4f %.4f <<<<< %d %d \n"
                                ,classes[0],classes[1],classes[2], classifier.getOutputClass(classes) , classes.length);
                        //for(int r = 0; r < 4; r++){
                        System.out.printf("******>>>> %9.4f %9.4f %9.4f %9.4f\n",
                                result[0]*10,Math.toDegrees(result[1]*Math.PI*0.5),
                                Math.toDegrees(result[2]*Math.PI*2-Math.PI),
                                result[3]*50.0-20
                        );
                       //cBank.show();
                       //hBank.show();
                       //}
                   }
               }
           }*/
           //bank.show();
        }
    }
}
