package eigencraft.cpuArchMod.simulation;

import com.google.gson.JsonElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

public interface SimulationAgent {
    public void connect(SimulationAgent other);
    public void disconnect(SimulationAgent other);
    public Collection<SimulationAgent> getConnections();
    public void tick();
    public void process(SimulationMessage message);
    public JsonElement getConfigData();
    public void loadConfig(JsonElement config);

    static HashMap<String, Supplier<SimulationAgent>> constructors = new HashMap<>();
    public static void register(String name,Supplier<SimulationAgent> constructor){
        constructors.put(name,constructor);
    }
    public static SimulationAgent getSimulationObject(String name){
        return constructors.get(name).get();
    }

}
