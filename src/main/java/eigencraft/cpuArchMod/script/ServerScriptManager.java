package eigencraft.cpuArchMod.script;

import com.google.gson.*;
import eigencraft.cpuArchMod.CpuArchMod;
import eigencraft.cpuArchMod.networking.CpuArchModPackets;
import eigencraft.cpuArchMod.networking.ScriptDownloadS2CPacket;
import eigencraft.cpuArchMod.networking.ScriptRequestC2SPacket;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class ServerScriptManager {
    public static final Script NO_SCRIPT = new Script("unconfigured",UUID.nameUUIDFromBytes(new byte[]{0,0,0,0,0,0,0,0}));
    private File localFilesDirectory;
    private final HashMap<String,Script> scripts = new HashMap<>();

    public ServerScriptManager(File localFilesDirectory) {
        ensureDirectory(localFilesDirectory);
        this.localFilesDirectory = localFilesDirectory;
        loadAvailableFiles();
    }

    private static void ensureDirectory(File localFilesDirectory) {
        if (!localFilesDirectory.isDirectory()) {
            if (!localFilesDirectory.mkdirs()) throw new IllegalStateException("Can't create directory");
        }
    }

    public void loadAvailableFiles() {
        try {
            JsonElement root = new JsonParser().parse(new FileReader(new File(localFilesDirectory,"index.json")));
            JsonArray scriptsArray = root.getAsJsonArray();
            for (JsonElement entry:scriptsArray){
                JsonObject scriptEntry = entry.getAsJsonObject();
                scripts.put(
                        scriptEntry.get("name").getAsString(),
                        new Script(scriptEntry.get("name").getAsString(), UUID.fromString(scriptEntry.get("uuid").getAsString()))
                );
            }
        } catch (JsonParseException | FileNotFoundException e) {
            CpuArchMod.LOGGER.error(String.format("Failed to read script index file: %s",e.getMessage()));
        }
    }

    public void saveIndexFile(){
        CpuArchMod.LOGGER.info("saving index file");
        JsonArray root = new JsonArray();
        for (Script script: scripts.values()){
            JsonObject entry = new JsonObject();
            entry.add("name",new JsonPrimitive(script.getName()));
            entry.add("uuid",new JsonPrimitive(script.getUUID().toString()));
            root.add(entry);
            //System.out.println(entry.toString());
        }

        try {
            FileWriter writer = new FileWriter(new File(localFilesDirectory,"index.json"));
            writer.write(root.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void store(Script script, String src) {
        if (!scripts.containsValue(script)){
            scripts.put(script.getName(),script);
        }
        try {
            FileWriter writer = new FileWriter(getScriptFileName(script));
            writer.write(src);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getScriptFileName(Script script){
        return new File(localFilesDirectory,script.getUUID().toString()+".lua");
    }

    public String readScript(Script script) throws IOException {
        File fileName = getScriptFileName(script);
        return new String(Files.readAllBytes(fileName.toPath()));
    }


    public void answerScriptRequestPacket(ScriptRequestC2SPacket packet, PlayerEntity player) {
        ScriptDownloadS2CPacket responsePacket;
        try{
            Script requested = null;
            for (Script script:scripts.values()){
                if (script.getUUID().equals(packet.getUUID())){
                    requested = script;
                    break;
                }
            }
            if (requested==null){
                responsePacket = new ScriptDownloadS2CPacket(false, packet.getUUID(), "");
            } else {
                responsePacket = new ScriptDownloadS2CPacket(true, packet.getUUID(), readScript(requested));
            }
        } catch (IOException e){
            responsePacket = new ScriptDownloadS2CPacket(false,packet.getUUID(),"");
        }
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player,CpuArchModPackets.SCRIPT_DOWNLOAD_S2C,responsePacket.asPacketByteBuffer());
    }

    public boolean scriptExists(String scriptFileName) {
        return scripts.containsKey(scriptFileName);
    }

    public Script getScript(String scriptFileName) {
        return scripts.get(scriptFileName);
    }

    public Script getScript(UUID id){
        Script script;
        for(Script s: scripts.values()){
            if (s.getUUID().equals(id)){
                return s;
            }
        }
        return null;
    }

    public Collection<Script> listScripts() {
        return scripts.values();
    }
}
