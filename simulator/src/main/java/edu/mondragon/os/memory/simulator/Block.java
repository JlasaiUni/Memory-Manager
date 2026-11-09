package edu.mondragon.os.memory.simulator;

public class Block {

    private final int start;
    private final int end;

    public Block(int start, int end) {

        if (end < start) {
            throw new IllegalArgumentException("end < start: " + end + " < " + start);
        }
        this.start = start;
        this.end = end;
    }

    public boolean contains(Block blockB) {
        return start <= blockB.start() && blockB.end() <= end;
    }

    public boolean contains(int address) {
        return start <= address && address <= end;
    }

    public int getSize() {
        return end - start + 1;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }
}
