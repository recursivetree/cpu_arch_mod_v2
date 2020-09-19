package eigencraft.cpuArchMod.simulation.agents;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import eigencraft.cpuArchMod.CpuArchMod;
import eigencraft.cpuArchMod.block.ProgrammableAgentBlockEntity;
import eigencraft.cpuArchMod.block.ProgrammableAgentContainerBlock;
import eigencraft.cpuArchMod.lua.LuaAPI;
import eigencraft.cpuArchMod.lua.LuaScript;
import eigencraft.cpuArchMod.script.Script;
import eigencraft.cpuArchMod.script.ServerScriptManager;
import eigencraft.cpuArchMod.simulation.DynamicAgent;
import eigencraft.cpuArchMod.simulation.SimulationMessage;
import eigencraft.cpuArchMod.simulation.SimulationWorld;
import eigencraft.cpuArchMod.simulation.WorldRunnable;
import io.github.cottonmc.cotton.gui.widget.data.Color;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.IOException;
import java.util.UUID;

public class ProgrammableAgent extends DynamicAgent {
    private static final Logger LOGGER = LogManager.getLogger(CpuArchMod.MOD_ID);

    LuaScript luaScript = new LuaScript();
    LuaAPI api = new LuaAPI("node");
    LuaAPI.LuaCallback ON_REDSTONE_SIGNAL = new LuaAPI.LuaCallback();
    LuaAPI.LuaCallback ON_MESSAGE = new LuaAPI.LuaCallback();
    LuaAPI.LuaCallback ON_SAVE = new LuaAPI.LuaCallback();
    LuaAPI.LuaCallback ON_LOAD = new LuaAPI.LuaCallback();
    DataSaver DATA_SAVER = new DataSaver();
    private Script script;
    private String scriptSrc = "";
    private String lastError = null;

    public ProgrammableAgent(SimulationWorld world, BlockPos pos) {
        super(world,pos);
        api.register("onRedstoneSignal", ON_REDSTONE_SIGNAL);
        api.register("onMessage", ON_MESSAGE);
        api.register("publish", new MessagePublisher());
        api.register("setRedstonePower", new RedstoneEmitter());
        api.register("setInfoLine",new InfoLineSetter());
        api.register("onSave",ON_SAVE);
        api.register("onConfigurationLoaded",ON_LOAD);
        api.register("save", DATA_SAVER);
        script = ServerScriptManager.NO_SCRIPT;
        luaScript.compileCode("", CpuArchMod.CONFIGURATION.SCRIPT_EXECUTION_TIMEOUT, api,"default");
    }

