package eigencraft.cpuArchMod.script;

import java.util.UUID;

public class Script {
    private UUID id;
    private String name;

    public Script(String name,UUID uuid){
        this.name = name;
        this.id = uuid;
    }

    public UUID getUUID() {
        return id;
    }

    public String getName() {
        return name;
    }
}
