package eigencraft.cpuArchMod.simulation;

import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class SimulationChunkStorage {
    public static final int version = 0;

    public static void saveChunk(ChunkPos pos, SimulationChunk chunk,File worldSavePath){
        JsonObject root = new JsonObject();
        root.add("version",new JsonPrimitive(version));

        JsonArray simulationObjects = new JsonArray();
        root.add("objects",simulationObjects);

        for(Map.Entry<BlockPos, SimulationAgent> simulationObject:chunk.getSimulationObjects().entrySet()){
            JsonObject simulationObjectJson = new JsonObject();
            simulationObjectJson.add("x",new JsonPrimitive(simulationObject.getKey().getX()));
            simulationObjectJson.add("y",new JsonPrimitive(simulationObject.getKey().getY()));
            simulationObjectJson.add("z",new JsonPrimitive(simulationObject.getKey().getZ()));
            simulationObjectJson.add("type",new JsonPrimitive(simulationObject.getValue().getClass().getSimpleName()));
            JsonElement customData = simulationObject.getValue().getConfigData();
            if (customData!=null){
                simulationObjectJson.add("data",customData);
            }
            simulationObjects.add(simulationObjectJson);
        }

        File chunkFile = getChunkSavePath(pos,worldSavePath);

        try {
            FileWriter writer = new FileWriter(chunkFile);
            writer.write(root.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static SimulationChunk loadChunk(ChunkPos pos,SimulationChunk chunk, File worldSavePath){
        File savePath = getChunkSavePath(pos,worldSavePath);

        if (savePath.isFile()){
            JsonObject root;
            try {
                root = JsonParser.parseString(new String(Files.readAllBytes(savePath.toPath()))).getAsJsonObject();
            } catch (IOException e) {
                e.printStackTrace();
                return chunk;
            }

            JsonArray simulationObjects = root.getAsJsonArray("objects");
            for (JsonElement i:simulationObjects){
                JsonObject agentElement = i.getAsJsonObject();
                BlockPos agentPos = new BlockPos(agentElement.get("x").getAsInt(),agentElement.get("y").getAsInt(),agentElement.get("z").getAsInt());
                SimulationAgent simulationAgent = SimulationAgent.getSimulationObject(agentElement.get("type").getAsString());
                chunk.addAgent(agentPos,simulationAgent);
            }

        }
        return chunk;
    }
    public static File getChunkSavePath(ChunkPos pos, File worldSavePath){
        return new File(worldSavePath,String.format("x%dz%d.json",pos.x,pos.z));
    }
}
