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

public class ClientScriptManager {
    private File localFilesDirectory;
    private List<String> requestedScriptFiles = new ArrayList<>();

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

    public List<String> listAvailableFiles() {
        List<String> files = new ArrayList<>();
        File[] dir_objects = localFilesDirectory.listFiles();
        if (dir_objects!=null) {
            for (File dir_object : dir_objects) {
                if (dir_object.isFile()) {
                    files.add(dir_object.getName());
                }
            }
        }
        return files;
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
        System.out.println("back");
        if (packet.getSuccess()) {
            System.out.println("success packet");
            System.out.println(packet.getFileName());
            if (requestedScriptFiles.contains(packet.getFileName())) {
                System.out.println("expected packet");
                this.store(packet.getFileName(), packet.getFileSrc());
            }
        }
    }


    public void requestScript(String scriptName) {
        requestedScriptFiles.add(scriptName);
        ScriptRequestC2SPacket packet = new ScriptRequestC2SPacket(scriptName);
        ClientSidePacketRegistry.INSTANCE.sendToServer(CpuArchModPackets.SCRIPT_REQUEST_C2S, packet.asPacketByteBuffer());
        System.out.println("sent");
    }
}
