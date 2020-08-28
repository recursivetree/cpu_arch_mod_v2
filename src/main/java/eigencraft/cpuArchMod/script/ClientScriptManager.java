package eigencraft.cpuArchMod.script;

import eigencraft.cpuArchMod.CpuArchModClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ClientScriptManager {
    private File localFilesDirectory;

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

    public void setWorldDirectory(String name) {
        File directory = new File(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpu_arch_mod_scripts"),(name.equals("") ? CpuArchModClient.CONFIGURATION.DEFAULT_SCRIPT_DIRECTORY_NAME : name));

        if (CpuArchModClient.CONFIGURATION.USE_WORLD_DIRECTORIES&&localFilesDirectory==null) {
            directory = new File(new File(FabricLoader.getInstance().getConfigDir().toFile(), "cpu_arch_mod_scripts"),CpuArchModClient.CONFIGURATION.DEFAULT_SCRIPT_DIRECTORY_NAME);
        }

        if (!ensureDirectory(directory)) setWorldDirectory(CpuArchModClient.CONFIGURATION.DEFAULT_SCRIPT_DIRECTORY_NAME);
        localFilesDirectory = directory;
    }
}
