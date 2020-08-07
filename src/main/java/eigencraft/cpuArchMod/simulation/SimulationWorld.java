package eigencraft.cpuArchMod.simulation;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.io.File;
import java.io.IOError;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SimulationWorld implements Runnable{
    private Thread simulationThread;
    private ServerWorld world;
    private File saveDirectory;
    private final HashMap<ChunkPos,SimulationChunk> loadedChunks = new HashMap();
    private volatile LinkedList<SimulationWorldRunnable> tasks = new LinkedList<>();
    private boolean running = true;

    public SimulationWorld(File cpu_sim_directory, ServerWorld serverWorld) {
        simulationThread = new Thread(this);
        world = serverWorld;
        saveDirectory = cpu_sim_directory;
        saveDirectory.mkdirs();
        simulationThread.start();
    }

    public SimulationChunk getChunk(ChunkPos chunkPos){
        if (!loadedChunks.containsKey(chunkPos)){
            SimulationChunk newChunk = new SimulationChunk(this);
            loadedChunks.put(chunkPos,newChunk);
            SimulationChunkStorage.loadChunk(chunkPos,newChunk,saveDirectory);
        }
        return loadedChunks.get(chunkPos);
    }

    public SimulationChunk getChunk(BlockPos pos){
        return getChunk(new ChunkPos(pos));
    }

    public void addSimulationAgent(BlockPos pos, SimulationAgent simulationAgent){
        getChunk(pos).addAgent(pos, simulationAgent);
    }

    public void removeSimulationAgent(BlockPos pos){
        getChunk(pos).removeConnectable(pos);
    }

    @Override
    public void run() {
        long lastSave = System.currentTimeMillis();
        while (running) {
            while (!tasks.isEmpty()) {
                synchronized (tasks) {
                    tasks.remove().run(this);
                }
                for (SimulationChunk chunk:loadedChunks.values()){
                    chunk.tick();
                }
                if (System.currentTimeMillis()-lastSave>60000){
                    saveWorld();
                }
            }
        }
        saveWorld();
    }

    public void saveWorld(){
        for (Map.Entry<ChunkPos, SimulationChunk> chunk:loadedChunks.entrySet()){
            if (chunk.getValue().shouldSave()){
                SimulationChunkStorage.saveChunk(chunk.getKey(),chunk.getValue(),saveDirectory);
            } else {
                try {
                    SimulationChunkStorage.getChunkSavePath(chunk.getKey(), saveDirectory).delete();
                } catch (IOError e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void addTask(SimulationWorldRunnable simulationWorldRunnable) {
        synchronized (tasks) {
            tasks.add(simulationWorldRunnable);
        }
    }

    public SimulationAgent getSimulationAgent(BlockPos pos) {
        return getChunk(pos).getSimulationObjectAt(pos);
    }

    public void stop() {
        running = false;
        simulationThread.interrupt();
    }
}
