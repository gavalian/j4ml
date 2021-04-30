/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.network;

import j4ml.deepnetts.data.DataSetReader;
import j4ml.deepnetts.data.HipoDataLoader;
import j4ml.deepnetts.data.LSVMDataProvider;
import j4ml.deepnetts.utils.ArchiveUtils;
import j4ml.deepnetts.utils.NetworkFlavor;
import java.util.List;
import javax.visrec.ml.data.DataSet;
import org.jlab.jnp.utils.options.OptionStore;

/**
 *
 * @author gavalian
 */
public class DeepNettsInterface {
    
    public static void trainClassifierLSVM(String lsvmTrain, String lsvmTest, int nepochs){
        LSVMDataProvider provider = new LSVMDataProvider();
        
        DataSet  train = provider.readFile(lsvmTrain);
        DataSet  test  = provider.readFile(lsvmTest);
         DeepNettsClassifier classifier = new DeepNettsClassifier();
        classifier.init(new int[]{6,24,24,12,3});
        classifier.train(train,nepochs);
//classifier.train(chunks[0],nepochs);
        classifier.evaluate(test);
        
        classifier.save("trackClassifier.network");

    }
    
    public static void trainClassifier(String filename, String zipFileName, Integer run, NetworkFlavor flavor, int nepochs, int max, boolean balance){
        
        DataSet inputData = HipoDataLoader.readDataSet(filename, max);
        
        inputData.shuffle();
        
        //DataSet[]  chunks = inputData.split(0.8,0.2);
        
        //chunks[0].shuffle();
        //chunks[1].shuffle();;
        
        DeepNettsClassifier classifier = new DeepNettsClassifier();
        classifier.init(new int[]{6,24,24,12,3});
        classifier.train(inputData,nepochs);
//classifier.train(chunks[0],nepochs);
        //classifier.evaluate(chunks[1]);
        
        //classifier.save("trackClassifier.network");
        List<String> networkStream = classifier.getNetworkStream();
        String outputFile = String.format("network/%s/%s/trackClassifier.network", run.toString(), flavor.getName());
        ArchiveUtils.addInputStream(zipFileName, outputFile, networkStream);
    }
    
    public static void trainFixer(String filename, String zipFileName, Integer run, NetworkFlavor flavor, int nepochs, int max, boolean balance){
        
        /*List<float[]> trainList = 
                DataSetReader.readHipo(filename, max);
        
        List<float[]> trSet = DataSetReader.chunk(trainList, 0.0, 0.8);
        List<float[]> tsSet = DataSetReader.chunk(trainList, 0.8, 0.2);
        System.out.println(" Imported Data set : \ntraining size = "                 
                + trSet.size() + "\ntesting size = " + tsSet.size());
        
        DataSet data = DeepNettsEncoder.createDataset(trSet);*/
        DataSet data = HipoDataLoader.readEncoderData(filename, max);
        
        //DataSet[] chunks = data.split(0.8,0.2);
        
        //data.shuffle();
        
        DeepNettsEncoder encoder = new DeepNettsEncoder();
        encoder.init(new int[]{6,12,12,6,12,12,6});
        encoder.train(data, nepochs);
        //encoder.save("trackFixer.network");
        List<String> networkStream = encoder.getNetworkStream();
        
        String outputFile = String.format("network/%s/%s/trackFixer.network", run.toString(), flavor.getName());
        ArchiveUtils.addInputStream(zipFileName, outputFile, networkStream);
        //ArchiveUtils.addInputStream(zipFileName, run, "trackFixer.network", networkStream);
        //AutoEncoderMetrics metrics = new AutoEncoderMetrics();        
        //metrics.evaluate(encoder.getNetwork(), tsSet);
    }
    
    public static void main(String[] args){
        //DeepNettsInterface.trainClassifierLSVM("dc_combined_sample_70k.lsvm","dc_combined_sample_70k_test.lsvm", 5000);        
        OptionStore store = new OptionStore("deep-netts");
        store.addCommand("-classifier", "train a classifier network");
        store.addCommand("-fixer", "train a classifier network");      
        
        store.getOptionParser("-classifier").addOption("-b", "false", "balance the data sample");
        store.getOptionParser("-classifier").addOption("-e", "240", "number of epochs to train");
        store.getOptionParser("-classifier").addOption("-max", "-1", "maximum number of samples to load");
        store.getOptionParser("-classifier").addRequired("-run","run number for the trained network");
        store.getOptionParser("-classifier").addRequired("-network","network filename");
        store.getOptionParser("-classifier").addOption("-flavor","default","flavor of the network");
        
        store.getOptionParser("-fixer").addOption("-b", "false", "balance the data sample");
        store.getOptionParser("-fixer").addOption("-e", "240", "number of epochs to train");
        store.getOptionParser("-fixer").addRequired("-run","run number for the trained network");
        store.getOptionParser("-fixer").addRequired("-network","network filename");
        store.getOptionParser("-fixer").addOption("-max", "-1", "maximum number of samples to load");
        store.getOptionParser("-fixer").addOption("-flavor","default","flavor of the network");
        
        store.parse(args);
        
        if(store.getCommand().compareTo("-classifier")==0){
            List<String> input = store.getOptionParser("-classifier").getInputList();
            int          max   = store.getOptionParser("-classifier").getOption("-max").intValue();
            int       epochs   = store.getOptionParser("-classifier").getOption("-e").intValue();
            boolean  balance   = store.getOptionParser("-classifier").getOption("-b").stringValue().compareTo("true")==0;
            Integer      run   = store.getOptionParser("-classifier").getOption("-run").intValue();
            String   network   = store.getOptionParser("-classifier").getOption("-network").stringValue();
            String    flavor   = store.getOptionParser("-classifier").getOption("-flavor").stringValue();
            
            NetworkFlavor   networkFlavor = NetworkFlavor.getType(flavor);
            if(networkFlavor==NetworkFlavor.UNDEFINED){
                System.out.println("ERROR: unknown flavor\n");
                System.out.println("choose from the list below:");
                NetworkFlavor.showTypeList();
            } else {
                DeepNettsInterface.trainClassifier(input.get(0), network, run, networkFlavor, epochs, max, balance);
            }
        }
        
        if(store.getCommand().compareTo("-fixer")==0){
            List<String> input = store.getOptionParser("-fixer").getInputList();
            int          max   = store.getOptionParser("-fixer").getOption("-max").intValue();
            int       epochs   = store.getOptionParser("-fixer").getOption("-e").intValue();
            boolean  balance   = store.getOptionParser("-fixer").getOption("-b").stringValue().compareTo("true")==0;
            Integer      run   = store.getOptionParser("-fixer").getOption("-run").intValue();
            String   network   = store.getOptionParser("-fixer").getOption("-network").stringValue();
            String    flavor   = store.getOptionParser("-fixer").getOption("-flavor").stringValue();
            
            NetworkFlavor   networkFlavor = NetworkFlavor.getType(flavor);
            if(networkFlavor==NetworkFlavor.UNDEFINED){
                System.out.println("ERROR: unknown flavor\n");
                System.out.println("choose from the list below:");
                NetworkFlavor.showTypeList();
            } else {            
                DeepNettsInterface.trainFixer(input.get(0), network, run, networkFlavor, epochs, max, balance);
            }
        }
    }
}
