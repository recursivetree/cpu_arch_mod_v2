package eigencraft.cpuArchMod;

import eigencraft.cpuArchMod.block.ProgrammableAgentBlockEntity;
import eigencraft.cpuArchMod.gui.ProgrammableAgentGUI;
import eigencraft.cpuArchMod.script.ClientScriptManager;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

import java.io.File;

public class CpuArchModClient implements ClientModInitializer {

    public static ClientScriptManager SCRIPT_MANAGER = new ClientScriptManager(new File(new File(FabricLoader.getInstance().getConfigDirectory(), "cpu_arch_mod_scripts"),"temp"));;

    @Override
    public void onInitializeClient() {

        //Block entity renderer
        BlockEntityRendererRegistry.INSTANCE.register(CpuArchMod.PROGRAMMABLE_AGENT_BLOCK_ENTITY, ProgrammableAgentBlockEntity.ProgrammableAgentRenderer::new);

        //Networking
        ClientSidePacketRegistry.INSTANCE.register(CpuArchMod.PROGRAMMABLE_AGENT_OPEN_GUI_S2C_PACKET, (packetContext, packetByteBuf) -> {
            BlockPos pos = packetByteBuf.readBlockPos();
            String currentScriptFileName = packetByteBuf.readString();
            String currentScript = packetByteBuf.readString();
            String errorLog = packetByteBuf.readString();
            if (errorLog.equals("")) errorLog = null;
            //Weird, but otherwise it won't compile
            String finalErrorLog = errorLog;
            packetContext.getTaskQueue().execute(() -> MinecraftClient.getInstance().openScreen(new CottonClientScreen(new ProgrammableAgentGUI(currentScriptFileName, currentScript, finalErrorLog, pos))));
        });

        ClientSidePacketRegistry.INSTANCE.register(CpuArchMod.SCRIPT_MANAGER_SYNC_S2C, new PacketConsumer() {
            @Override
            public void accept(PacketContext context, PacketByteBuf buffer) {
                String fileName = buffer.readString();
                String data = buffer.readString();
                CpuArchModClient.SCRIPT_MANAGER.store(fileName,data);
            }
        });
    }
}
