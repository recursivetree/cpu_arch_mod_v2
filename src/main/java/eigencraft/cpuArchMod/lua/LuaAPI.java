package eigencraft.cpuArchMod.lua;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class LuaAPI {
    LuaTable table = new LuaTable();
    private final String name;

    public LuaAPI(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LuaValue asLuaValue() {
        return table;
    }

    public void register(String name, LuaValue value) {
        table.set(LuaString.valueOf(name), value);
    }

    public static class LuaCallback extends TwoArgFunction {
        private LuaValue callback = LuaValue.NIL;

        public LuaValue getCallback() {
            return callback;
        }

        @Override
        public LuaValue call(LuaValue arg, LuaValue arg2) {
            callback = arg2;
            return LuaValue.NIL;
        }
    }
}
