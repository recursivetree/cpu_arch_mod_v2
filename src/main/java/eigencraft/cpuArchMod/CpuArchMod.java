package eigencraft.cpuArchMod;


import eigencraft.cpuArchMod.block.PipeContainer;
import eigencraft.cpuArchMod.block.ProgrammableAgentBlockEntity;
import eigencraft.cpuArchMod.block.ProgrammableAgentContainerBlock;
import eigencraft.cpuArchMod.networking.CpuArchModPackets;
import eigencraft.cpuArchMod.networking.PrgAgentConfigurationC2SPacket;
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
import java.util.Queue;

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
        ServerSidePacketRegistry.INSTANCE.register(CpuArchModPackets.PROGRAMMABLE_AGENT_CONFIG_C2S_PACKET, (packetContext, packetByteBuf) -> {
            PrgAgentConfigurationC2SPacket packet = PrgAgentConfigurationC2SPacket.readPacket(packetByteBuf);

            if (packet.hasScriptSrc()) {
                SCRIPT_MANAGER.store(packet.getScriptFileName(), packet.getScriptSrc());

                SCRIPT_MANAGER.syncScriptsToServer((ServerPlayerEntity) packetContext.getPlayer(),packetContext.getPlayer().getServer());

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


