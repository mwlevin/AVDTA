/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.priority;

import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;

/**
 *
 * @author micha
 */
public class ASTAuction implements Priority
{
    private int ast_duration;
    
    public ASTAuction(int ast)
    {
        this.ast_duration = ast;
    }
    
    public int compare(Vehicle v1, Vehicle v2)
    {
        int dtime1 = 0;
        int dtime2 = 0;
        
        if(v1 instanceof PersonalVehicle)
        {
            dtime1 = ((PersonalVehicle)v1).getDepTime();
        }
        
        if(v2 instanceof PersonalVehicle)
        {
            dtime2 = ((PersonalVehicle)v2).getDepTime();
        }
        
        int ast1 = dtime1 / ast_duration;
        int ast2 = dtime2 / ast_duration;
        
        if(ast1 != ast2)
        {
            return ast1 - ast2;
        }
        else
        {
            return (int)Math.ceil(10000*(v2.getVOT() - v1.getVOT()));
        }
    }
        
}
