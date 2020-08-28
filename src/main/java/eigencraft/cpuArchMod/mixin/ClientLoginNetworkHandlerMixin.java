package eigencraft.cpuArchMod.mixin;

import eigencraft.cpuArchMod.CpuArchModClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLoginNetworkHandler.class)
public class ClientLoginNetworkHandlerMixin {
    @Inject(method = "onLoginSuccess(Lnet/minecraft/network/packet/s2c/login/LoginSuccessS2CPacket;)V", at = @At("HEAD"))
    public void connectProxy(LoginSuccessS2CPacket packet, CallbackInfo cb){
        String name = CpuArchModClient.CONFIGURATION.DEFAULT_SCRIPT_DIRECTORY_NAME;
        if (MinecraftClient.getInstance().isIntegratedServerRunning() && MinecraftClient.getInstance().getServer() != null) {
            //TODO get the name
            name = "singleplayer";
        } else if (MinecraftClient.getInstance().getCurrentServerEntry() != null) {
            name = MinecraftClient.getInstance().getCurrentServerEntry().name;
        }
        CpuArchModClient.SCRIPT_MANAGER.setWorldDirectory(name);
    }
}
