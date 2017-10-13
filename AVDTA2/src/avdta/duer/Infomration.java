/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

/**
 *
 * @author hdx
 */
class Information {
    public static Information FALSE = new Information(false);
    
    private boolean info;
        
    public Information (boolean info){
        this.info = info;
    }
    
//    public boolean perceiveInfo(){
//        return info;
//    }
//    
//    public int toInt(){
//        int a = (info)? 1: 0;
//        return a;
//    }
}
