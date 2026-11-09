package edu.mondragon.os.memory.simulator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit test for Memory.
 */
public class MemoryTest {

    @Test
    public void checkMemory() throws MemoryException {

        Memory mainMemory = new Memory(4000, 100, "Main");
        Memory secondaryMemory = new Memory(10000, 1000, "Secondary");

        OperatingSystem os = new OperatingSystem(mainMemory, secondaryMemory);

        Program programA = new Program('A', os, ProgramType.BENIGN);
        Program programB = new Program('B', os, ProgramType.BENIGN);
        Block block1 = new Block(0, 999);
        mainMemory.allocate(programA, block1);
        Assertions.assertThrows(MemoryException.class, () -> {
            Block block2 = new Block(999, 1999);
            mainMemory.allocate(programB, block2);
        });
        Block block3 = new Block(3999, 3999);
        mainMemory.allocate(programB, block3);

        Assertions.assertThrows(MemoryException.class, () -> {
            mainMemory.write(programB, 500);
        });
    }

    @Test
    public void testPartialAllocation() throws MemoryException {
        Memory mainMemory = new Memory(1000, 100, "Main");
        OperatingSystem os = new OperatingSystem(mainMemory, null);

        Program programA = new Program('A', os, ProgramType.BENIGN);

        // Allocate part of memory
        Block block1 = new Block(100, 299);
        mainMemory.allocate(programA, block1);

        // Allocate another part (non-contiguous)
        Block block2 = new Block(400, 599);
        mainMemory.allocate(programA, block2);

        // Verify programA can write to both blocks
        mainMemory.write(programA, 150);
        mainMemory.write(programA, 500);

        // Verify gaps still exist
        Block gapBlock = new Block(300, 399);
        Program programB = new Program('B', os, ProgramType.BENIGN);
        mainMemory.allocate(programB, gapBlock);

        mainMemory.write(programB, 350);
    }

    @Test
    public void testPartialFreeing() throws MemoryException {
        Memory mainMemory = new Memory(1000, 100, "Main");
        OperatingSystem os = new OperatingSystem(mainMemory, null);

        Program programA = new Program('A', os, ProgramType.BENIGN);

        // Allocate a large block
        Block largeBlock = new Block(100, 599);
        mainMemory.allocate(programA, largeBlock);

        // Free only a portion in the middle
        Block partialFree = new Block(250, 449);
        mainMemory.free(programA, partialFree);

        // ProgramA should still be able to access the remaining parts
        mainMemory.write(programA, 150); // Before freed region
        mainMemory.write(programA, 550); // After freed region

        // But not the freed region
        Assertions.assertThrows(MemoryException.class, () -> {
            mainMemory.write(programA, 350);
        });

        // Another program should be able to use the freed region
        Program programB = new Program('B', os, ProgramType.BENIGN);
        Block newBlock = new Block(300, 399);
        mainMemory.allocate(programB, newBlock);
        mainMemory.write(programB, 350);
    }

    @Test
    public void testBlockConsolidationOnFree() throws MemoryException {
        Memory mainMemory = new Memory(1000, 100, "Main");
        OperatingSystem os = new OperatingSystem(mainMemory, null);

        Program programA = new Program('A', os, ProgramType.BENIGN);
        Program programB = new Program('B', os, ProgramType.BENIGN);
        Program programC = new Program('C', os, ProgramType.BENIGN);

        // Allocate three adjacent blocks to different programs
        Block block1 = new Block(100, 299);
        Block block2 = new Block(300, 499);
        Block block3 = new Block(500, 699);

        mainMemory.allocate(programA, block1);
        mainMemory.allocate(programB, block2);
        mainMemory.allocate(programC, block3);

        // Free the middle block
        mainMemory.free(programB, block2);

        // Free the first block - should consolidate with the freed middle block
        mainMemory.free(programA, block1);

        // Now we should be able to allocate a larger block spanning both freed regions
        Program programD = new Program('D', os, ProgramType.BENIGN);
        Block largeBlock = new Block(100, 499);
        mainMemory.allocate(programD, largeBlock);

        mainMemory.write(programD, 300);
    }

    @Test
    public void testBlockConsolidationOnAllocation() throws MemoryException {
        Memory mainMemory = new Memory(1000, 100, "Main");
        OperatingSystem os = new OperatingSystem(mainMemory, null);

        Program programA = new Program('A', os, ProgramType.BENIGN);

        // Allocate two non-adjacent blocks
        Block block1 = new Block(100, 299);
        Block block2 = new Block(400, 599);

        mainMemory.allocate(programA, block1);
        mainMemory.allocate(programA, block2);

        // Allocate the gap between them to the same program - should consolidate
        Block gapBlock = new Block(300, 399);
        mainMemory.allocate(programA, gapBlock);

        // Now programA should have one contiguous block from 100 to 599
        mainMemory.write(programA, 150);
        mainMemory.write(programA, 350);
        mainMemory.write(programA, 550);
    }

    @Test
    public void testFreeingEdgeCases() throws MemoryException {
        Memory mainMemory = new Memory(1000, 100, "Main");
        OperatingSystem os = new OperatingSystem(mainMemory, null);

        Program programA = new Program('A', os, ProgramType.BENIGN);

        // Allocate a block
        Block block1 = new Block(100, 599);
        mainMemory.allocate(programA, block1);

        // Free from the beginning
        Block partialFree1 = new Block(100, 199);
        mainMemory.free(programA, partialFree1);

        // ProgramA can still access the rest
        mainMemory.write(programA, 300);

        // Free from the end
        Block partialFree2 = new Block(500, 599);
        mainMemory.free(programA, partialFree2);

        // ProgramA can still access the middle
        mainMemory.write(programA, 300);

        // Cannot access freed parts
        Assertions.assertThrows(MemoryException.class, () -> {
            mainMemory.write(programA, 150);
        });
        Assertions.assertThrows(MemoryException.class, () -> {
            mainMemory.write(programA, 550);
        });
    }
}
