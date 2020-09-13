package eigencraft.cpuArchMod.simulation;


import org.luaj.vm2.LuaValue;

public class SimulationMessage {
    private final LuaValue dataObject;
    private final String type;

    public SimulationMessage(LuaValue dataObject,String type) {
        this.dataObject = dataObject;
        this.type = type;
    }

    public LuaValue getAsLuaValue() {
        return dataObject;
    }


    public String getType() {
        return type;
    }
}
