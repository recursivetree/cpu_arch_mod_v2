package eigencraft.cpuArchMod.simulation;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class SimulationChunk {
    private HashMap<BlockPos, SimulationAgent> simulationAgents = new HashMap<>();
    SimulationWorld simulation;

    public SimulationChunk(SimulationWorld simulation) {
        this.simulation = simulation;
    }

    public void addAgent(BlockPos pos, SimulationAgent simulationAgent){
        simulationAgents.put(pos, simulationAgent);

        simulation.getChunk(pos.north()).connectConnectable(simulationAgent,pos.north());
        simulation.getChunk(pos.south()).connectConnectable(simulationAgent,pos.south());
        simulation.getChunk(pos.east()).connectConnectable(simulationAgent,pos.east());
        simulation.getChunk(pos.west()).connectConnectable(simulationAgent,pos.west());
        simulation.getChunk(pos.up()).connectConnectable(simulationAgent,pos.up());
        simulation.getChunk(pos.down()).connectConnectable(simulationAgent,pos.down());
    }

    private void connectConnectable(SimulationAgent newSimulationAgent, BlockPos pos) {
        if (simulationAgents.containsKey(pos)){
            SimulationAgent oldSimulationAgent = simulationAgents.get(pos);
            oldSimulationAgent.connect(newSimulationAgent);
            newSimulationAgent.connect(oldSimulationAgent);
        }
    }

    public void removeConnectable(BlockPos pos){
        if (simulationAgents.containsKey(pos)){
            SimulationAgent toRemove = simulationAgents.get(pos);
            for (SimulationAgent agent:toRemove.getConnections()){
                agent.disconnect(toRemove);
            }
            simulationAgents.remove(pos);
        }
    }

    public SimulationAgent getSimulationObjectAt(BlockPos pos) {
        return simulationAgents.getOrDefault(pos,null);
    }

    public boolean shouldSave(){
        return simulationAgents.size()>0;
    }

    public HashMap<BlockPos, SimulationAgent> getSimulationAgents() {
        return simulationAgents;
    }

    public void tick(){
        for (SimulationAgent agent: simulationAgents.values()){
            agent.tick();
        }
    }
}
