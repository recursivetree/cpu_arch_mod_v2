package eigencraft.cpuArchMod.lua;

import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

public class LuaScript {
    static Globals server_globals;

    static {
        server_globals = new Globals();
        server_globals.load(new JseBaseLib());
        server_globals.load(new PackageLib());

        server_globals.load(new JseMathLib());
        LoadState.install(server_globals);
        LuaC.install(server_globals);
    }


    Globals user_globals = new Globals();
    LuaScriptWatchDog watchDog;

    public void compileCode(String script, int maxTime, LuaAPI api) {
        user_globals = new Globals();
        user_globals.load(new JseBaseLib());
        user_globals.load(new PackageLib());
        user_globals.load(new Bit32Lib());
        user_globals.load(new TableLib());
        user_globals.load(new StringLib());
        user_globals.load(new JseMathLib());

        watchDog = new LuaScriptWatchDog(maxTime);
        user_globals.load(watchDog);
        user_globals.set("debug", LuaValue.NIL);

        user_globals.set(api.getName(), api.asLuaValue());

        LuaValue chunk = server_globals.load(script, "main", user_globals);
        execute(chunk);
    }

    public void execute(LuaValue function) throws LuaError, WatchDogError {
        watchDog.resetTimer();
        function.call();
    }

    public void execute(LuaValue callback, LuaTable arg) {
        watchDog.resetTimer();
        callback.call(arg);
    }

    public static class LuaScriptWatchDog extends DebugLib {
        int maxTime;
        private long startTime = System.currentTimeMillis();

        public LuaScriptWatchDog(int maxTime) {
            this.maxTime = maxTime;
        }

        @Override
        public void onInstruction(int pc, Varargs v, int top) {
            if (System.currentTimeMillis() - startTime > maxTime) {
                throw new WatchDogError(String.format("Callback took longer than %d ms", maxTime));
            }
        }

        public void resetTimer() {
            startTime = System.currentTimeMillis();
        }
    }

    public static class WatchDogError extends Error {
        public WatchDogError(String format) {
            super(format);
        }
    }
}
