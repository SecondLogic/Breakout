/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulationEvent.java
 */


package Simulation;

interface SimulationEventHandler<T extends SimulationEvent> {
    void run(T event);
}

public abstract class SimulationEvent {}
