/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.editor;

import avdta.gui.editor.visual.rules.LinkRule;

/**
 *
 * @author ml26893
 */
public interface AbstractLinkRulePanel 
{
    public abstract void addRule(LinkRule rule);
    
    public abstract void saveRule(LinkRule rule);
    public abstract void cancel();
}
