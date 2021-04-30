/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package j4ml.deepnetts.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public enum NetworkFlavor {
    
    UNDEFINED ("undefined"),
    DEFAULT ("default"),
    DAHILA ("dahila"),
    JASMINE ("jasmine"),
    DAISY ("daisy"),
    LILAC ("lilac");
    
    private final String typename;
    
    NetworkFlavor(){
        typename = "dahila";
    }
    
    NetworkFlavor(String name){
        typename = name;
    }
    
    public String getName() {
        return typename;
    }
    
    public static List<String>  getTypeList(){
        List<String> list = new ArrayList<>();
        for(NetworkFlavor id: NetworkFlavor.values())
            list.add(id.getName());
        return list;
    }
    
    public static NetworkFlavor getType(String name) {
        name = name.trim();
        for(NetworkFlavor id: NetworkFlavor.values())
            if (id.getName().equalsIgnoreCase(name)) 
                return id;
        return UNDEFINED;
    }
    
    public static void  showTypeList(){
        List<String> list = NetworkFlavor.getTypeList();
        System.out.println("--------------------------------------------------");
        for(int i = 0; i < list.size(); i++){
            System.out.printf("\t flavor : %s\n", list.get(i));
        }
        System.out.println("--------------------------------------------------");
    }
}
