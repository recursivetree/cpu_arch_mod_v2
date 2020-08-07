package eigencraft.cpuArchMod.script;

import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

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

    LuaValue chunk;
    Globals user_globals = new Globals();
    LuaScriptWatchDog watchDog;

    public void compileCode(String script, int maxTime){
        Globals user_globals = new Globals();
        user_globals.load(new JseBaseLib());
        user_globals.load(new PackageLib());
        user_globals.load(new Bit32Lib());
        user_globals.load(new TableLib());
        user_globals.load(new StringLib());
        user_globals.load(new JseMathLib());

        watchDog = new LuaScriptWatchDog(maxTime);
        user_globals.load(watchDog);
        user_globals.set("debug", LuaValue.NIL);

        chunk = server_globals.load(script, "main", user_globals);
    }

    public void execute() throws LuaScriptError {
        watchDog.resetTimer();
        LuaThread thread = new LuaThread(user_globals, chunk);
        Varargs result = thread.resume(LuaValue.NIL);
        if (result.arg(1) == LuaValue.FALSE){
            throw new LuaScriptError(result.arg(2).tojstring());
        }
    }

    private static class LuaScriptWatchDog extends DebugLib{
        private long startTime = System.currentTimeMillis();
        int maxTime;

        public LuaScriptWatchDog(int maxTime){
            this.maxTime = maxTime;
        }

        @Override
        public void onInstruction(int pc, Varargs v, int top){
            if (System.currentTimeMillis()-startTime>maxTime){
                throw new WatchDogError(String.format("More than %d instructions",500));
            }
        }

        public void resetTimer() {
            startTime = System.currentTimeMillis();
        }
    }

    private static class WatchDogError extends Error{
        public WatchDogError(String format) {
            super(format);
        }
    }

    public static class LuaScriptError extends Exception{
        public LuaScriptError(String format) {
            super(format);
        }
    }
}
