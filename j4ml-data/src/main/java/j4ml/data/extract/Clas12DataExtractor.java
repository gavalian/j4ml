/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.extract;

import j4ml.data.clas12.ChainDataExtractor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class Clas12DataExtractor {
    public static void main(String[] args){
        
        List<String>  fileList = new ArrayList<>();
        if(args.length>0){
            for(int i = 0; i < args.length; i++) fileList.add(args[i]);
        }
        String filename = "/Users/gavalian/Work/DataSpace/raw/out_ai_005038.00165-00169.hipo";
        ChainDataExtractor ce = new ChainDataExtractor(Arrays.asList(filename));
        //ChainDataExtractor ce = new ChainDataExtractor(fileList);
        RawDataDenoise ext = new RawDataDenoise("dc_deniose_output.lsvm");
        ce.addExtractor(ext);
        
        LSTMExtractor extlstm = new LSTMExtractor("dc_lstm_data.lsvm");
        //ce.setLimit(2000);
        
        ce.addExtractor(extlstm);
        ce.process();
    }
}
