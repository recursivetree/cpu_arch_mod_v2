package eigencraft.cpuArchMod.block;

import eigencraft.cpuArchMod.simulation.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ProgrammableNodeContainer extends Block implements CpuArchModBlock{
    public ProgrammableNodeContainer() {
        super(Settings.of(Material.STONE).breakInstantly().strength(1));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient()){
            ((SimulationWorldProvider)world).addSimulationWorldTask(new SimulationWorldRunnable() {
                @Override
                public void run(SimulationWorld world) {
                    world.addSimulationObject(pos,new ProgrammableNode());
                }
            });
        }
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (!world.isClient()){
            ((SimulationWorldProvider)world).addSimulationWorldTask(new SimulationWorldRunnable() {
                @Override
                public void run(SimulationWorld world) {
                    world.removeSimulationObject(pos);
                }
            });
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()){
            ((SimulationWorldProvider)world).addSimulationWorldTask(new SimulationWorldRunnable() {
                @Override
                public void run(SimulationWorld world) {
                    world.getSimulationObjectAt(pos).process(new SimulationMessage());
                }
            });
        }
        return ActionResult.SUCCESS;
    }
}