/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/26/2021

    Breakout
    Simulation/SimulationEvent.java
 */

package Simulation;

public interface SimulationEventHandler<T extends SimulationEvent> {
    void run(T event);
}
