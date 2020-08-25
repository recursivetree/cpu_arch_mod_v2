package eigencraft.cpuArchMod.simulation;

import com.google.gson.*;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class SimulationWorldStorage {
    public static final int SAVE_VERSION = 2;
    private static final Logger LOGGER = LogManager.getLogger();

    public static void saveWorld(SimulationWorld world, File savesDirectory){
        JsonObject root = new JsonObject();
        root.add("version",new JsonPrimitive(SAVE_VERSION));

        JsonArray dynamicAgents = new JsonArray();
        root.add("dynamicAgents",dynamicAgents);
        for(Map.Entry<BlockPos, DynamicAgent> agentData:world.getDynamicAgents().entrySet()){

            JsonObject jsonAgent = new JsonObject();

            jsonAgent.add("x",new JsonPrimitive(agentData.getKey().getX()));
            jsonAgent.add("y",new JsonPrimitive(agentData.getKey().getY()));
            jsonAgent.add("z",new JsonPrimitive(agentData.getKey().getZ()));
            jsonAgent.add("type",new JsonPrimitive(agentData.getValue().getClass().getSimpleName()));
            jsonAgent.add("customData",agentData.getValue().getConfigData());

            dynamicAgents.add(jsonAgent);
        }

        JsonArray pipeNetworks = new JsonArray();
        root.add("pipeNetworks",pipeNetworks);
        for (PipeNetwork network:world.getPipeNetworks()){

            JsonArray positions = new JsonArray();

            for (BlockPos pos:network.getMembers()){

                JsonObject jsonPos = new JsonObject();
                jsonPos.add("x",new JsonPrimitive(pos.getX()));
                jsonPos.add("y",new JsonPrimitive(pos.getY()));
                jsonPos.add("z",new JsonPrimitive(pos.getZ()));

                positions.add(jsonPos);
            }

            pipeNetworks.add(positions);
        }

        File chunkFile = new File(savesDirectory,"cpu_logic.json");

        try {
            FileWriter writer = new FileWriter(chunkFile);
            writer.write(root.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadWorld(SimulationWorld world,File savesDirectory){
        JsonObject root;
        try {
            root = JsonParser.parseString(new String(Files.readAllBytes(new File(savesDirectory,"cpu_logic.json").toPath()))).getAsJsonObject();
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to load simulation world: %s",e.toString()));
            return;
        }
        try {
            int found_version = root.get("version").getAsInt();
            if (found_version > SAVE_VERSION) {
                LOGGER.warn("Found simulation chunk from newer mod version!");
            } else if (found_version < SAVE_VERSION) {
                //TODO upgrade system?
                LOGGER.warn(String.format("Found simulation chunk from older mod version! Found: %d, running version: %d", found_version, SAVE_VERSION));
            }

            JsonArray dynamicAgents = root.getAsJsonArray("dynamicAgents");
            for (JsonElement agentJsonElement:dynamicAgents){
                JsonObject agentElement = agentJsonElement.getAsJsonObject();
                BlockPos agentPos = new BlockPos(agentElement.get("x").getAsInt(), agentElement.get("y").getAsInt(), agentElement.get("z").getAsInt());
                DynamicAgent dynamicAgent = DynamicAgent.createDynamicAgent(agentElement.get("type").getAsString(),world,agentPos);
                if (agentElement.has("customData")) {
                    dynamicAgent.loadConfig(agentElement.get("customData"));
                }
                world.addDynamicAgent(agentPos,dynamicAgent);
            }

            JsonArray pipeNetworks = root.getAsJsonArray("pipeNetworks");
            for (JsonElement jsonElement:pipeNetworks){
                JsonArray pipeNetwork = jsonElement.getAsJsonArray();
                for (JsonElement posElement:pipeNetwork){
                    JsonObject posObject = posElement.getAsJsonObject();
                    BlockPos pipePos = new BlockPos(posObject.get("x").getAsInt(), posObject.get("y").getAsInt(), posObject.get("z").getAsInt());
                    world.addPipe(pipePos);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
