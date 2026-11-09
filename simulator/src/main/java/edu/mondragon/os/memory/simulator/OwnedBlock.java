package edu.mondragon.os.memory.simulator;

public class OwnedBlock extends Block {

    private final Program owner;

    public OwnedBlock(int start, int end, Program owner) {
        super(start, end);
        this.owner = owner;
    }

    public Program owner() {
        return owner;
    }
}
