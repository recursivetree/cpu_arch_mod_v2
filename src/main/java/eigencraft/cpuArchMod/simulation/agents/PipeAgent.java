package eigencraft.cpuArchMod.simulation.agents;

import com.google.gson.JsonElement;
import eigencraft.cpuArchMod.simulation.SimulationAgent;
import eigencraft.cpuArchMod.simulation.SimulationMessage;

import java.util.ArrayList;

public class PipeAgent implements SimulationAgent {
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

    @Override
    public void loadConfig(JsonElement config) {

    }

    static {
        SimulationAgent.register(PipeAgent.class.getSimpleName(), PipeAgent::new);
    }
}
