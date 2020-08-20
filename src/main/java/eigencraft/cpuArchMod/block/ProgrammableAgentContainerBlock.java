package eigencraft.cpuArchMod.block;

import eigencraft.cpuArchMod.CpuArchMod;
import eigencraft.cpuArchMod.simulation.DynamicAgent;
import eigencraft.cpuArchMod.simulation.SimulationWorldInterface;
import eigencraft.cpuArchMod.simulation.agents.ProgrammableAgent;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ProgrammableAgentContainerBlock extends Block implements CpuArchModBlock, BlockEntityProvider {
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public ProgrammableAgentContainerBlock() {
        super(Settings.of(Material.STONE).breakInstantly().strength(1));
        setDefaultState(getStateManager().getDefaultState().with(POWERED, false));
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

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            //Open configuration gui
            ((SimulationWorldInterface) world).addSimulationWorldTask(world1 -> {
                DynamicAgent rawAgent = world1.getDynamicAgent(pos);
                if (rawAgent instanceof ProgrammableAgent) {
                    ProgrammableAgent agent = (ProgrammableAgent) rawAgent;
                    PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                    //The position
                    passedData.writeBlockPos(pos);
                    //Filename
                    passedData.writeString(agent.getScriptFileName());
                    //script src
                    passedData.writeString(agent.getScriptSrc());
                    //error log, if "", the client won't show the log
                    passedData.writeString(agent.getErrorLog());
                    //Reset error log
                    agent.resetErrorLog();
                    //Send package
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, CpuArchMod.PROGRAMMABLE_AGENT_OPEN_GUI_S2C_PACKET, passedData);
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
                world.setBlockState(pos, state.with(POWERED, isPowered));
                if (isPowered) {
                    ((SimulationWorldInterface) world).addSimulationWorldTask(world1 -> {
                        DynamicAgent rawAgent = world1.getDynamicAgent(pos);
                        if (rawAgent instanceof ProgrammableAgent) {
                            ProgrammableAgent agent = (ProgrammableAgent) rawAgent;
                            agent.onRedstonePowered();
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

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new ProgrammableAgentBlockEntity();
    }
}
