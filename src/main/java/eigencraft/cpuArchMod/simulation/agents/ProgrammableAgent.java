package eigencraft.cpuArchMod.simulation.agents;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import eigencraft.cpuArchMod.script.LuaScript;
import eigencraft.cpuArchMod.simulation.SimulationAgent;
import eigencraft.cpuArchMod.simulation.SimulationMessage;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ProgrammableAgent implements SimulationAgent {
    private final ArrayList<SimulationAgent> connectedObjects =   new ArrayList<>();
    private LinkedList<SimulationMessage> messages = new LinkedList<>();
    LuaScript luaScript = new LuaScript();

    public ProgrammableAgent(){
        luaScript.compileCode("",0);
    }

    public String getScriptFileName() {
        return scriptFileName;
    }

    public String getScriptSrc() {
        return scriptSrc;
    }

    public void setScriptFileName(String scriptFileName) {
        this.scriptFileName = scriptFileName;
    }

    public void setScriptSrc(String scriptSrc) {
        this.scriptSrc = scriptSrc;
        luaScript.compileCode(scriptSrc,100);
    }

    private String scriptFileName = "no script";
    private String scriptSrc = "";

    public void connect(SimulationAgent other){
        connectedObjects.add(other);
    }

    public void disconnect(SimulationAgent other){
        connectedObjects.remove(other);
    }

    public ArrayList<SimulationAgent> getConnections(){
        return connectedObjects;
    }

    @Override
    public void tick() {
        while (!messages.isEmpty()){
            messages.remove();
        }
    }

    @Override
    public void process(SimulationMessage message) {
        for (SimulationAgent agent:connectedObjects){
            agent.process(message);
        }
        messages.add(message);
        try {
            luaScript.execute();
        } catch (LuaScript.LuaScriptError luaScriptError) {
            luaScriptError.printStackTrace();
        }
    }

    @Override
    public JsonElement getConfigData() {
        JsonObject root = new JsonObject();
        root.add("file",new JsonPrimitive(scriptFileName));
        return root;
    }

    @Override
    public void loadConfig(JsonElement rawConfig) {
        if (rawConfig.isJsonObject()){
            JsonObject config = rawConfig.getAsJsonObject();
            String fileName = config.get("file").getAsString();
            File srcFile = new File(new File(FabricLoader.getInstance().getConfigDirectory(),"cpu_arch_mod_scripts"),fileName);
            System.out.println(srcFile.toString());
            if (srcFile.isFile()){
                try {
                    scriptSrc = new String(Files.readAllBytes(srcFile.toPath()));
                    scriptFileName = fileName;
                } catch (IOException e) {
                    return;
                }
            }
        }
    }
}
