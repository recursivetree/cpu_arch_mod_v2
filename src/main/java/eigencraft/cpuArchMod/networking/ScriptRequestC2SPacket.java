package eigencraft.cpuArchMod.networking;

import eigencraft.cpuArchMod.script.ClientScript;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class ScriptRequestC2SPacket {
    public ScriptRequestC2SPacket(ClientScript script) {
        this.id = script.getUUID();
    }

    public ScriptRequestC2SPacket(UUID id) {
        this.id = id;
    }

    UUID id;

    public UUID getUUID(){
        return id;
    }

    public PacketByteBuf asPacketByteBuffer(){
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeUuid(id);
        return buffer;
    }

    public static ScriptRequestC2SPacket readPacket(PacketByteBuf buffer){
        return new ScriptRequestC2SPacket(buffer.readUuid());
    }
}
