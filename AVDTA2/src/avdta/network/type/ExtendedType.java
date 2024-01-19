/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.type;

/**
 *
 * @author mlevin
 */
public class ExtendedType extends Type
{
    private Type base;
    
    public ExtendedType(int type, String description, Type base)
    {
        super(type, description);
        this.base = base;
    }
    
    public Type getBase()
    {
        return base.getBase();
    }
    
    public int getCode()
    {
        return super.getCode() + base.getCode();
    }
}
