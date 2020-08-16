package eigencraft.cpuArchMod.simulation;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PipeNetwork {
    private final Set<BlockPos> members = new HashSet<>();
    private final Set<DynamicAgent> dynamicAgents = new HashSet<>();
    public void addMember(BlockPos pos){
        members.add(pos);
    }

    public boolean containsBlock(BlockPos pos){
        return members.contains(pos);
    }

    public void publish(SimulationMessage message,DynamicAgent src) {
        for (DynamicAgent agent:dynamicAgents){
            if (agent!=src) {
                agent.receive(message);
            }
        }
    }

    public void merge(PipeNetwork other) {
        for (DynamicAgent agent: other.dynamicAgents){
            agent.disconnect(other);
            agent.connect(this);
        }
        members.addAll(other.members);
        dynamicAgents.addAll(other.dynamicAgents);
    }

    public void addDynamicAgent(DynamicAgent agent) {
        dynamicAgents.add(agent);
    }

    public void removeDynamicAgent(DynamicAgent dynamicAgent) {
        dynamicAgents.remove(dynamicAgent);
    }
}
