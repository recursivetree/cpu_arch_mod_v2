package eigencraft.cpuArchMod;


import eigencraft.cpuArchMod.block.PipeContainer;
import eigencraft.cpuArchMod.block.ProgrammableAgentContainer;
import eigencraft.cpuArchMod.gui.ProgrammableAgentGUI;
import eigencraft.cpuArchMod.simulation.SimulationAgent;
import eigencraft.cpuArchMod.simulation.SimulationWorld;
import eigencraft.cpuArchMod.simulation.SimulationWorldInterface;
import eigencraft.cpuArchMod.simulation.SimulationWorldRunnable;
import eigencraft.cpuArchMod.simulation.agents.PipeAgent;
import eigencraft.cpuArchMod.simulation.agents.ProgrammableAgent;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CpuArchMod implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "cpu_arch_mod";

    //TODO add better item image
    public static final ItemGroup CPU_ARCH_MOD_ITEMGROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "items"),
            () -> ItemStack.EMPTY);

    public static final Block PIPE = new PipeContainer();
    public static final Block PROGRAMMABLE_NODE = new ProgrammableAgentContainer();

    public static final Identifier PROGRAMMABLE_AGENT_OPEN_GUI_S2C_PACKET = new Identifier(MOD_ID,"prg_agent_open_s2c");
    public static final Identifier PROGRAMMABLE_AGENT_SAFE_CONFIG_C2S_PACKET = new Identifier(MOD_ID,"prg_agent_safe_c2s");

    public static final Configuration CONFIGURATION = new Configuration();

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "pipe"), PIPE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "pipe"), new BlockItem(PIPE, new Item.Settings().group(CPU_ARCH_MOD_ITEMGROUP)));
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "node"), PROGRAMMABLE_NODE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "node"), new BlockItem(PROGRAMMABLE_NODE, new Item.Settings().group(CPU_ARCH_MOD_ITEMGROUP)));

        ServerStopCallback.EVENT.register(new ServerStopCallback() {
            @Override
            public void onStopServer(MinecraftServer minecraftServer) {
                for (ServerWorld world:minecraftServer.getWorlds()){
                    ((SimulationWorldInterface)world).stopSimulation();
                }
            }
        });

        SimulationAgent.register(ProgrammableAgent.class.getSimpleName(), ProgrammableAgent::new);
        SimulationAgent.register(PipeAgent.class.getSimpleName(), PipeAgent::new);

        new File(FabricLoader.getInstance().getConfigDirectory(),"cpu_arch_mod_scripts").mkdirs();

        ServerSidePacketRegistry.INSTANCE.register(PROGRAMMABLE_AGENT_SAFE_CONFIG_C2S_PACKET, new PacketConsumer() {
            @Override
            public void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
                BlockPos pos = packetByteBuf.readBlockPos();
                String fileName = packetByteBuf.readString();
                String src = packetByteBuf.readString();

                File saveFile = new File(new File(FabricLoader.getInstance().getConfigDirectory(),"cpu_arch_mod_scripts"),fileName);
                try {
                    FileWriter writer = new FileWriter(saveFile);
                    writer.write(src);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ServerWorld world = (ServerWorld) packetContext.getPlayer().getEntityWorld();
                ((SimulationWorldInterface) world).addSimulationWorldTask(new SimulationWorldRunnable() {
                    @Override
                    public void run(SimulationWorld world) {
                        SimulationAgent simAgent = world.getSimulationAgent(pos);
                        if (simAgent instanceof ProgrammableAgent){
                            ProgrammableAgent agent = (ProgrammableAgent) simAgent;
                            agent.setScriptFileName(fileName);
                            agent.setScriptSrc(src);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onInitializeClient() {
        ClientSidePacketRegistry.INSTANCE.register(PROGRAMMABLE_AGENT_OPEN_GUI_S2C_PACKET, new PacketConsumer() {
            @Override
            public void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
                BlockPos pos = packetByteBuf.readBlockPos();
                String currentScriptFileName = packetByteBuf.readString();
                String currentScript = packetByteBuf.readString();
                packetContext.getTaskQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        MinecraftClient.getInstance().openScreen(new CottonClientScreen(new ProgrammableAgentGUI(currentScriptFileName,currentScript,pos)));
                    }
                });
            }
        });
    }
}


