package netdesign;


import avdta.network.ReadNetwork;
import avdta.network.node.*;
import java.util.HashMap;


public class Street {

    private String name;
    private HashMap<Integer, NodeRecord> lights;
    private boolean contiguous;
    private int control;

    public Street(String street_name, NodeRecord i) {
        name = street_name;
        lights = new HashMap<>();
        i.setType(ReadNetwork.SIGNAL);
        lights.put(i.getId(), i);
        contiguous = true;
        control = ReadNetwork.SIGNAL;
    }

    public Street(String s, int c) {
        name = s;
        lights = new HashMap<>();
        contiguous = true;
        control = c;
    }

    public boolean addNode(NodeRecord i) {
        if (i.getType() == control) {
            lights.put(i.getId(), i);
            return true;
        }
        return false;
    }

    public boolean flipAllIntersections() {
        if(contiguous) {
            if(control == ReadNetwork.SIGNAL) {
                control = ReadNetwork.RESERVATION + ReadNetwork.FCFS;
                lights.values().forEach(intersection -> intersection.setType(control));
                return true;
            } else if (control == ReadNetwork.RESERVATION + ReadNetwork.FCFS) {
                control = ReadNetwork.SIGNAL;
                lights.values().forEach(intersection -> intersection.setType(control));
                return true;
            }
        }
        return false;
    }

    public boolean flipIntersection(int id) {
        if(!contiguous) {
            if (lights.containsKey(id)) {
                int t = lights.get(id).getType();
                if (t == ReadNetwork.SIGNAL) {
                    lights.get(id).setType(ReadNetwork.RESERVATION + ReadNetwork.FCFS);
                    return true;
                } else if (t == ReadNetwork.RESERVATION + ReadNetwork.FCFS) {
                    lights.get(id).setType(ReadNetwork.SIGNAL);
                    return true;
                }
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void allowInterUpdates() {
        contiguous = false;
    }

    public void disableInterUpdates() {
        contiguous = true;
    }

    public HashMap<Integer, NodeRecord> getLights() {
        return lights;
    }

    public int getControl() {
        return control;
    }
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Street) {
            Street otherObj = (Street) obj;
            return name.equals(otherObj.name) && control == otherObj.control;
        }
        return false;
    }

    @Override
    public String toString() {
        return name + "=" + control;
    }
}
