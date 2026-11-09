package edu.mondragon.os.memory.simulator;

import java.util.Random;

/**
 * simulator
 *
 */
public class App {

    public static final int NUM_PROGRAMS = 10;

    public static void main(String[] args) {

        Random rand = new Random();
        Memory mainMemory = new Memory(4000, 4, "Main");
        Memory secondaryMemory = new Memory(10000, 1, "Secondary");
        OperatingSystem os = new OperatingSystem(mainMemory, secondaryMemory);

        try {
            for (int i = 0; i < NUM_PROGRAMS; i++) {
                ProgramType programType;
                if (rand.nextInt(100) < 70)
                    programType = ProgramType.BENIGN;
                else
                    programType = ProgramType.MALICIOUS;
                Program program = new Program((char) (i + 65), os, programType);
                os.startProgram(program);
                Thread.sleep(rand.nextInt(1000));
            }
            Thread.sleep(rand.nextInt(10000));

            os.halt();
        } catch (InterruptedException e) {
        }
    }
}
