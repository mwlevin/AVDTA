/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.editor;

import avdta.gui.editor.visual.rules.NodeRule;

/**
 *
 * @author ml26893
 */
public interface AbstractNodeRulePanel 
{
    public abstract void cancel();
    
    public abstract void addRule(NodeRule rule);
    
    public abstract void saveRule(NodeRule rule);
}
