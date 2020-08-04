package eigencraft.cpuArchMod;


import eigencraft.cpuArchMod.block.PipeContainer;
import eigencraft.cpuArchMod.block.ProgrammableNodeContainer;
import eigencraft.cpuArchMod.simulation.SimulationWorldProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CpuArchMod implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "cpu_arch_mod";

    //TODO add better item image
    public static final ItemGroup CPU_ARCH_MOD_ITEMGROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "items"),
            () -> ItemStack.EMPTY);

    public static final Block PIPE = new PipeContainer();
    public static final Block PROGRAMMABLE_NODE = new ProgrammableNodeContainer();

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "pipe"), PIPE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "pipe"), new BlockItem(PIPE, new Item.Settings().group(CPU_ARCH_MOD_ITEMGROUP)));
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "node"), PROGRAMMABLE_NODE);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "node"), new BlockItem(PROGRAMMABLE_NODE, new Item.Settings().group(CPU_ARCH_MOD_ITEMGROUP)));

        ServerStopCallback.EVENT.register(new ServerStopCallback() {
            @Override
            public void onStopServer(MinecraftServer minecraftServer) {
                for (ServerWorld world:minecraftServer.getWorlds()){
                    ((SimulationWorldProvider)world).stopSimulation();
                }
            }
        });
    }

    @Override
    public void onInitializeClient() {
    }
}


