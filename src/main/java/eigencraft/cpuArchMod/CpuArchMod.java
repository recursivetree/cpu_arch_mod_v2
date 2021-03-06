package eigencraft.cpuArchMod;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eigencraft.cpuArchMod.block.PipeContainer;
import eigencraft.cpuArchMod.block.ProgrammableAgentBlockEntity;
import eigencraft.cpuArchMod.block.ProgrammableAgentContainerBlock;
import eigencraft.cpuArchMod.networking.CpuArchModPackets;
import eigencraft.cpuArchMod.networking.PrgAgentConfigurationC2SPacket;
import eigencraft.cpuArchMod.networking.ScriptRequestC2SPacket;
import eigencraft.cpuArchMod.script.Script;
import eigencraft.cpuArchMod.script.ServerScriptManager;
import eigencraft.cpuArchMod.simulation.DynamicAgent;
import eigencraft.cpuArchMod.simulation.SimulationWorldInterface;
import eigencraft.cpuArchMod.simulation.WorldRunnable;
import eigencraft.cpuArchMod.simulation.agents.ProgrammableAgent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.UUID;

public class CpuArchMod implements ModInitializer {
    public static final String MOD_ID = "cpu_arch_mod";
    public static final Logger LOGGER = LogManager.getLogger();

    public static Item PIPE_ITEM;
    public static Item NODE_ITEM;

    //TODO add better item image
    public static final ItemGroup CPU_ARCH_MOD_ITEMGROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "items"),
            () -> new ItemStack(NODE_ITEM));

    public static final Block PIPE = new PipeContainer();
    public static final Block PROGRAMMABLE_NODE = new ProgrammableAgentContainerBlock();


    public static ServerSideConfiguration CONFIGURATION;

    public static BlockEntityType<ProgrammableAgentBlockEntity> PROGRAMMABLE_AGENT_BLOCK_ENTITY;

    public static ServerScriptManager SCRIPT_MANAGER;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();


    @Override
    public void onInitialize() {
        //Load configuration
        try {
            //Config file
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(),"cpu_arch_mod.server.json");
            //Create config if it doesn't exist
            if (!configFile.isFile()){
                FileWriter fileWriter = new FileWriter(configFile);
                GSON.toJson(new ServerSideConfiguration(),fileWriter);
                fileWriter.close();
                LOGGER.warn("No configuration found, creating new configuration");
            }
            //Load config file
            CONFIGURATION = GSON.fromJson(new FileReader(configFile),ServerSideConfiguration.class);
        } catch (IOException e) {
            //Failed to load it, using default
            LOGGER.error(String.format("Failed to load configuration: %s",e.toString()));
            CONFIGURATION = new ServerSideConfiguration();
        }

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
            SCRIPT_MANAGER.saveIndexFile();
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
                Script script;
                if (SCRIPT_MANAGER.scriptExists(packet.getScriptFileName())){
                    script = SCRIPT_MANAGER.getScript(packet.getScriptFileName());
                } else {
                    script = new Script(packet.getScriptFileName(), UUID.randomUUID());
                }
                SCRIPT_MANAGER.store(script, packet.getScriptSrc());

                ServerWorld world = (ServerWorld) packetContext.getPlayer().getEntityWorld();
                ((SimulationWorldInterface) world).addSimulationWorldTask(world1 -> {
                    DynamicAgent simAgent = world1.getDynamicAgent(packet.getPos());
                    if (simAgent instanceof ProgrammableAgent) {
                        ProgrammableAgent agent = (ProgrammableAgent) simAgent;
                        agent.setScript(script);
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

        ServerSidePacketRegistry.INSTANCE.register(CpuArchModPackets.SCRIPT_REQUEST_C2S, new PacketConsumer() {
            @Override
            public void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
                ScriptRequestC2SPacket packet = ScriptRequestC2SPacket.readPacket(packetByteBuf);
                SCRIPT_MANAGER.answerScriptRequestPacket(packet,packetContext.getPlayer());
            }
        });

        //End of setup
    }
}


