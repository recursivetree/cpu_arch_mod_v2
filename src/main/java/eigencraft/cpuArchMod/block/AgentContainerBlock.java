package eigencraft.cpuArchMod.block;

import eigencraft.cpuArchMod.simulation.DynamicAgent;
import eigencraft.cpuArchMod.simulation.SimulationWorld;
import eigencraft.cpuArchMod.simulation.SimulationWorldInterface;
import eigencraft.cpuArchMod.simulation.agents.ProgrammableAgent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.function.BiFunction;

public class AgentContainerBlock extends Block implements CpuArchModBlock{

    private final BiFunction<SimulationWorld, BlockPos, DynamicAgent> constructor;

    public AgentContainerBlock(BiFunction<SimulationWorld, BlockPos, DynamicAgent> constructor) {
        super(Settings.of(Material.STONE).breakInstantly().strength(1));
        this.constructor = constructor;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient()) {
            ((SimulationWorldInterface) world).addSimulationWorldTask(world1 -> world1.addDynamicAgent(pos, new ProgrammableAgent(world1,pos)));
        }
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (!world.isClient()) {
            ((SimulationWorldInterface) world).addSimulationWorldTask(world1 -> world1.removeSimulationObject(pos));
        }
    }
}
