package eigencraft.cpuArchMod.simulation;

import eigencraft.cpuArchMod.CpuArchMod;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class SimulationWorld implements Runnable {
    private static Logger LOGGER = LogManager.getLogger(CpuArchMod.MOD_ID);
    private final Thread simulationThread;
    private final File saveDirectory;
    private final LinkedList<SimulationWorldRunnable> tasks = new LinkedList<>();
    private boolean running = true;

    public SimulationWorld(File cpu_sim_directory, ServerWorld serverWorld) {
        simulationThread = new Thread(this);
        saveDirectory = cpu_sim_directory;
        saveDirectory.mkdirs();
        simulationThread.start();
    }

    public void persistentLoadChunk(ChunkPos pos){
        //TODO ensure pipeNetworks in chunk are loaded
    }

    @Override
    public void run() {
        SimulationWorldStorage.loadWorld(this,saveDirectory);
        long lastSave = System.currentTimeMillis();
        while (running) {
            while (!tasks.isEmpty()) {
                synchronized (tasks) {
                    tasks.remove().run(this);
                }
            }
            for (DynamicAgent agent : dynamicAgents.values()) {
                agent.tick();
            }
            if (System.currentTimeMillis() - lastSave > 60000) {
                saveWorld();
                lastSave = System.currentTimeMillis();
            }
        }
        saveWorld();
    }

    public void saveWorld() {
        //TODO saving
        SimulationWorldStorage.saveWorld(this,saveDirectory);
        LOGGER.info("saved simulation world");
    }

    public void addTask(SimulationWorldRunnable simulationWorldRunnable) {
        synchronized (tasks) {
            tasks.add(simulationWorldRunnable);
        }
    }

    public void stop() {
        running = false;
        simulationThread.interrupt();
    }

    public void markChunkUnloadable(ChunkPos pos) {
        //TODO unload chunk if no longer required.
    }

    private final HashMap<BlockPos, DynamicAgent> dynamicAgents = new HashMap<>();
    private final List<PipeNetwork> pipeNetworkList = new ArrayList<>();

    public void addDynamicAgent(BlockPos pos, DynamicAgent dynamicAgent) {
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
        DynamicAgent agent = getDynamicAgent(pos);
        System.out.println("call to connect");
        if (agent!=null) {
            for (PipeNetwork network : getNeighborNetworks(pos)) {
                network.addDynamicAgent(agent);
                agent.connect(network);
                System.out.println("connect");
            }
        }
    }

    public void removeSimulationObject(BlockPos pos) {
        if (dynamicAgents.containsKey(pos)){
            getDynamicAgent(pos).removeFromWorld();
        } else {
            //TODO efficient pipe splitting algorithm
            PipeNetwork toSplit = getSimulationNetworkAt(pos);
            pipeNetworkList.remove(toSplit);
            if (toSplit != null) {
                for(DynamicAgent agent:toSplit.getAgents()){
                    agent.disconnect(toSplit);
                }
                Set<BlockPos> members = toSplit.getMembers();
                members.remove(pos);
                for(BlockPos memberPos:members){
                    addPipe(memberPos);
                }
            }
        }
    }

    private Set<PipeNetwork> getNeighborNetworks(BlockPos pos){
        Set<PipeNetwork> networks = new HashSet<>();
        networks.add(getSimulationNetworkAt(pos));
        networks.add(getSimulationNetworkAt(pos.up()));
        networks.add(getSimulationNetworkAt(pos.down()));
        networks.add(getSimulationNetworkAt(pos.west()));
        networks.add(getSimulationNetworkAt(pos.east()));
        networks.add(getSimulationNetworkAt(pos.north()));
        networks.add(getSimulationNetworkAt(pos.south()));
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

    public DynamicAgent getDynamicAgent(BlockPos pos) {
        return dynamicAgents.getOrDefault(pos, null);
    }

    public HashMap<BlockPos, DynamicAgent> getDynamicAgents() {
        return dynamicAgents;
    }

    public List<PipeNetwork> getPipeNetworks() {
        return pipeNetworkList;
    }
}
