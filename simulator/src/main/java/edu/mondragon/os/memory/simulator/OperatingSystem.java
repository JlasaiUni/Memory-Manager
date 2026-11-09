package edu.mondragon.os.memory.simulator;

import java.util.ArrayList;
import java.util.List;

public class OperatingSystem implements API {

    private MemoryManager memoryManager;
    private List<Program> programs;

    public OperatingSystem(Memory mainMemory, Memory secondaryMemory) {

        programs = new ArrayList<Program>();
        memoryManager = new MemoryManager(mainMemory, secondaryMemory);
    }

    public void startProgram(Program program) {

        System.out.println("Starting " + program.getName());
        try {
            memoryManager.start(program);
            programs.add(program);
            program.start();
        } catch (MemoryManagerException e) {
            handleMemoryManagerException(program, e);
        } catch (Exception e) {
            handleOtherException(program, e);
        }
    }

    public void halt() throws InterruptedException {

        System.out.println("OS halting");

        for (Program program : programs) {
            program.interrupt();
        }

        for (Program program : programs) {
            program.join();
        }
    }

    @Override
    public void write(Program program, int section, int logical_address) {

        try {
            memoryManager.write(program, section, logical_address);
        } catch (MemoryManagerException e) {
            handleMemoryManagerException(program, e);
        } catch (Exception e) {
            handleOtherException(program, e);
        }
    }

    @Override
    public void read(Program program, int section, int logical_address) {

        try {
            memoryManager.read(program, section, logical_address);
        } catch (MemoryManagerException e) {
            handleMemoryManagerException(program, e);
        } catch (Exception e) {
            handleOtherException(program, e);
        }
    }

    @Override
    public void sleep(Program program) {

        System.out.println(program.getName() + " sleeping");
        try {
            memoryManager.sleep(program);
        } catch (MemoryManagerException e) {
            handleMemoryManagerException(program, e);
        } catch (Exception e) {
            handleOtherException(program, e);
        }
    }

    @Override
    public void awake(Program program) {

        System.out.println(program.getName() + " awaken");
        try {
            memoryManager.awake(program);
        } catch (MemoryManagerException e) {
            handleMemoryManagerException(program, e);
        } catch (Exception e) {
            handleOtherException(program, e);
        }
    }

    @Override
    public void end(Program program) {

        System.out.println(program.getName() + " ending");
        try {
            memoryManager.end(program);
        } catch (MemoryManagerException e) {
            handleMemoryManagerException(program, e);
        } catch (Exception e) {
            handleOtherException(program, e);
        }
    }

    private void handleMemoryManagerException(Program program, Exception e) {
        System.out.println("\t✅ Exception handled by the MemoryManager:\n\t" + e.getMessage());
        program.interrupt();
    }

    private void handleOtherException(Program program, Exception e) {
        System.out.println("\t❌ Exception not handled:\n\t" + e.getClass().getCanonicalName()
                + "\n\t" + e.getMessage());
        program.interrupt();
    }
}
