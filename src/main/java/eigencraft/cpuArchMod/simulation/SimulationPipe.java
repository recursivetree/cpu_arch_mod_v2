package eigencraft.cpuArchMod.simulation;

import com.google.gson.JsonElement;

import java.util.ArrayList;

public class SimulationPipe implements SimulationAgent {
    private final ArrayList<SimulationAgent> connectedObjects =   new ArrayList<>();
    private boolean blocked = false;

    public void connect(SimulationAgent other){
        connectedObjects.add(other);
    }

    public void disconnect(SimulationAgent other){
        connectedObjects.remove(other);
    }

    public ArrayList<SimulationAgent> getConnections(){
        return connectedObjects;
    }

    @Override
    public void tick() {

    }

    @Override
    public void process(SimulationMessage message) {
        if (!blocked){
            blocked = true;
            for (SimulationAgent simObj:connectedObjects){
                simObj.process(message);
            }
            blocked = false;
        }
    }

    @Override
    public JsonElement getConfigData() {
        return null;
    }

    static {
        SimulationAgent.register(SimulationPipe.class.getSimpleName(),SimulationPipe::new);
    }
}
