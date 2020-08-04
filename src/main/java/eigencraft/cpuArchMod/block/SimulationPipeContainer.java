package eigencraft.cpuArchMod.block;

import net.minecraft.block.*;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Map;

public class SimulationPipeContainer extends ConnectingBlock implements CpuArchModBlock {

    private static final BooleanProperty UP;
    private static final BooleanProperty DOWN;
    private static final BooleanProperty NORTH;
    private static final BooleanProperty SOUTH;
    private static final BooleanProperty EAST;
    private static final BooleanProperty WEST;
    private static final Map<Direction, BooleanProperty> FACING_PROPERTIES;


    public SimulationPipeContainer() {
        super(0.2f,Settings.of(Material.STONE).breakInstantly().strength(1));
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
