package eigencraft.cpuArchMod.simulation;

import eigencraft.cpuArchMod.CpuArchMod;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class SimulationWorld implements Runnable {
    //Logger for logging
    private static Logger LOGGER = LogManager.getLogger(CpuArchMod.MOD_ID);
    //The simulation is in a separate thread
    private final Thread simulationThread;
    //The directory where the world files are stored (different for every dimension)
    private final File saveDirectory;
    //Because it's in a separate thread, to avoid problems use this task queue
    private final LinkedList<SimulationWorldRunnable> tasks = new LinkedList<>();
    //Because it's in a separate thread, to avoid problems use this task queue
    private final LinkedList<WorldRunnable> mainGameThreadTasks = new LinkedList<WorldRunnable>();
    //Used to stop the simulation on server shutdown
    private boolean running = true;
    //All dynamicAgents and their position
    private final HashMap<BlockPos, DynamicAgent> dynamicAgents = new HashMap<>();
    //All pipe Networks
    private final List<PipeNetwork> pipeNetworkList = new ArrayList<>();

    public SimulationWorld(File cpu_sim_directory, ServerWorld serverWorld, String level_name) {
        //Create Thread
        simulationThread = new Thread(this,String.format("cpu simulation: %s",level_name));
        //Store the saves directory
        saveDirectory = cpu_sim_directory;
        //If not existent, create the directory
        saveDirectory.mkdirs();
        //Launch simulation thread
        simulationThread.start();
    }

    @Override
    public void run() {
        //TODO portioned world loading
        //Load the world
        SimulationWorldStorage.loadWorld(this,saveDirectory);
        //autosave of world every minute
        long lastSave = System.currentTimeMillis();

        //main loop
        while (running) {

            //Execute task queue
            while (!tasks.isEmpty()) {
                //Without synchronisation, there were some issues.
                synchronized (tasks) {
                    tasks.remove().run(this);
                }
            }
            //tick all dynamic agents
            for (DynamicAgent agent : dynamicAgents.values()) {
                agent.tick();
            }
            //autosave
            if (System.currentTimeMillis() - lastSave > 60000) {
                saveWorld();
                lastSave = System.currentTimeMillis();
            }
        }
        //Save the world on shutdown
        saveWorld();
    }

    public void saveWorld() {
        //TODO portioned world saving
        SimulationWorldStorage.saveWorld(this,saveDirectory);
        LOGGER.info("saved simulation world");
    }

    public void addTask(SimulationWorldRunnable simulationWorldRunnable) {
        //Adds a tasks to the task queue
        synchronized (tasks) {
            tasks.add(simulationWorldRunnable);
        }
    }

    public void stop() {
        //Shutdown the simulation thread
        running = false;
        simulationThread.interrupt();
    }

    public void addDynamicAgent(BlockPos pos, DynamicAgent dynamicAgent) {
        //Add the agent to the agent hashmap
        dynamicAgents.put(pos, dynamicAgent);
        //Connect the agent to pipes nearby.
        connectAgent(pos);
    }

    public void addPipe(BlockPos pos) {
        //get all existing pipe networks nearby
        Set<PipeNetwork> networks = getNeighborNetworks(pos);

        //No networks nearby
        if (networks.size()==0){
            //Create new network
            PipeNetwork newNetwork = new PipeNetwork();
            //Store network in network list
            pipeNetworkList.add(newNetwork);
            //Add the new pipe
            newNetwork.addMember(pos);
        }

        //Add to existing network
        else {
            pipeNetworkList.removeAll(networks);
            PipeNetwork currentNetwork = null;
            for (PipeNetwork network:networks){
                network.merge(currentNetwork);
                currentNetwork = network;
            }
            currentNetwork.addMember(pos);
            pipeNetworkList.add(currentNetwork);
        }

        //connect agents nearby
        connectAgent(pos.up());
        connectAgent(pos.down());
        connectAgent(pos.west());
        connectAgent(pos.east());
        connectAgent(pos.north());
        connectAgent(pos.south());
    }

    private void connectAgent(BlockPos pos){
        //Is there even an agent at the given pos?
        DynamicAgent agent = getDynamicAgent(pos);
        if (agent!=null) {
            //Connect agent to all neighboring networks
            for (PipeNetwork network : getNeighborNetworks(pos)) {
                network.addDynamicAgent(agent);
                agent.connect(network);
            }
        }
    }

    public void removeSimulationObject(BlockPos pos) {
        //If it is an agent
        if (dynamicAgents.containsKey(pos)){
            //The agent can remove itself
            getDynamicAgent(pos).removeFromWorld();
        }
        //If it is a pipe, it's more complex
        else {
            //TODO efficient pipe splitting algorithm

            //First, get the network to split and check if it exists
            PipeNetwork toSplit = getSimulationNetworkAt(pos);
            if (toSplit != null) {
                //Remove it, since there will be multiple new ones
                pipeNetworkList.remove(toSplit);

                //Disconnect all connected agents from the old network
                for(DynamicAgent agent:toSplit.getAgents()){
                    agent.disconnect(toSplit);
                }
                //Get all pipes included in the network
                Set<BlockPos> members = toSplit.getMembers();
                //Remove the pos which should be removed
                members.remove(pos);
                //add all positions to the world
                for(BlockPos memberPos:members){
                    addPipe(memberPos);
                }
            }
        }
    }

    private Set<PipeNetwork> getNeighborNetworks(BlockPos pos){
        Set<PipeNetwork> networks = new HashSet<>();
        //Use optimised method (takes a collection instead of returning single, null containing values)
        getPipeNetworkWithCollection(pos.up(),networks);
        getPipeNetworkWithCollection(pos.down(),networks);
        getPipeNetworkWithCollection(pos.west(),networks);
        getPipeNetworkWithCollection(pos.south(),networks);
        getPipeNetworkWithCollection(pos.north(),networks);
        getPipeNetworkWithCollection(pos.east(),networks);
        return networks;
    }

    private void getPipeNetworkWithCollection(BlockPos pos, Collection<PipeNetwork> collection){
        //Stores the result in a collection instead of directly returning it
        for(PipeNetwork network:pipeNetworkList){
            if (network.containsBlock(pos)){
                collection.add(network);
            }
        }
    }

    private PipeNetwork getSimulationNetworkAt(BlockPos pos){
        //Returns the simulation network at a pos
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

    public void addMainGameTask(WorldRunnable worldRunnable) {
        synchronized (mainGameThreadTasks){
            mainGameThreadTasks.add(worldRunnable);
        }
    }

    public LinkedList<WorldRunnable> getMainGameThreadTasks() {
        return mainGameThreadTasks;
    }
}
