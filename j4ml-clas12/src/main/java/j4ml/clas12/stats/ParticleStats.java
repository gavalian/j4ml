/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.stats;

import org.jlab.jnp.hipo4.data.Bank;
import j4np.physics.LorentzVector;
import j4np.physics.Vector3;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
/**
 *
 * @author gavalian
 */
public class ParticleStats {
    
    int numberOfElectron = 0;
    int numberOfPositiveHadrons = 0;
    int numberOfNegativeHadrons = 0;

    public static List<LorentzVector>  getElectron(Bank bank){
        if(bank.getInt("pid", 0)==11){
            double chi2pid = bank.getFloat("chi2pid", 0);
            if(Math.abs(chi2pid)<5){
                LorentzVector vec = new LorentzVector();
                Vector3 vertex = new Vector3(
                        bank.getFloat("vx", 0), 
                        bank.getFloat("vy", 0), 
                        bank.getFloat("vz", 0));
                vec.setPxPyPzM(bank.getFloat("px", 0), 
                        bank.getFloat("py", 0), 
                        bank.getFloat("pz", 0), 
                        0.0005);
                if(vec.vect().mag()>2.5&&vertex.z()>-15&&vertex.z()<5.0) return Arrays.asList(vec);
            }
        }
        return new ArrayList<LorentzVector>();
    }
    
    public static List<LorentzVector>  getCharged(Bank bank, int charge){
        List<LorentzVector> vectors = new ArrayList<>();
        for(int i =1 ; i < bank.getRows();i++){
            int status = bank.getInt("status", i);
            double chi2pid = bank.getFloat("chi2pid", i);
            int q = bank.getInt("charge", i);
            if(status>2000&&status<3999&&Math.abs(chi2pid)<5){
                LorentzVector vector =  LorentzVector.withPxPyPzM(
                        bank.getFloat("px", i), bank.getFloat("py", i),
                        bank.getFloat("pz", i), 0.139
                );
                Vector3 vertex = new Vector3(
                        bank.getFloat("vx", i), 
                        bank.getFloat("vy", i), 
                        bank.getFloat("vz", i));
                if(q==charge&&vector.vect().mag()>0.4&&vertex.z()>-15&&vertex.z()<5.0) vectors.add(vector);
                //if(q==charge) vectors.add(vector);
            }
        }
        return vectors;
    }
    
    public void analyze(Bank bank){
        List<LorentzVector> electron = ParticleStats.getElectron(bank);
        if(electron.size()>0){
            this.numberOfElectron++;
            List<LorentzVector> positives = ParticleStats.getCharged(bank, 1);
            List<LorentzVector> negatives = ParticleStats.getCharged(bank, -1);
            if(positives.size()>0) this.numberOfPositiveHadrons++;
            if(negatives.size()>0) this.numberOfNegativeHadrons++;            
        }
    }
    
    public void show(){
        double fractionPositive = ((double) numberOfPositiveHadrons)/numberOfElectron;
        double fractionNegative = ((double) numberOfNegativeHadrons)/numberOfElectron;
        System.out.printf("\n>> %8d  %8d %8d %8.4f %8.4f\n",
                this.numberOfElectron,this.numberOfPositiveHadrons, this.numberOfNegativeHadrons,
                fractionPositive, fractionNegative);
    }
}
