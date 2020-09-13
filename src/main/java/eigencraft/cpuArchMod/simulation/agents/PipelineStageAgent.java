package eigencraft.cpuArchMod.simulation.agents;

import com.google.gson.JsonElement;
import eigencraft.cpuArchMod.simulation.DynamicAgent;
import eigencraft.cpuArchMod.simulation.SimulationMessage;
import eigencraft.cpuArchMod.simulation.SimulationWorld;
import net.minecraft.util.math.BlockPos;

public class PipelineStageAgent extends DynamicAgent {
    SimulationMessage lastDataInput;
    public PipelineStageAgent(SimulationWorld world, BlockPos pos) {
        super(world, pos);
    }

    @Override
    public void process(SimulationMessage message) {
        lastDataInput = message;
    }

    @Override
    public JsonElement getConfigData() {
        return null;
    }

    @Override
    public void loadConfig(JsonElement config) {

    }
}
