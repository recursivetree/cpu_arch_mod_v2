package eigencraft.cpuArchMod.networking;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class PrgAgentOpenGUIPacket {
    public BlockPos getPos() {
        return pos;
    }

    public String getCurrentScriptFileName() {
        return currentScriptFileName;
    }

    public String getCurrentScript() {
        return currentScript;
    }

    public String getErrorLog() {
        return errorLog;
    }

    public PrgAgentOpenGUIPacket(BlockPos pos, String currentScriptFileName, String currentScript, String errorLog) {
        this.pos = pos;
        this.currentScriptFileName = currentScriptFileName;
        this.currentScript = currentScript;
        this.errorLog = errorLog;
    }

    public PacketByteBuf asPacketByteBuffer(){
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(pos);
        buffer.writeString(currentScriptFileName);
        buffer.writeString(currentScript);
        buffer.writeString(errorLog);
        return buffer;
    }

    public static PrgAgentOpenGUIPacket readPacket(PacketByteBuf buffer){
        BlockPos pos = buffer.readBlockPos();
        String currentScriptFileName = buffer.readString();
        String currentScript = buffer.readString();
        String errorLog = buffer.readString();
        if (errorLog.equals("")) errorLog = null;
        return new PrgAgentOpenGUIPacket(pos,currentScriptFileName,currentScript,errorLog);
    }

    BlockPos pos;
    String currentScriptFileName;
    String currentScript;
    String errorLog;
}
