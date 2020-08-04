package eigencraft.cpuArchMod.simulation.agents;

import com.google.gson.JsonElement;
import eigencraft.cpuArchMod.simulation.SimulationAgent;
import eigencraft.cpuArchMod.simulation.SimulationMessage;

import java.util.*;

public class ProgrammableAgent implements SimulationAgent {
    private final ArrayList<SimulationAgent> connectedObjects =   new ArrayList<>();
    private Set<SimulationMessage> messages = new HashSet<>();

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
        while (!messages.isEmpty()){
            System.out.println("yeah");
        }
    }

    @Override
    public void process(SimulationMessage message) {
        messages.add(message);
    }

    @Override
    public JsonElement getConfigData() {
        return null;
    }

    @Override
    public void loadConfig(JsonElement config) {

    }

    static {
        SimulationAgent.register(ProgrammableAgent.class.getSimpleName(), ProgrammableAgent::new);
    }
}
