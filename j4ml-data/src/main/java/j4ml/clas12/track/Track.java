/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.clas12.track;

import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoReader;

/**
 *
 * @author gavalian
 */
public class Track {
    
    private Bank    trackBank = null;
    private String  trackBankName = "TimeBasedTrkg::TBTracks";
    
    public Track(HipoReader reader){
        trackBank = reader.getBank(trackBankName);
    }
    
    public void read(Event event){
        event.read(trackBank);
    }
    
    
}
