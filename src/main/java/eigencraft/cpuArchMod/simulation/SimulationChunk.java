package eigencraft.cpuArchMod.simulation;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class SimulationChunk {
    private HashMap<BlockPos, SimulationAgent> simulationObjects = new HashMap<>();
    SimulationWorld simulation;

    public SimulationChunk(SimulationWorld simulation) {
        this.simulation = simulation;
    }

    public void addAgent(BlockPos pos, SimulationAgent simulationAgent){
        simulationObjects.put(pos, simulationAgent);

        simulation.getChunk(pos.north()).connectConnectable(simulationAgent,pos.north());
        simulation.getChunk(pos.south()).connectConnectable(simulationAgent,pos.south());
        simulation.getChunk(pos.east()).connectConnectable(simulationAgent,pos.east());
        simulation.getChunk(pos.west()).connectConnectable(simulationAgent,pos.west());
        simulation.getChunk(pos.up()).connectConnectable(simulationAgent,pos.up());
        simulation.getChunk(pos.down()).connectConnectable(simulationAgent,pos.down());
    }

    private void connectConnectable(SimulationAgent newSimulationAgent, BlockPos pos) {
        if (simulationObjects.containsKey(pos)){
            SimulationAgent oldSimulationAgent = simulationObjects.get(pos);
            oldSimulationAgent.connect(newSimulationAgent);
            newSimulationAgent.connect(oldSimulationAgent);
        }
    }

    public void removeConnectable(BlockPos pos){
        if (simulationObjects.containsKey(pos)){
            SimulationAgent toRemove = simulationObjects.get(pos);
            for (SimulationAgent thing:toRemove.getConnections()){
                thing.disconnect(toRemove);
            }
            simulationObjects.remove(pos);
        }
    }

    public SimulationAgent getSimulationObjectAt(BlockPos pos) {
        return simulationObjects.getOrDefault(pos,null);
    }

    public boolean shouldSave(){
        return simulationObjects.size()>0;
    }

    public HashMap<BlockPos, SimulationAgent> getSimulationObjects() {
        return simulationObjects;
    }
}
