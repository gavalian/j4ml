/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.data.clas12;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.io.HipoChain;
import org.jlab.jnp.hipo4.operations.BankIterator;
import org.jlab.jnp.hipo4.operations.BankSelector;

/**
 *
 * @author gavalian
 */
public class TrackExtraction extends DataExtractor {
    private Bank   tracksBank = null;
    private Bank   tdcBank = null;

    private Bank clustersBank = null;
    
    private BankSelector tracksSelector = null;
    private BankSelector clustersSelector = null;
    
    private BankIterator iter = new BankIterator();
    
    public TrackExtraction(String filename){
        output(filename);
    }
    
    @Override
    public void init(HipoChain chain){
        
        tracksBank   = new Bank(chain.getSchemaFactory().getSchema("TimeBasedTrkg::TBTracks"));
        tdcBank      = new Bank(chain.getSchemaFactory().getSchema("DC::tdc"));
        clustersBank = new Bank( chain.getSchemaFactory().getSchema("HitBasedTrkg::HBClusters"));
        
        tracksSelector = new BankSelector(chain.getSchemaFactory().getSchema("TimeBasedTrkg::TBTracks"));
        clustersSelector = new BankSelector(chain.getSchemaFactory().getSchema("HitBasedTrkg::HBClusters"));
        tracksSelector.add("sector==1");
        clustersSelector.add("sector==1");
        
        this.open();
    }
    
    public int[][] getArray(Bank bank){
        int nrows = bank.getRows();
        int[][] array = new int[36][112];
        for(int i = 0; i < nrows; i++){
            int sector = bank.getInt("sector", i);
            int layer = bank.getInt("layer", i);
            int component = bank.getInt("component", i);
            if(sector==1){
                array[layer-1][component-1] = 1;
            }
        }
        return array;
    }
    
    public int[][] getArrayClusters(Bank bank, Bank tdc){
        int nrows = bank.getRows();
        int[][] array = new int[36][112];
        for(int i = 0; i < nrows; i++){
            int sector = bank.getInt("sector", i);

            if(sector==1){
                for(int h = 1; h <= 12 ; h++){
                    int hit = bank.getInt("Hit"+h+"_ID",i);
                    if(hit>0){
                        int layer = tdc.getInt("layer", hit-1);
                        int component = tdc.getInt("component", hit-1);
                        array[layer-1][component-1] = 1;
                    }
                }                                
                
            }
        }
        return array;
    }
    
    public void showArray(int[][] array){
        System.out.println("----->");
        for(int y = 0; y < 36; y++){
            for(int x = 0; x < 112; x++){
                if(array[y][x]==0){
                    //System.out.printf("%2d",array[y][x]);
                    System.out.print("-");
                } else {
                    System.out.print("X");
                }
            }
            System.out.println();
        }
    }
    public boolean isValid(Bank tracks){
        boolean flag = true;
        for(int i = 1 ; i <= 6; i++){
            int cid = tracks.getInt("Cluster"+i+"_ID", 0);
            if(cid<=0) return false;
        }
        return true;
    }
    public String  getDataString(int[][] dc){
        StringBuilder str = new StringBuilder();
        int counter = 0;
        for(int y = 0; y < 36; y++){
            for(int x = 0; x < 112; x++){
                if(dc[y][x]>0){
                    str.append(String.format("%d:1.0 ", counter));
                }
                counter++;
            }
        }
        return str.toString();
    }
    public int[][] getArrayTracks(Bank tracks, Bank clusters, Bank tdc){
        int nrows = tracks.getRows();
        int[][] array = new int[36][112];
        for(int r = 0; r < nrows; r++){
            for(int c = 1; c <= 6; c++ ){
                int cid = tracks.getInt("Cluster"+c+"_ID", r);
                if(cid>0){
                    for(int h = 1; h <= 12; h++){
                        int hid = clusters.getInt("Hit"+h+"_ID", cid-1);
                        if(hid>0){
                            int layer = tdc.getInt("layer", hid-1);
                            int component = tdc.getInt("component", hid-1);
                            array[layer-1][component-1] = 1;
                        }
                    }
                }
            }
        }
        return array;
    }
    
    @Override
    public void process(Event event){   
        int counter = 0;
        int nevents = 0;
        event.read(tracksBank);
        event.read(clustersBank);
        event.read(tdcBank);
        //Map<Integer,List<Integer>> sectors = tracksBank.getMapList("sector", "q");
        //System.out.println(sectors.size() + "  " + tracksBank.getRows() );
        tracksSelector.getIterator(event, iter);
        Bank sectorOneTracks = tracksSelector.reduceBank(iter);
        /*if(sectors.containsKey(1)==true){
            System.out.println(" number of charged tracks = " + sectors.get(1));
        }*/
        
        if(sectorOneTracks.getRows()>0){
            //sectorOneTracks.show();
            if(sectorOneTracks.getRows()==1){
                int charge = sectorOneTracks.getInt("q", 0);
                
                if(charge<0){
                    
                    boolean valid = isValid(sectorOneTracks);
                    if(valid==true){
                        double chi2 = sectorOneTracks.getFloat("chi2", 0);
                        double vz   = sectorOneTracks.getFloat("Vtx0_z", 0);
                        
                        clustersSelector.getIterator(event, iter);
                        Bank sectorOneClusters = clustersSelector.reduceBank(iter);

                        if(sectorOneClusters.getRows()>8&&sectorOneClusters.getRows()<12
                                &&chi2<400&&vz>-30&&vz<5.0){
                            
                            //System.out.printf("-->>> [%3d] %10.2f %10.5f c = %5d\n",charge,chi2,vz,sectorOneClusters.getRows());                            
                            int[][] array = getArrayClusters(clustersBank,tdcBank);
                            //showArray(array);
                            
                            int[][] arrayTracks = this.getArrayTracks(sectorOneTracks, clustersBank, tdcBank);
                            
                            //showArray(arrayTracks);
                            //System.out.println(">>>>>>>\n>>>>>>>\n>>>>>\n");
                            
                            String dataTrack = this.getDataString(arrayTracks);
                            //System.out.println(dataTrack);
                            String dataAll   = this.getDataString(array);
                            outputLines.clear();
                            outputLines.add("1 " + dataTrack);
                            outputLines.add("0 " + dataAll);
                            write();
                        }
                    }
                }
            }
        }
    }
    
    public static void main(String[] args){
        String filename = "/Users/gavalian/Work/DataSpace/autoencoder/test_reduced.hipo";
        ChainDataExtractor ce = new ChainDataExtractor(Arrays.asList(filename));
        TrackExtraction ext = new TrackExtraction("driftchamber_tracks.lsvm");
        ce.addExtractor(ext);
        ce.setLimit(1000);
        ce.process();
    }
}
