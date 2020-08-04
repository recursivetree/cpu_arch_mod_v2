package eigencraft.cpuArchMod.simulation;

public interface SimulationWorldProvider {
    public void addSimulationWorldTask(SimulationWorldRunnable runnable);

    void stopSimulation();
}
