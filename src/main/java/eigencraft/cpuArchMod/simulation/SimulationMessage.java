package eigencraft.cpuArchMod.simulation;


import org.luaj.vm2.LuaTable;

public class SimulationMessage {
    public SimulationMessage(LuaTable dataObject) {
        this.dataObject = dataObject;
    }

    public LuaTable getAsLuaValue() {
        return dataObject;
    }

    private LuaTable dataObject;


}
