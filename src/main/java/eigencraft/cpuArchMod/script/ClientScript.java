package eigencraft.cpuArchMod.script;

import java.util.UUID;

public class ClientScript extends Script{
    public SCRIPT_TYPE getType() {
        return type;
    }

    public static enum SCRIPT_TYPE{
        CLIENTSIDE,
        SERVERSIDE
    }

    private SCRIPT_TYPE type;

    public ClientScript(String name, UUID uuid, SCRIPT_TYPE type) {
        super(name, uuid);
        this.type = type;
    }
}
