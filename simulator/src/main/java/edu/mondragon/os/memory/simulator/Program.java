package edu.mondragon.os.memory.simulator;

import java.util.Random;

enum ProgramType {
    BENIGN, MALICIOUS
}


public class Program extends Thread {

    private Random rand;
    private int section_sizes[];
    private char pid;
    private API api;
    private ProgramType programType;

    public Program(char pid, API api, ProgramType programType) {
        super("Program " + pid + (programType == ProgramType.MALICIOUS ? " (M)" : ""));
        this.pid = pid;
        this.api = api;
        this.programType = programType;
        rand = new Random();
        // Increase number of sections for segmentation
        section_sizes = new int[1];
        section_sizes[0] = rand.nextInt(400, 1000);
    }

    @Override
    public void run() {

        while (!Thread.interrupted()) {
            try {
                Thread.sleep(rand.nextInt(1000));
                int action = rand.nextInt(100);
                if (action < 80) {
                    int section = rand.nextInt(section_sizes.length);
                    int logical_address;
                    if (programType == ProgramType.BENIGN) {
                        logical_address = rand.nextInt(section_sizes[section]);
                    } else {
                        // Malicious programs try to access out of bounds addresses
                        logical_address =
                                rand.nextInt(section_sizes[section], section_sizes[section] + 500);
                    }
                    if (action < 40)
                        api.read(this, section, logical_address);
                    else
                        api.write(this, section, logical_address);
                } else if (action < 90) {
                    api.sleep(this);
                    Thread.sleep(rand.nextInt(1000));
                    api.awake(this);
                } else {
                    interrupt();
                }
            } catch (InterruptedException e) {
                interrupt();
            }
        }
        api.end(this);
    }

    public char getPid() {
        return pid;
    }

    public int[] getSectionSizes() {
        return section_sizes;
    }
}
