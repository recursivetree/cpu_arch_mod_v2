package eigencraft.cpuArchMod.simulation;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public abstract class SimulationAgent {
    static HashMap<String, Supplier<SimulationAgent>> constructors = new HashMap<>();
    private final ArrayList<SimulationAgent> connectedAgents = new ArrayList<>();
    private boolean blocked = false;

    public static void register(String name, Supplier<SimulationAgent> constructor) {
        constructors.put(name, constructor);
    }

    public static SimulationAgent getSimulationObject(String name) {
        return constructors.get(name).get();
    }

    public void connect(SimulationAgent other) {
        connectedAgents.add(other);
    }

    public void disconnect(SimulationAgent other) {
        connectedAgents.remove(other);
    }

    public ArrayList<SimulationAgent> getConnections() {
        return connectedAgents;
    }

    public void publish(SimulationMessage message) {
        if (!blocked) {
            lock();
            for (SimulationAgent agent : connectedAgents) {
                agent.process(message);
            }
            unlock();
        }
    }

    public void unlock() {
        if (blocked) {
            blocked = false;
            for (SimulationAgent agent : connectedAgents) {
                agent.unlock();
            }
        }
    }

    public void lock() {
        blocked = true;
    }

    public boolean isLocked() {
        return blocked;
    }

    public abstract void tick();

    public abstract void process(SimulationMessage message);

    public abstract JsonElement getConfigData();

    public abstract void loadConfig(JsonElement config);

}
