package eigencraft.cpuArchMod.simulation;

import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.Collections;

public class DummySimulationAgent implements SimulationAgent {
    @Override
    public void connect(SimulationAgent other) {

    }

    @Override
    public void disconnect(SimulationAgent other) {

    }

    @Override
    public Collection<SimulationAgent> getConnections() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void tick() {

    }

    @Override
    public void process(SimulationMessage message) {

    }

    @Override
    public JsonElement getConfigData() {
        return null;
    }

    static {
        SimulationAgent.register(DummySimulationAgent.class.getSimpleName(), DummySimulationAgent::new);
    }
}
