package eigencraft.cpuArchMod.block;

import eigencraft.cpuArchMod.simulation.SimulationWorld;
import eigencraft.cpuArchMod.simulation.SimulationWorldInterface;
import eigencraft.cpuArchMod.simulation.SimulationWorldRunnable;
import eigencraft.cpuArchMod.simulation.agents.PipeAgent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.Map;

public class PipeContainer extends ConnectingBlock implements CpuArchModBlock {

    private static final BooleanProperty UP;
    private static final BooleanProperty DOWN;
    private static final BooleanProperty NORTH;
    private static final BooleanProperty SOUTH;
    private static final BooleanProperty EAST;
    private static final BooleanProperty WEST;
    private static final Map<Direction, BooleanProperty> FACING_PROPERTIES;


    public PipeContainer() {
        super(0.25f,Settings.of(Material.STONE).breakInstantly().strength(1));
        setDefaultState(getDefaultState()
                .with(UP, false)
                .with(DOWN,false)
                .with(EAST,false)
                .with(WEST,false)
                .with(NORTH,false)
                .with(SOUTH,false)
        );
    }

    private boolean shouldConnect(BlockState state){
        return state.getBlock() instanceof CpuArchModBlock;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient()){
            ((SimulationWorldInterface)world).addSimulationWorldTask(new SimulationWorldRunnable() {
                @Override
                public void run(SimulationWorld world) {
                    world.addSimulationAgent(pos,new PipeAgent());
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
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP);
        builder.add(DOWN);
        builder.add(NORTH);
        builder.add(SOUTH);
        builder.add(EAST);
        builder.add(WEST);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockView world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        return getDefaultState()
                .with(UP, shouldConnect(world.getBlockState(pos.up())))
                .with(DOWN,shouldConnect(world.getBlockState(pos.down())))
                .with(EAST,shouldConnect(world.getBlockState(pos.east())))
                .with(WEST,shouldConnect(world.getBlockState(pos.west())))
                .with(NORTH,shouldConnect(world.getBlockState(pos.north())))
                .with(SOUTH,shouldConnect(world.getBlockState(pos.south())));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        return state
                .with(UP, shouldConnect(world.getBlockState(pos.up())))
                .with(DOWN,shouldConnect(world.getBlockState(pos.down())))
                .with(EAST,shouldConnect(world.getBlockState(pos.east())))
                .with(WEST,shouldConnect(world.getBlockState(pos.west())))
                .with(NORTH,shouldConnect(world.getBlockState(pos.north())))
                .with(SOUTH,shouldConnect(world.getBlockState(pos.south())));
    }

    static {
        NORTH = ConnectingBlock.NORTH;
        EAST = ConnectingBlock.EAST;
        SOUTH = ConnectingBlock.SOUTH;
        WEST = ConnectingBlock.WEST;
        UP = ConnectingBlock.UP;
        DOWN = ConnectingBlock.DOWN;
        FACING_PROPERTIES = ConnectingBlock.FACING_PROPERTIES;
    }
}
