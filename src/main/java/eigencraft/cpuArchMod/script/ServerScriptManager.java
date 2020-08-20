package eigencraft.cpuArchMod.script;

import eigencraft.cpuArchMod.CpuArchMod;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ServerScriptManager {
    private File localFilesDirectory;

    public ServerScriptManager(File localFilesDirectory) {
        ensureDirectory(localFilesDirectory);
        this.localFilesDirectory = localFilesDirectory;
    }

    private static void ensureDirectory(File localFilesDirectory) {
        if (!localFilesDirectory.isDirectory()) {
            if (!localFilesDirectory.mkdirs()) throw new IllegalStateException("Can't create directory");
        }
    }

    public File getScriptsDirectory(){
        return localFilesDirectory;
    }

    public List<String> listAvailableFiles() {
        List<String> files = new ArrayList<>();
        File[] dir_objects = localFilesDirectory.listFiles();
        for (int i = 0; i < dir_objects.length; i++) {
            if (dir_objects[i].isFile()){
                files.add(dir_objects[i].getName());
            }
        }
        return files;
    }

    public void store(String fileName, String src) {
        try {
            FileWriter writer = new FileWriter(new File(localFilesDirectory,fileName));
            writer.write(src);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFile(String file) throws IOException {
        return new String(Files.readAllBytes(new File(localFilesDirectory,file).toPath()));
    }

    public void syncScriptsToServer(Collection<ServerPlayerEntity> players){
        for (String fileName: listAvailableFiles()){
            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
            passedData.writeString(fileName);
            try {
                passedData.writeString(CpuArchMod.SCRIPT_MANAGER.readFile(fileName));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            for(ServerPlayerEntity player:players) {
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, CpuArchMod.SCRIPT_MANAGER_SYNC_S2C, passedData);
            }
        }
    }
}
