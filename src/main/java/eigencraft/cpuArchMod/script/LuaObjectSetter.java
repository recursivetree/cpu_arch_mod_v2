package eigencraft.cpuArchMod.script;

import org.luaj.vm2.LuaValue;

public class LuaObjectSetter {
    private String name;

    public LuaObjectSetter(String name,LuaValue object) {
        this.name = name;
        this.object = object;
    }

    public String getName() {
        return name;
    }

    public LuaValue getObject() {
        return object;
    }

    protected LuaValue object;
}
