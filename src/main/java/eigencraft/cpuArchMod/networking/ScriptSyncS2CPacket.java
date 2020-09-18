package eigencraft.cpuArchMod.networking;

import eigencraft.cpuArchMod.script.ClientScript;
import eigencraft.cpuArchMod.script.Script;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScriptSyncS2CPacket {
    private Collection<Script> scriptList;

    public ScriptSyncS2CPacket(Collection<Script> scripts){
        scriptList = scripts;
    }

    public PacketByteBuf asPacketByteBuffer(){
        PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
        buffer.writeInt(scriptList.size());
        for(Script script:scriptList){
            buffer.writeString(script.getName());
            buffer.writeUuid(script.getUUID());
        }
        return buffer;
    }

    public static List<ClientScript> readPacket(PacketByteBuf buffer){

        List<ClientScript> scripts = new ArrayList<>();

        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            scripts.add(new ClientScript(buffer.readString(), buffer.readUuid(), ClientScript.SCRIPT_TYPE.SERVERSIDE));
        }
        return scripts;
    }
}
