/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import java.util.List;

/**
 *
 * @author crams
 */
public interface Signalized 
{
    public void addPhase(Phase p);
    public void setOffset(double o);
    public double getOffset();
    public List<Phase> getPhases();
}
