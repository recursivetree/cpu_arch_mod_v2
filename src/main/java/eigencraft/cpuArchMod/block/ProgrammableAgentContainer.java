package eigencraft.cpuArchMod.block;

import eigencraft.cpuArchMod.CpuArchMod;
import eigencraft.cpuArchMod.simulation.SimulationAgent;
import eigencraft.cpuArchMod.simulation.SimulationWorld;
import eigencraft.cpuArchMod.simulation.SimulationWorldInterface;
import eigencraft.cpuArchMod.simulation.SimulationWorldRunnable;
import eigencraft.cpuArchMod.simulation.agents.ProgrammableAgent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ProgrammableAgentContainer extends Block implements CpuArchModBlock{
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public ProgrammableAgentContainer() {
        super(Settings.of(Material.STONE).breakInstantly().strength(1));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient()){
            ((SimulationWorldInterface)world).addSimulationWorldTask(new SimulationWorldRunnable() {
                @Override
                public void run(SimulationWorld world) {
                    world.addSimulationAgent(pos,new ProgrammableAgent());
                }
            });
        }
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (!world.isClient()){
            ((SimulationWorldInterface)world).addSimulationWorldTask(new SimulationWorldRunnable() {
                @Override
                public void run(SimulationWorld world) {
                    world.removeSimulationAgent(pos);
                }
            });
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient){
            ((SimulationWorldInterface) world).addSimulationWorldTask(new SimulationWorldRunnable() {
                @Override
                public void run(SimulationWorld world) {
                    SimulationAgent rawAgent = world.getSimulationAgent(pos);
                    if (rawAgent instanceof ProgrammableAgent){
                        ProgrammableAgent agent = (ProgrammableAgent)rawAgent;
                        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                        passedData.writeBlockPos(pos);
                        passedData.writeString(agent.getScriptFileName());
                        passedData.writeString(agent.getScriptSrc());
                        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, CpuArchMod.PROGRAMMABLE_AGENT_OPEN_GUI_S2C_PACKET,passedData);
                    }
                }
            });
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            boolean isPowered = world.isReceivingRedstonePower(pos);
            if (state.get(POWERED) != isPowered) {
                world.setBlockState(pos,state.with(POWERED,isPowered));
                if (isPowered) {
                    ((SimulationWorldInterface) world).addSimulationWorldTask(new SimulationWorldRunnable() {
                        @Override
                        public void run(SimulationWorld world) {
                            SimulationAgent rawAgent = world.getSimulationAgent(pos);
                            if (rawAgent instanceof ProgrammableAgent) {
                                ProgrammableAgent agent = (ProgrammableAgent) rawAgent;
                                agent.onRedstonePowered();
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.appendProperties(builder);
    }
}
