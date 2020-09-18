package eigencraft.cpuArchMod.networking;

import eigencraft.cpuArchMod.CpuArchMod;
import net.minecraft.util.Identifier;

public class CpuArchModPackets {
    public static final Identifier PROGRAMMABLE_AGENT_OPEN_GUI_S2C_PACKET = new Identifier(CpuArchMod.MOD_ID, "prg_agent_gui_s2c");
    public static final Identifier PROGRAMMABLE_AGENT_CONFIG_C2S_PACKET = new Identifier(CpuArchMod.MOD_ID, "prg_agent_conf_c2s");
    public static final Identifier SCRIPT_DOWNLOAD_S2C = new Identifier(CpuArchMod.MOD_ID, "download_script_s2c");
    public static final Identifier SCRIPT_REQUEST_C2S = new Identifier(CpuArchMod.MOD_ID, "request_script_s2c");
    public static final Identifier SCRIPT_SYNC_S2C = new Identifier(CpuArchMod.MOD_ID, "sync_scripts_s2c");
}
