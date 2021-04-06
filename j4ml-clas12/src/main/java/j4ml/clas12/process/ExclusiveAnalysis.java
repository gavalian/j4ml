/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.process;

import j4np.physics.LorentzVector;
import java.util.ArrayList;
import java.util.List;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;

/**
 *
 * @author gavalian
 */
public class ExclusiveAnalysis {
    
    public static List<LorentzVector> getVectors(Bank bank){
        List<LorentzVector> vecs = new ArrayList<>();
        if(bank.getInt("pid", 0)==11){
            LorentzVector electron = new LorentzVector();
            electron.setPxPyPzM(bank.getFloat("px", 0),
                    bank.getFloat("py", 0),bank.getFloat("pz", 0),0.0005
            );
            vecs.add(electron);
            for(int i = 1; i < bank.getRows(); i++){
                int pid = bank.getInt("pid", i);
                if(pid==211||pid==-211){
                    LorentzVector pion = new LorentzVector();
                    pion.setPxPyPzM(
                            bank.getFloat("px", i),
                            bank.getFloat("py", i),bank.getFloat("pz", i),0.13957018
                    );
                    vecs.add(pion);
                }
            }
        }
        return vecs;
    }
    public static List<LorentzVector> getVectorsP(Bank bank){
        List<LorentzVector> vecs = new ArrayList<>();
        if(bank.getInt("pid", 0)==11){
            LorentzVector electron = new LorentzVector();
            electron.setPxPyPzM(bank.getFloat("px", 0),
                    bank.getFloat("py", 0),bank.getFloat("pz", 0),0.0005
            );
            vecs.add(electron);
            for(int i = 1; i < bank.getRows(); i++){
                int pid = bank.getInt("pid", i);
                if(pid==-211){
                    LorentzVector pion = new LorentzVector();
                    pion.setPxPyPzM(
                            bank.getFloat("px", i),
                            bank.getFloat("py", i),bank.getFloat("pz", i),0.13957018
                    );
                    vecs.add(pion);
                }
                if(pid==2212){
                    LorentzVector pion = new LorentzVector();
                    pion.setPxPyPzM(
                            bank.getFloat("px", i),
                            bank.getFloat("py", i),bank.getFloat("pz", i),0.938
                    );
                    vecs.add(pion);
                }
            }
        }
        return vecs;
    }
    public static void main(String[] args){
        
        String filename = "/Users/gavalian/Work/DataSpace/raw/exclusive_e_pi_pi.hipo";
        HipoChain chain = new HipoChain();
        
        chain.addFile(filename);
        chain.open();
        Bank particles = chain.getBank("REC::Particle");
        Event event = new Event();
        
        while(chain.hasNext()==true){
            chain.nextEvent(event);
            event.read(particles);
            List<LorentzVector>  vec = ExclusiveAnalysis.getVectors(particles);
            LorentzVector   beam = new LorentzVector(0.0,0.0,10.5,10.5);
            LorentzVector target = new LorentzVector(0.0,0.0,0.0,0.938);
            if(vec.size()==3){
                beam.add(target);
                for(int i = 0; i < 3; i++) beam.sub(vec.get(i));
                System.out.printf("%12.6f\n",beam.mass());
            }
        }
    }
}
