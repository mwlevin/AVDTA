/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.util;

import avdta.network.Simulator;
import avdta.util.DownloadElevation;

/**
 * This is an interface used by classes that have a long run time to provide progress reports.
 * An instantiation of this class may be provided to certain classes (e.g. {@link Simulator}, {@link DownloadElevation}).
 * As they run, they will call either of the two methods provided in this interface.
 * @author Michael
 */
public interface StatusUpdate
{
    /**
     * Notify of an update.
     * @param estimate the estimate of the proportion of run time completed
     * @param interval the update interval
     */
    public abstract void update(double estimate, double interval);
    
    /**
     * Notify of an update.
     * @param estimate the estimate of the proportion of run time completed
     * @param interval the update interval
     * @param text the update text
     */
    public abstract void update(double estimate, double interval, String text);
}
