package eigencraft.cpuArchMod.simulation;

import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class SimulationChunkStorage {
    public static final int version = 1;

    public static void saveChunk(ChunkPos pos, SimulationChunk chunk, File worldSavePath) {
        JsonObject root = new JsonObject();
        root.add("version", new JsonPrimitive(version));

        JsonArray simulationAgents = new JsonArray();
        root.add("agents", simulationAgents);

        for (Map.Entry<BlockPos, DynamicAgent> simulationAgent : chunk.getDynamicAgents().entrySet()) {
            JsonObject simulationAgentJson = new JsonObject();
            simulationAgentJson.add("x", new JsonPrimitive(simulationAgent.getKey().getX()));
            simulationAgentJson.add("y", new JsonPrimitive(simulationAgent.getKey().getY()));
            simulationAgentJson.add("z", new JsonPrimitive(simulationAgent.getKey().getZ()));
            simulationAgentJson.add("type", new JsonPrimitive(simulationAgent.getValue().getClass().getSimpleName()));
            JsonElement customData = simulationAgent.getValue().getConfigData();
            if (customData != null) {
                simulationAgentJson.add("data", customData);
            }
            simulationAgents.add(simulationAgentJson);
        }

        File chunkFile = getChunkSavePath(pos, worldSavePath);

        try {
            FileWriter writer = new FileWriter(chunkFile);
            writer.write(root.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void loadChunk(ChunkPos pos, SimulationChunk chunk, File worldSavePath) {
        File savePath = getChunkSavePath(pos, worldSavePath);

        if (savePath.isFile()) {
            JsonObject root;
            try {
                root = JsonParser.parseString(new String(Files.readAllBytes(savePath.toPath()))).getAsJsonObject();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                int found_version = root.get("version").getAsInt();
                if (found_version > version) {
                    LogManager.getLogger().warn("Found simulation chunk from newer mod version!");
                } else if (found_version < version) {
                    //TODO upgrade system?
                    LogManager.getLogger().warn(String.format("Found simulation chunk from older mod version! Found: %d, running version: %d", found_version, version));
                }

                JsonArray simulationAgents = root.getAsJsonArray("agents");
                for (JsonElement i : simulationAgents) {
                    JsonObject agentElement = i.getAsJsonObject();
                    BlockPos agentPos = new BlockPos(agentElement.get("x").getAsInt(), agentElement.get("y").getAsInt(), agentElement.get("z").getAsInt());
                    DynamicAgent dynamicAgent = DynamicAgent.getSimulationObject(agentElement.get("type").getAsString());
                    if (agentElement.has("data")) {
                        dynamicAgent.loadConfig(agentElement.get("data"));
                    }
                    chunk.addAgent(agentPos, dynamicAgent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static File getChunkSavePath(ChunkPos pos, File worldSavePath) {
        return new File(worldSavePath, String.format("x%dz%d.json", pos.x, pos.z));
    }
}
