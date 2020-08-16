package eigencraft.cpuArchMod.simulation.agents;

import com.google.gson.JsonElement;
import eigencraft.cpuArchMod.simulation.DynamicAgent;
import eigencraft.cpuArchMod.simulation.SimulationMessage;

public class PipeAgent extends DynamicAgent {

    @Override
    public void tick() {

    }

    @Override
    public void process(SimulationMessage message) {
//        if (!isLocked()) {
//            lock();
//            for (DynamicAgent agent : getConnections()) {
//                agent.process(message);
//            }
//        }
    }

    @Override
    public JsonElement getConfigData() {
        return null;
    }

    @Override
    public void loadConfig(JsonElement config) {

    }

}
