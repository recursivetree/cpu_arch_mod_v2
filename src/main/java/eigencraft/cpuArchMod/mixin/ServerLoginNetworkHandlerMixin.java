package eigencraft.cpuArchMod.mixin;

import eigencraft.cpuArchMod.CpuArchMod;
import eigencraft.cpuArchMod.networking.CpuArchModPackets;
import eigencraft.cpuArchMod.networking.ScriptSyncS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.server.network.ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {
    @Shadow @Final public ClientConnection connection;

    @Inject(method = "acceptPlayer()V",at=@At("RETURN"))
    public void syncScriptsClient(CallbackInfo info){
        connection.send(new CustomPayloadS2CPacket(CpuArchModPackets.SCRIPT_SYNC_S2C,new ScriptSyncS2CPacket(CpuArchMod.SCRIPT_MANAGER.listScripts()).asPacketByteBuffer()));
    }
}
