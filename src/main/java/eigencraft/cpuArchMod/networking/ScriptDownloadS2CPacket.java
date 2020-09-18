package eigencraft.cpuArchMod.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

import java.util.UUID;

public class ScriptDownloadS2CPacket {
    private UUID id;
    private String fileSrc;
    private boolean success;

    public UUID getUUID() {
        return id;
    }

    public String getFileSrc() {
        return fileSrc;
    }

    public boolean getSuccess() {
        return success;
    }

    public ScriptDownloadS2CPacket(boolean success, UUID id, String fileSrc){
        this.id = id;
        this.fileSrc = fileSrc;
        this.success = success;
    }

    public PacketByteBuf asPacketByteBuffer(){
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeBoolean(success);
        buffer.writeUuid(id);
        buffer.writeString(fileSrc);
        return buffer;
    }

    public static ScriptDownloadS2CPacket readPacket(PacketByteBuf buffer){
        return new ScriptDownloadS2CPacket(buffer.readBoolean(), buffer.readUuid(), buffer.readString(32767));
    }
}
