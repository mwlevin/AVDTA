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
public class InfoClass {
    private Incident incident;
    private Information information;

    public InfoClass(Incident incident, Information information) {
        this.incident = incident;
        this.information = information;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public Information getInformation() {
        return information;
    }

    public void setInformation(Information information) {
        this.information = information;
    }
    
    
    
    
}
