package eigencraft.cpuArchMod;

import eigencraft.cpuArchMod.block.ProgrammableAgentBlockEntity;
import eigencraft.cpuArchMod.gui.CpuArchModScreen;
import eigencraft.cpuArchMod.gui.ProgrammableAgentGUI;
import eigencraft.cpuArchMod.networking.CpuArchModPackets;
import eigencraft.cpuArchMod.networking.PrgAgentOpenGUIPacket;
import eigencraft.cpuArchMod.networking.ScriptDownloadS2CPacket;
import eigencraft.cpuArchMod.networking.ScriptSyncS2CPacket;
import eigencraft.cpuArchMod.script.ClientScript;
import eigencraft.cpuArchMod.script.ClientScriptManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.util.List;

public class CpuArchModClient implements ClientModInitializer {
    public static ClientScriptManager SCRIPT_MANAGER = new ClientScriptManager();

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

        ClientSidePacketRegistry.INSTANCE.register(CpuArchModPackets.SCRIPT_DOWNLOAD_S2C, new PacketConsumer() {
            @Override
            public void accept(PacketContext context, PacketByteBuf buffer) {
                ScriptDownloadS2CPacket scriptPacket = ScriptDownloadS2CPacket.readPacket(buffer);
                CpuArchModClient.SCRIPT_MANAGER.processScriptDownloadPacket(scriptPacket);
            }
        });

        ClientSidePacketRegistry.INSTANCE.register(CpuArchModPackets.SCRIPT_SYNC_S2C, new PacketConsumer() {
            @Override
            public void accept(PacketContext packetContext, PacketByteBuf packetByteBuf) {
                List<ClientScript> scripts = ScriptSyncS2CPacket.readPacket(packetByteBuf);
                for (ClientScript script:scripts){
                    SCRIPT_MANAGER.addServerSideScript(script);
                }
            }
        });
    }
}
