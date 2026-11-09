# Simulator

This simulator mimics the memory-management tasks of an operating system.

## MemoryManager Class

- `void start(Program program)`: Allocate one or more physical blocks in main memory for the new program.
- `void write(Program program, int section, int logical_address)`: The program intends to write to one of its sections at the given logical address.
- `void read(Program program, int section, int logical_address)`: The program intends to read from one of its sections at the given logical address.
- `void sleep(Program program)`: The program indicates it is going to sleep. During this period it may be relocated to secondary memory.
- `void awake(Program program)`: The program indicates it is waking up. It must be restored to main memory.
- `void end(Program program)`: The program notifies that it will finish execution so the physical memory blocks allocated to it can be freed.

## Memory Class

- `void allocate(Program owner, Block newBlock)`: Allocate a block of memory for the program.
- `void free(Program owner, Block block2remove)`: Free a memory block.
- `void read(Program program, int physical_address)`: Read from a physical memory address. The program must own a memory block that contains this address.
- `void write(Program program, int physical_address)`: Write to a physical memory address. The program must own a memory block that contains this address.
