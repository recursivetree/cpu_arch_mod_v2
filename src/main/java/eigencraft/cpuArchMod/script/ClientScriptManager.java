package eigencraft.cpuArchMod.script;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ClientScriptManager {
    private File localFilesDirectory;

    public ClientScriptManager(File localFilesDirectory) {
        this.localFilesDirectory = localFilesDirectory;
        ensureDirectory(this.localFilesDirectory);
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
}
