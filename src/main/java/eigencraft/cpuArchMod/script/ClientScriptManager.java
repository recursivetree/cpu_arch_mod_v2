package eigencraft.cpuArchMod.script;

import eigencraft.cpuArchMod.networking.CpuArchModPackets;
import eigencraft.cpuArchMod.networking.ScriptDownloadS2CPacket;
import eigencraft.cpuArchMod.networking.ScriptRequestC2SPacket;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientScriptManager {
    private File localFilesDirectory;
    private final List<UUID> requestedScriptFiles = new ArrayList<UUID>();
    private final List<ClientScript> serverSideScripts = new ArrayList<>();

    public ClientScriptManager() {
        setWorldDirectory("shared");
    }

    private static boolean ensureDirectory(File localFilesDirectory) {
        if (!localFilesDirectory.isDirectory()) {
            if (!localFilesDirectory.mkdirs()) return false;
        }
        return true;
    }

    public File getScriptsDirectory(){
        return localFilesDirectory;
    }

    public List<ClientScript> listAvailableScripts() {
        List<ClientScript> files = new ArrayList<>();
        File[] dir_objects = localFilesDirectory.listFiles();
        if (dir_objects!=null) {
            for (File dir_object : dir_objects) {
                if (dir_object.isFile()) {
                    boolean existServerSide = false;
                    for(ClientScript script:serverSideScripts){
                        if (script.getName().equals(dir_object.getName())){
                            files.add(new ClientScript(dir_object.getName(), script.getUUID(), ClientScript.SCRIPT_TYPE.CLIENTSIDE));
                            existServerSide = true;
                        }
                    }
                    if (!existServerSide) {
                        files.add(new ClientScript(dir_object.getName(), UUID.randomUUID(), ClientScript.SCRIPT_TYPE.CLIENTSIDE));
                    }
                }
            }
        }
        files.addAll(serverSideScripts);
        return files;
    }

    public void addServerSideScript(ClientScript script){
        this.serverSideScripts.add(script);
    }

    private void store(String fileName, String src) {
        try {
            FileWriter writer = new FileWriter(new File(localFilesDirectory, fileName));
            writer.write(src);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFile(String file) throws IOException {
        return new String(Files.readAllBytes(new File(localFilesDirectory,file).toPath()));
    }

    public void setWorldDirectory(String name) {
        File directory = new File(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpu_arch_mod_scripts"), name);

        if (!ensureDirectory(directory)) {
            setWorldDirectory("default");
            return;
        }
        localFilesDirectory = directory;
    }

    public void processScriptDownloadPacket(ScriptDownloadS2CPacket packet) {
        if (packet.getSuccess()) {
            if (requestedScriptFiles.contains(packet.getUUID())) {
                for(ClientScript script:serverSideScripts){
                    if (script.getUUID().equals(packet.getUUID())){
                        this.store(script.getName(), packet.getFileSrc());
                        break;
                    }
                }
            }
        }
    }


    public void requestScript(ClientScript script) {
        requestedScriptFiles.add(script.getUUID());
        ScriptRequestC2SPacket packet = new ScriptRequestC2SPacket(script);
        ClientSidePacketRegistry.INSTANCE.sendToServer(CpuArchModPackets.SCRIPT_REQUEST_C2S, packet.asPacketByteBuffer());
        System.out.println("sent");
    }
}
