package eigencraft.cpuArchMod.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class ScriptSyncS2CPacket {
    private String fileName;
    private String fileSrc;

    public String getFileName() {
        return fileName;
    }

    public String getFileSrc() {
        return fileSrc;
    }

    public ScriptSyncS2CPacket(String fileName,String fileSrc){
        this.fileName = fileName;
        this.fileSrc = fileSrc;
    }

    public PacketByteBuf asPacketByteBuffer(){
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeString(fileName);
        buffer.writeString(fileSrc);
        return buffer;
    }

    public static ScriptSyncS2CPacket readPacket(PacketByteBuf buffer){
        return new ScriptSyncS2CPacket(buffer.readString(), buffer.readString());
    }
}
