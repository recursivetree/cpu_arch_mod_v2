package eigencraft.cpuArchMod.mixin;

import eigencraft.cpuArchMod.CpuArchMod;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void onPlayerConnectEnd(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {

        //Sync serverside scripts
        for (String fileName: CpuArchMod.SCRIPT_MANAGER.listAvailableFiles()){
            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
            passedData.writeString(fileName);
            try {
                passedData.writeString(CpuArchMod.SCRIPT_MANAGER.readFile(fileName));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, CpuArchMod.SCRIPT_MANAGER_SYNC_S2C, passedData);
        }
    }
}
