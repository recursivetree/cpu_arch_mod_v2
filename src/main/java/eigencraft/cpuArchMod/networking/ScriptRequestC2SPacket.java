package eigencraft.cpuArchMod.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class ScriptRequestC2SPacket {
    public ScriptRequestC2SPacket(String fileName) {
        this.fileName = fileName;
    }

    String fileName;

    public String getFileName(){
        return fileName;
    }

    public PacketByteBuf asPacketByteBuffer(){
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeString(fileName);
        return buffer;
    }

    public static ScriptRequestC2SPacket readPacket(PacketByteBuf buffer){
        return new ScriptRequestC2SPacket(buffer.readString(32767));
    }
}
