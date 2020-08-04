package eigencraft.cpuArchMod.simulation;

import com.google.gson.JsonElement;

import java.util.ArrayList;

public class ProgrammableNode implements SimulationAgent {
    private final ArrayList<SimulationAgent> connectedObjects =   new ArrayList<>();

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
        System.out.println("yeah");
        for (SimulationAgent o:connectedObjects){
            o.process(message);
        }
    }

    @Override
    public JsonElement getConfigData() {
        return null;
    }

    static {
        SimulationAgent.register(ProgrammableNode.class.getSimpleName(),ProgrammableNode::new);
    }
}
