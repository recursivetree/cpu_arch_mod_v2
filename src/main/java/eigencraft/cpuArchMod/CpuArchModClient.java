package eigencraft.cpuArchMod;

import eigencraft.cpuArchMod.block.ProgrammableAgentBlockEntity;
import eigencraft.cpuArchMod.gui.CpuArchModScreen;
import eigencraft.cpuArchMod.gui.ProgrammableAgentGUI;
import eigencraft.cpuArchMod.networking.CpuArchModPackets;
import eigencraft.cpuArchMod.networking.PrgAgentOpenGUIPacket;
import eigencraft.cpuArchMod.networking.ScriptSyncS2CPacket;
import eigencraft.cpuArchMod.script.ClientScriptManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.io.File;

public class CpuArchModClient implements ClientModInitializer {

    public static ClientScriptManager SCRIPT_MANAGER = new ClientScriptManager(new File(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpu_arch_mod_scripts"),"temp"));

    @Override
    public void onInitializeClient() {

        //Block entity renderer
        BlockEntityRendererRegistry.INSTANCE.register(CpuArchMod.PROGRAMMABLE_AGENT_BLOCK_ENTITY, ProgrammableAgentBlockEntity.ProgrammableAgentRenderer::new);

        //Networking
        ClientSidePacketRegistry.INSTANCE.register(CpuArchModPackets.PROGRAMMABLE_AGENT_OPEN_GUI_S2C_PACKET, (packetContext, packetByteBuf) -> {
            PrgAgentOpenGUIPacket guiPacket = PrgAgentOpenGUIPacket.readPacket(packetByteBuf);
            packetContext.getTaskQueue().execute(
                    () -> MinecraftClient.getInstance().openScreen(
                            new CpuArchModScreen(
                                    new ProgrammableAgentGUI(
                                            guiPacket.getCurrentScriptFileName(),
                                            guiPacket.getCurrentScript(),
                                            guiPacket.getErrorLog(),
                                            guiPacket.getPos()
                                    )
                            )
                    )
            );
        });

        ClientSidePacketRegistry.INSTANCE.register(CpuArchModPackets.SYNC_SCRIPT_S2C, new PacketConsumer() {
            @Override
            public void accept(PacketContext context, PacketByteBuf buffer) {
                ScriptSyncS2CPacket scriptPacket = ScriptSyncS2CPacket.readPacket(buffer);
                CpuArchModClient.SCRIPT_MANAGER.store(scriptPacket.getFileName(), scriptPacket.getFileSrc());
            }
        });
    }
}
