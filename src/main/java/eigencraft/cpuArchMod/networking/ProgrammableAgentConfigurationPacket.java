package eigencraft.cpuArchMod.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class ProgrammableAgentConfigurationPacket {

    BlockPos pos;
    boolean hasScriptSrc;
    String scriptSrc;
    String scriptFileName;
    String displayName;
    int displayColor;

    public ProgrammableAgentConfigurationPacket(BlockPos pos, boolean hasScriptSrc, String scriptSrc, String scriptFileName, String displayName, int displayColor) {
        this.pos = pos;
        this.hasScriptSrc = hasScriptSrc;
        this.scriptSrc = scriptSrc;
        this.scriptFileName = scriptFileName;
        this.displayName = displayName;
        this.displayColor = displayColor;
    }

    public PacketByteBuf asPacketByteBuf(){
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(hasScriptSrc);
        if (hasScriptSrc){
            buffer.writeString(scriptFileName);
            buffer.writeString(scriptSrc);
        }
        buffer.writeString(displayName);
        buffer.writeInt(displayColor);

        return buffer;
    }

    public static ProgrammableAgentConfigurationPacket readPacket(PacketByteBuf buffer){
        BlockPos pos = buffer.readBlockPos();
        boolean hasScriptSrc = buffer.readBoolean();

        String scriptSrc = null;
        String scriptFileName = null;
        if (hasScriptSrc){
            scriptFileName = buffer.readString();
            scriptSrc = buffer.readString();
        }

        String displayName = buffer.readString();
        int displayColor = buffer.readInt();
        return new ProgrammableAgentConfigurationPacket(pos,hasScriptSrc,scriptSrc,scriptFileName,displayName,displayColor);
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean hasScriptSrc() {
        return hasScriptSrc;
    }

    public String getScriptSrc() {
        return scriptSrc;
    }

    public String getScriptFileName() {
        return scriptFileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDisplayColor() {
        return displayColor;
    }

}
