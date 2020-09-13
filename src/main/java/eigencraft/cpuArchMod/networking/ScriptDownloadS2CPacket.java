package eigencraft.cpuArchMod.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class ScriptDownloadS2CPacket {
    private String fileName;
    private String fileSrc;
    private boolean success;

    public String getFileName() {
        return fileName;
    }

    public String getFileSrc() {
        return fileSrc;
    }

    public boolean getSuccess() {
        return success;
    }

    public ScriptDownloadS2CPacket(boolean success, String fileName, String fileSrc){
        this.fileName = fileName;
        this.fileSrc = fileSrc;
        this.success = success;
    }

    public PacketByteBuf asPacketByteBuffer(){
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeBoolean(success);
        buffer.writeString(fileName);
        buffer.writeString(fileSrc);
        return buffer;
    }

    public static ScriptDownloadS2CPacket readPacket(PacketByteBuf buffer){
        return new ScriptDownloadS2CPacket(buffer.readBoolean(), buffer.readString(32767), buffer.readString(32767));
    }
}
