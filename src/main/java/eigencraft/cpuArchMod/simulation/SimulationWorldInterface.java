package eigencraft.cpuArchMod.simulation;

public interface SimulationWorldInterface {
    public void addSimulationWorldTask(SimulationWorldRunnable runnable);

    void stopSimulation();
}
