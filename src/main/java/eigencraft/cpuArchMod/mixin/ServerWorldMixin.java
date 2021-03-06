package eigencraft.cpuArchMod.mixin;

import eigencraft.cpuArchMod.simulation.SimulationWorld;
import eigencraft.cpuArchMod.simulation.SimulationWorldInterface;
import eigencraft.cpuArchMod.simulation.SimulationWorldRunnable;
import eigencraft.cpuArchMod.simulation.WorldRunnable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;


@Mixin(ServerWorld.class)
public class ServerWorldMixin implements SimulationWorldInterface {

    private SimulationWorld simulationWorld;

    public void addSimulationWorldTask(SimulationWorldRunnable runnable) {
        simulationWorld.addTask(runnable);
    }

    public void stopSimulation() {
        simulationWorld.stop();
    }

    @Override
    public Queue<WorldRunnable> getMainGameThreadTasks() {
        return simulationWorld.getMainGameThreadTasks();
    }

    @Inject(method = "<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorage$Session;Lnet/minecraft/world/level/ServerWorldProperties;Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionType;Lnet/minecraft/server/WorldGenerationProgressListener;Lnet/minecraft/world/gen/chunk/ChunkGenerator;ZJLjava/util/List;Z)V", at = @At("RETURN"))
    public void constructor(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> registryKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean bl, long l, List<Spawner> list, boolean bl2, CallbackInfo ci) {
        simulationWorld = new SimulationWorld(session.getWorldDirectory(registryKey), (ServerWorld) (Object) this,registryKey.getValue().toString());
    }
}