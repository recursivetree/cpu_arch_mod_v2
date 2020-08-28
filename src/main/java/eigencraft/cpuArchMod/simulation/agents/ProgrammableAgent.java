package eigencraft.cpuArchMod.simulation.agents;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import eigencraft.cpuArchMod.CpuArchMod;
import eigencraft.cpuArchMod.block.ProgrammableAgentBlockEntity;
import eigencraft.cpuArchMod.lua.LuaAPI;
import eigencraft.cpuArchMod.lua.LuaScript;
import eigencraft.cpuArchMod.simulation.DynamicAgent;
import eigencraft.cpuArchMod.simulation.SimulationMessage;
import eigencraft.cpuArchMod.simulation.SimulationWorld;
import eigencraft.cpuArchMod.simulation.WorldRunnable;
import io.github.cottonmc.cotton.gui.widget.data.Color;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.IOException;

public class ProgrammableAgent extends DynamicAgent {
    private static final Logger LOGGER = LogManager.getLogger(CpuArchMod.MOD_ID);

    LuaScript luaScript = new LuaScript();
    LuaAPI api = new LuaAPI("node");
    LuaAPI.LuaCallback ON_REDSTONE_SIGNAL = new LuaAPI.LuaCallback();
    LuaAPI.LuaCallback ON_MESSAGE = new LuaAPI.LuaCallback();
    private String scriptFileName = "unconfigured";
    private String scriptSrc = "";
    private String lastError = null;

    public ProgrammableAgent(SimulationWorld world, BlockPos pos) {
        super(world,pos);
        api.register("onRedstoneSignal", ON_REDSTONE_SIGNAL);
        api.register("onMessage", ON_MESSAGE);
        api.register("publish", new MessagePublisher());
        luaScript.compileCode("", CpuArchMod.CONFIGURATION.SCRIPT_EXECUTION_TIMEOUT, api,"default");
    }

    public String getScriptFileName() {
        return scriptFileName;
    }

    public void setScriptFileName(String scriptFileName) {
        this.scriptFileName = scriptFileName;
    }

    public String getScriptSrc() {
        return scriptSrc;
    }

    public String getErrorLog() {
        return (lastError == null) ? "" : lastError;
    }

    public void resetErrorLog() {
        lastError = null;
    }

    public void setScriptSrc(String scriptSrc) {
        this.scriptSrc = scriptSrc;
        try {
            luaScript.compileCode(scriptSrc, CpuArchMod.CONFIGURATION.SCRIPT_EXECUTION_TIMEOUT, api, scriptFileName);
        } catch (LuaError luaError) {
            luaError.printStackTrace();
            handleLuaError(luaError);
        }
    }

    private void handleLuaError(Throwable t){
        lastError = t.getMessage();
        world.addMainGameTask(new WorldRunnable(){
            @Override
            public void run(ServerWorld world) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity != null) {
                    if (blockEntity instanceof ProgrammableAgentBlockEntity) {
                        ((ProgrammableAgentBlockEntity) blockEntity).setAppearance(String.format("Failed: %s",scriptFileName), Color.RED_DYE.toRgb());
                    }
                }
            }
        });
    }

    public void onRedstonePowered() {
        try {
            luaScript.execute(ON_REDSTONE_SIGNAL.getCallback());
        } catch (LuaError | LuaScript.WatchDogError error) {
            error.printStackTrace();
            handleLuaError(error);
        }
    }

    @Override
    public void process(SimulationMessage message) {
        try {
            luaScript.execute(ON_MESSAGE.getCallback(), message.getAsLuaValue());
        } catch (LuaError | LuaScript.WatchDogError error) {
            error.printStackTrace();
            handleLuaError(error);
        }
    }

    @Override
    public JsonElement getConfigData() {
        JsonObject root = new JsonObject();
        root.add("file", new JsonPrimitive(scriptFileName));
        return root;
    }

    @Override
    public void loadConfig(JsonElement rawConfig) {
        if (rawConfig.isJsonObject()) {
            JsonObject config = rawConfig.getAsJsonObject();
            String fileName = config.get("file").getAsString();
            if (!fileName.equals("unconfigured")) {
                try {
                    setScriptSrc(CpuArchMod.SCRIPT_MANAGER.readFile(fileName));
                    scriptFileName = fileName;
                } catch (IOException  | NullPointerException e) {
                    LOGGER.error(String.format("Failed to load file %s: %s",fileName,e));
                    return;
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    private class MessagePublisher extends TwoArgFunction {
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
