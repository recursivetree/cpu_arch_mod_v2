package eigencraft.cpuArchMod.simulation;

import java.util.List;
import java.util.Queue;

public interface SimulationWorldInterface {
    void addSimulationWorldTask(SimulationWorldRunnable runnable);

    void stopSimulation();

    Queue<WorldRunnable> getMainGameThreadTasks();
}
