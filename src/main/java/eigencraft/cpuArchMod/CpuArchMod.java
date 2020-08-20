package eigencraft.cpuArchMod;


import eigencraft.cpuArchMod.block.PipeContainer;
import eigencraft.cpuArchMod.block.ProgrammableAgentBlockEntity;
import eigencraft.cpuArchMod.block.ProgrammableAgentContainerBlock;
import eigencraft.cpuArchMod.networking.ProgrammableAgentConfigurationPacket;
import eigencraft.cpuArchMod.script.ServerScriptManager;
import eigencraft.cpuArchMod.simulation.DynamicAgent;
import eigencraft.cpuArchMod.simulation.SimulationWorldInterface;
import eigencraft.cpuArchMod.simulation.WorldRunnable;
import eigencraft.cpuArchMod.simulation.agents.ProgrammableAgent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class CpuArchMod implements ModInitializer {
    public static final String MOD_ID = "cpu_arch_mod";

    Item PIPE_ITEM;
    Item NODE_ITEM;

    //TODO add better item image
    public static final ItemGroup CPU_ARCH_MOD_ITEMGROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "items"),
            () -> ItemStack.EMPTY);

    public static final Block PIPE = new PipeContainer();
    public static final Block PROGRAMMABLE_NODE = new ProgrammableAgentContainerBlock();

    public static final Identifier PROGRAMMABLE_AGENT_OPEN_GUI_S2C_PACKET = new Identifier(MOD_ID, "prg_agent_open_s2c");
    public static final Identifier PROGRAMMABLE_AGENT_SAFE_CONFIG_C2S_PACKET = new Identifier(MOD_ID, "prg_agent_safe_c2s");
    public static final Identifier SCRIPT_MANAGER_SYNC_S2C = new Identifier(MOD_ID,"script_managers_sync_s2c");


    public static final Configuration CONFIGURATION = new Configuration();

    public static BlockEntityType<ProgrammableAgentBlockEntity> PROGRAMMABLE_AGENT_BLOCK_ENTITY;

    public static ServerScriptManager SCRIPT_MANAGER;


    @Override
    public void onInitialize() {
        //Blocks
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "pipe"), PIPE);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "node"), PROGRAMMABLE_NODE);

        //Items
        NODE_ITEM = new BlockItem(PROGRAMMABLE_NODE, new Item.Settings().group(CPU_ARCH_MOD_ITEMGROUP));
        PIPE_ITEM = new BlockItem(PIPE, new Item.Settings().group(CPU_ARCH_MOD_ITEMGROUP));
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "pipe"), PIPE_ITEM);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "node"), NODE_ITEM);

        //Block entities
        PROGRAMMABLE_AGENT_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "cpu_arch_mod:node_block_entity", BlockEntityType.Builder.create(ProgrammableAgentBlockEntity::new, PROGRAMMABLE_NODE).build(null));

        //Shutdown callback
        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            for (ServerWorld world : minecraftServer.getWorlds()) {
                ((SimulationWorldInterface) world).stopSimulation();
            }
        });

        //Setup server script manager
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> SCRIPT_MANAGER = new ServerScriptManager(new File(minecraftServer.getSavePath(WorldSavePath.ROOT).toFile(),"cpu_arch_mod_scripts")));

        //
        ServerTickEvents.START_WORLD_TICK.register(serverWorld -> {
            Queue<WorldRunnable> worldTasks = ((SimulationWorldInterface)serverWorld).getMainGameThreadTasks();
            synchronized (worldTasks){
                while (!worldTasks.isEmpty()){
                    worldTasks.remove().run(serverWorld);
                }
            }
        });

        //Register simulation agents
        DynamicAgent.register(ProgrammableAgent.class.getSimpleName(), ProgrammableAgent::new);

        //Networking
        ServerSidePacketRegistry.INSTANCE.register(PROGRAMMABLE_AGENT_SAFE_CONFIG_C2S_PACKET, (packetContext, packetByteBuf) -> {
            ProgrammableAgentConfigurationPacket packet = ProgrammableAgentConfigurationPacket.readPacket(packetByteBuf);

            if (packet.hasScriptSrc()) {
                SCRIPT_MANAGER.store(packet.getScriptFileName(), packet.getScriptSrc());
                List<ServerPlayerEntity> players =  PlayerStream.all(packetContext.getPlayer().getServer()).filter(c -> c == packetContext.getPlayer()).collect(Collectors.toList());
                SCRIPT_MANAGER.syncScriptsToServer(players);
                ServerWorld world = (ServerWorld) packetContext.getPlayer().getEntityWorld();
                ((SimulationWorldInterface) world).addSimulationWorldTask(world1 -> {
                    DynamicAgent simAgent = world1.getDynamicAgent(packet.getPos());
                    if (simAgent instanceof ProgrammableAgent) {
                        ProgrammableAgent agent = (ProgrammableAgent) simAgent;
                        agent.setScriptFileName(packet.getScriptFileName());
                        agent.setScriptSrc(packet.getScriptSrc());
                    }
                });
            }
            packetContext.getTaskQueue().execute(() -> {
                BlockEntity blockEntity = packetContext.getPlayer().getEntityWorld().getBlockEntity(packet.getPos());
                if (blockEntity != null) {
                    if (blockEntity instanceof ProgrammableAgentBlockEntity) {
                        ((ProgrammableAgentBlockEntity) blockEntity).setAppearance(packet.getDisplayName(),packet.getDisplayColor());
                    }
                }
            });
        });

        //End of setup
    }
}


