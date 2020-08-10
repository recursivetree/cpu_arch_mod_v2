package eigencraft.cpuArchMod.simulation;

public interface SimulationWorldInterface {
    void addSimulationWorldTask(SimulationWorldRunnable runnable);

    void stopSimulation();
}
