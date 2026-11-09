package edu.mondragon.os.memory.simulator;

public interface API {

    public void write(Program program, int section, int logical_address);

    public void read(Program program, int section, int logical_address);

    public void sleep(Program program);

    public void awake(Program program);

    public void end(Program program);

}