    public Script getScript(){
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
        try {
            this.scriptSrc = CpuArchMod.SCRIPT_MANAGER.readScript(script);
            luaScript.compileCode(scriptSrc, CpuArchMod.CONFIGURATION.SCRIPT_EXECUTION_TIMEOUT, api, script.getName());
            setText(script.getName(),Color.LIME_DYE.toRgb());
            if (CpuArchMod.CONFIGURATION.RESET_SCRIPT_CONFIGURATION_WHEN_CHANGED) {
                DATA_SAVER.reset();
            }
        } catch (IOException  | NullPointerException e) {
            LOGGER.error(String.format("Failed to load file %s: %s",script.getName(),e));
            this.script = ServerScriptManager.NO_SCRIPT;
            setText("Failed to set script",Color.RED_DYE.toRgb());
        } catch (LuaError e){
            handleLuaError(e);
            setText(script.getName(),Color.RED_DYE.toRgb());
            this.script = ServerScriptManager.NO_SCRIPT;
        }
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

    private void handleLuaError(Throwable t){
        lastError = t.getMessage();
        world.addMainGameTask(new WorldRunnable(){
            @Override
            public void run(ServerWorld world) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity != null) {
                    if (blockEntity instanceof ProgrammableAgentBlockEntity) {
                        ((ProgrammableAgentBlockEntity) blockEntity).setAppearance(String.format("Failed: %s",script.getName()), Color.RED_DYE.toRgb());
                    }
                }
            }
        });
    }

    protected void setText(String text, int color){
        world.addMainGameTask(new WorldRunnable(){
            @Override
            public void run(ServerWorld world) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity != null) {
                    if (blockEntity instanceof ProgrammableAgentBlockEntity) {
                        ((ProgrammableAgentBlockEntity) blockEntity).setAppearance(text, color);
                    }
                }
            }
        });
    }

    public void onRedstonePowered() {
        try {
            luaScript.execute(ON_REDSTONE_SIGNAL.getCallback());
        } catch (LuaError | LuaScript.WatchDogError error) {
            handleLuaError(error);
        } catch (NullPointerException ignored){

        }
    }

    @Override
    public void process(SimulationMessage message) {
        try {
            luaScript.execute(ON_MESSAGE.getCallback(), message.getAsLuaValue(), LuaString.valueOf(message.getType()));
        } catch (LuaError | LuaScript.WatchDogError error) {
            error.printStackTrace();
            handleLuaError(error);
        } catch (NullPointerException ignored){

        }
    }

    @Override
    public JsonElement getConfigData() {
        JsonObject root = new JsonObject();

        //Save the scripts
        root.add("script", new JsonPrimitive(script.getUUID().toString()));

        //Call the save callback
        try {
            luaScript.execute(ON_SAVE.getCallback());
        } catch (LuaError | LuaScript.WatchDogError error) {
            error.printStackTrace();
            handleLuaError(error);
        } catch (NullPointerException ignored){

        }

        //if there's anything to store, store it
        if (DATA_SAVER.shouldSave()){
            root.add("scriptData", new JsonPrimitive(DATA_SAVER.getValue()));
        }


        return root;
    }

    @Override
    public void loadConfig(JsonElement rawConfig) {
        if (rawConfig.isJsonObject()) {
            JsonObject config = rawConfig.getAsJsonObject();
            UUID scriptUUID = UUID.fromString(config.get("script").getAsString());
            Script script = CpuArchMod.SCRIPT_MANAGER.getScript(scriptUUID);
            if (script==null) script = new Script(scriptUUID.toString(),scriptUUID);
            setScript(script);

            if (config.has("scriptData")){
                try {
                    luaScript.execute(ON_LOAD.getCallback(), LuaString.valueOf(config.get("scriptData").getAsString()));
                } catch (LuaError | LuaScript.WatchDogError error) {
                    error.printStackTrace();
                    handleLuaError(error);
                } catch (NullPointerException ignored){

                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    private class MessagePublisher extends ThreeArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
            if (arg2.istable()&&arg3.isstring()) {
                SimulationMessage message = new SimulationMessage(arg2,arg3.tojstring());
                publish(message);
            }
            return LuaValue.NIL;
        }
    }

    private class RedstoneEmitter extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg, LuaValue arg2) {
            if (arg2.isboolean()) {
                world.addMainGameTask(world -> {
                    BlockState blockState = world.getBlockState(pos);
                    if (blockState.isOf(CpuArchMod.PROGRAMMABLE_NODE)){
                        world.setBlockState(pos,blockState.with(ProgrammableAgentContainerBlock.POWER,arg2.toboolean()));
                        world.updateNeighborsAlways(pos,CpuArchMod.PROGRAMMABLE_NODE);
                    }
                });
                return LuaValue.TRUE;
            } else {
                return LuaValue.FALSE;
            }
        }
    }
    private class InfoLineSetter extends TwoArgFunction{

        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            if (arg2.isstring()){
                world.addMainGameTask(world -> {
                    BlockEntity entity = world.getBlockEntity(pos);
                    if (entity instanceof ProgrammableAgentBlockEntity){
                        ((ProgrammableAgentBlockEntity)entity).setInfoLine(arg2.tojstring());
                    }
                });
                return LuaValue.TRUE;
            }
            return LuaValue.FALSE;
        }
    }
    private class DataSaver extends TwoArgFunction{
        String luaData = null;
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            if (arg2.isstring()){
                luaData = arg2.tojstring();
            }
            return LuaValue.NIL;
        }

        public String getValue() {
            return luaData;
        }

        public boolean shouldSave() {
            if (luaData==null) return false;
            return !luaData.equals("");
        }

        public void reset() {
            luaData = null;
        }
    }
}
