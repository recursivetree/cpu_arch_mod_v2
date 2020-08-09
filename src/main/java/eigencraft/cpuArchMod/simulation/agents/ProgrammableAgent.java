package eigencraft.cpuArchMod.simulation.agents;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import eigencraft.cpuArchMod.CpuArchMod;
import eigencraft.cpuArchMod.script.LuaAPI;
import eigencraft.cpuArchMod.script.LuaScript;
import eigencraft.cpuArchMod.simulation.SimulationAgent;
import eigencraft.cpuArchMod.simulation.SimulationMessage;
import net.fabricmc.loader.api.FabricLoader;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;

public class ProgrammableAgent extends SimulationAgent {
    private LinkedList<SimulationMessage> messages = new LinkedList<>();
    LuaScript luaScript = new LuaScript();
    LuaAPI api = new LuaAPI("node");
    private String scriptFileName = "no script";
    private String scriptSrc = "";

    LuaAPI.LuaCallback ON_REDSTONE_SIGNAL = new LuaAPI.LuaCallback();
    LuaAPI.LuaCallback ON_MESSAGE = new LuaAPI.LuaCallback();


    public ProgrammableAgent(){
        api.register("onRedstoneSignal",ON_REDSTONE_SIGNAL);
        api.register("onMessage",ON_MESSAGE);
        api.register("publish",new MessagePublisher());
        luaScript.compileCode("",CpuArchMod.CONFIGURATION.getScriptExecutionTimeout(),api);
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
        luaScript.compileCode(scriptSrc, CpuArchMod.CONFIGURATION.getScriptExecutionTimeout(),api);
    }

    @Override
    public void tick() {
        while (!messages.isEmpty()){
            SimulationMessage message = messages.remove();
            try {
                luaScript.execute(ON_MESSAGE.getCallback(),message.getAsLuaValue());
            } catch (LuaError|LuaScript.WatchDogError error) {
                error.printStackTrace();
            }
        }
    }

    public void onRedstonePowered(){
        try {
            luaScript.execute(ON_REDSTONE_SIGNAL.getCallback());
        } catch (LuaError|LuaScript.WatchDogError error) {
            error.printStackTrace();
        }
    }

    @Override
    public void process(SimulationMessage message) {
        if (!isLocked()) {
            this.lock();
            messages.add(message);
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
            if (srcFile.isFile()){
                try {
                    setScriptSrc(new String(Files.readAllBytes(srcFile.toPath())));
                    scriptFileName = fileName;
                } catch (IOException e) {
                    return;
                }
            }
        }
    }

    private class MessagePublisher extends TwoArgFunction{
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            if (arg2.istable()) {
                SimulationMessage message = new SimulationMessage((LuaTable) arg2);
                publish(message);
            }
            return LuaValue.NIL;
        }
    }
}
