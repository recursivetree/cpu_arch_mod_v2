package eigencraft.cpuArchMod.simulation;

import net.minecraft.util.math.BlockPos;

import java.util.*;

public class SimulationChunk {
    SimulationWorld simulation;
    private final HashMap<BlockPos, DynamicAgent> dynamicAgents = new HashMap<>();
    private final List<PipeNetwork> pipeNetworkList = new ArrayList<>();
    private boolean persistent = false;

    public void makePersistent(){
        persistent = true;
    }

    public void makeUnloadable(){
        persistent = false;
    }

    public SimulationChunk(SimulationWorld simulation) {
        this.simulation = simulation;
    }

    public void addAgent(BlockPos pos, DynamicAgent dynamicAgent) {
        dynamicAgents.put(pos, dynamicAgent);
        System.out.println("node added");
        connectAgent(pos);
    }

    public void addPipe(BlockPos pos) {
        Set<PipeNetwork> networks = getNeighborNetworks(pos);

        if (networks.size()==0){
            PipeNetwork newNetwork = new PipeNetwork();
            pipeNetworkList.add(newNetwork);
            newNetwork.addMember(pos);
        } else {
            PipeNetwork root = new PipeNetwork();
            pipeNetworkList.add(root);
            for(PipeNetwork network:networks){
                pipeNetworkList.remove(network);
                root.merge(network);
            }
            root.addMember(pos);
        }

        connectAgent(pos.up());
        connectAgent(pos.down());
        connectAgent(pos.west());
        connectAgent(pos.east());
        connectAgent(pos.north());
        connectAgent(pos.south());
    }

    private void connectAgent(BlockPos pos){
        DynamicAgent agent = getDynamicAgentAt(pos);
        System.out.println("call to connect");
        if (agent!=null) {
            for (PipeNetwork network : getNeighborNetworks(pos)) {
                network.addDynamicAgent(agent);
                agent.connect(network);
                System.out.println("connect");
            }
        }
    }

    public void removeAgent(BlockPos pos) {
        if (dynamicAgents.containsKey(pos)){
            getDynamicAgentAt(pos).removeFromWorld();
        } else {
            //TODO pipe splitting algorithm
        }
    }

    private Set<PipeNetwork> getNeighborNetworks(BlockPos pos){
        SimulationChunk chunk = simulation.getChunk(pos);
        Set<PipeNetwork> networks = new HashSet<>();
        networks.add(chunk.getSimulationNetworkAt(pos));
        networks.add(chunk.getSimulationNetworkAt(pos.up()));
        networks.add(chunk.getSimulationNetworkAt(pos.down()));
        networks.add(chunk.getSimulationNetworkAt(pos.west()));
        networks.add(chunk.getSimulationNetworkAt(pos.east()));
        networks.add(chunk.getSimulationNetworkAt(pos.north()));
        networks.add(chunk.getSimulationNetworkAt(pos.south()));
        //noinspection StatementWithEmptyBody
        while (networks.remove(null));
        return networks;
    }

    private PipeNetwork getSimulationNetworkAt(BlockPos pos){
        for(PipeNetwork network:pipeNetworkList){
            if (network.containsBlock(pos)){
                return network;
            }
        }
        return null;
    }

    public DynamicAgent getDynamicAgentAt(BlockPos pos) {
        return dynamicAgents.getOrDefault(pos, null);
    }

    public boolean shouldSave() {
        return dynamicAgents.size() > 0;
    }

    public HashMap<BlockPos, DynamicAgent> getDynamicAgents() {
        return dynamicAgents;
    }

    public void tick() {
        for (DynamicAgent agent : dynamicAgents.values()) {
            //Use special tick for cleaner sub class implementation of SimulationAgent
            agent.tick();
        }
    }

    public boolean isPersistent() {
        return persistent;
    }
}
