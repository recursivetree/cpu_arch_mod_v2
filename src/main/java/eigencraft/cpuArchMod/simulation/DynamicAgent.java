package eigencraft.cpuArchMod.simulation;

import com.google.gson.JsonElement;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.BiFunction;


public abstract class DynamicAgent {
    private static HashMap<String, BiFunction<SimulationWorld,BlockPos,DynamicAgent>> constructors = new HashMap<>();


    private final ArrayList<PipeNetwork> pipeNetworks = new ArrayList<>(1);
    private final LinkedList<SimulationMessage> messages = new LinkedList<>();

    public static void register(String name, BiFunction<SimulationWorld,BlockPos,DynamicAgent> constructor) {
        constructors.put(name, constructor);
    }
    protected SimulationWorld world;
    protected BlockPos pos;

    public DynamicAgent(SimulationWorld world, BlockPos pos){
        this.world = world;
        this.pos = pos;
    }

    public static DynamicAgent createDynamicAgent(String name, SimulationWorld world,BlockPos pos) {
        return constructors.get(name).apply(world,pos);
    }

    public void connect(PipeNetwork network) {
        pipeNetworks.add(network);
    }

    public void disconnect(PipeNetwork other) {
        pipeNetworks.remove(other);
    }

    public void removeFromWorld(){
        for (PipeNetwork network:pipeNetworks){
            network.removeDynamicAgent(this);
        }
    }

    public ArrayList<PipeNetwork> getConnections() {
        return pipeNetworks;
    }

    public void publish(SimulationMessage message) {
        for (PipeNetwork network: pipeNetworks){
            network.publish(message,this);
        }
    }

    public void receive(SimulationMessage message){
        messages.add(message);
    }

    public void tick(){
        while (!messages.isEmpty()){
            process(messages.remove());
        }
    }

    public abstract void process(SimulationMessage message);

    public abstract JsonElement getConfigData();

    public abstract void loadConfig(JsonElement config);

}
