package edu.mondragon.os.memory.simulator;

public class MemoryManager {

    private Memory mainMemory;
    // private Memory secondaryMemory;
    // TODO: Your code goes here.

    public MemoryManager(Memory mainMemory, Memory secondaryMemory) {

        this.mainMemory = mainMemory;
        // secondaryMemory = secondaryMemory;

        // TODO: Your code goes here.
    }

    public synchronized void start(Program program) throws MemoryException, MemoryManagerException {

        // TODO: Your code goes here.
    }

    public synchronized void write(Program program, int section, int logical_address)
            throws MemoryException, MemoryManagerException {

        // TODO: Your code goes here. Throw a MemoryManagerException if the logical address is outside the section.
    }

    public synchronized void read(Program program, int section, int logical_address)
            throws MemoryException, MemoryManagerException {

        // TODO: Your code goes here. Throw a MemoryManagerException if the logical address is outside the section.
    }

    public synchronized void sleep(Program program) throws MemoryException, MemoryManagerException {

        // TODO: Your code goes here.
    }

    public synchronized void awake(Program program) throws MemoryException, MemoryManagerException {

        // TODO: Your code goes here.
    }

    public synchronized void end(Program program) throws MemoryException, MemoryManagerException {

        // TODO: Your code goes here.
    }
}
