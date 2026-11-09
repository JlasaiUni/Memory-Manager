package edu.mondragon.os.memory.simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Black-box tests of the MemoryManager: they only observe the calls that reach
 * main memory.
 */
public class MemoryManagerTest {

    private static final int MEMORY_SIZE = 4000;

    private static class Access {
        final Program program;
        final int address;

        Access(Program program, int address) {
            this.program = program;
            this.address = address;
        }
    }

    private static class RecordingMemory extends Memory {
        final List<Access> accesses = new ArrayList<>();

        RecordingMemory(int size, int speed, String name) {
            super(size, speed, name);
        }

        @Override
        public void read(Program program, int physical_address) throws MemoryException {
            super.read(program, physical_address);
            accesses.add(new Access(program, physical_address));
        }

        @Override
        public void write(Program program, int physical_address) throws MemoryException {
            super.write(program, physical_address);
            accesses.add(new Access(program, physical_address));
        }

        Set<Integer> addressesOf(Program program) {
            Set<Integer> result = new HashSet<>();
            for (Access access : accesses) {
                if (access.program == program)
                    result.add(access.address);
            }
            return result;
        }
    }

    private RecordingMemory mainMemory;
    private MemoryManager manager;

    @BeforeEach
    public void setUp() {
        mainMemory = new RecordingMemory(MEMORY_SIZE, 1000, "Main");
        manager = new MemoryManager(mainMemory, new Memory(10000, 1000, "Secondary"));
    }

    private Program newProgram(char pid) {
        return new Program(pid, null, ProgramType.BENIGN);
    }

    private int[] sampleAddresses(Program program, int section) {
        int size = program.getSectionSizes()[section];
        return new int[] { 0, 1, size / 2, size - 1 };
    }

    @Test
    public void validAccessesReachMainMemory() throws Exception {
        Program program = newProgram('A');
        manager.start(program);

        int accesses = 0;
        for (int section = 0; section < program.getSectionSizes().length; section++) {
            for (int address : sampleAddresses(program, section)) {
                manager.read(program, section, address);
                manager.write(program, section, address);
                accesses += 2;
            }
        }

        Assertions.assertEquals(accesses, mainMemory.accesses.size(),
                "Level 1 (allocation and mapping): every read and write inside a section must be"
                        + " translated into an access to a physical address in main memory");
    }

    @Test
    public void logicalAddressesMapToDifferentPhysicalAddresses() throws Exception {
        Program program = newProgram('A');
        manager.start(program);

        int[] addresses = sampleAddresses(program, 0);
        for (int address : addresses)
            manager.read(program, 0, address);

        Assertions.assertEquals(addresses.length, mainMemory.addressesOf(program).size(),
                "Level 1 (mapping): different logical addresses of a section must map to different"
                        + " physical addresses");
    }

    @Test
    public void programsDoNotShareMemory() throws Exception {
        Program programA = newProgram('A');
        Program programB = newProgram('B');
        manager.start(programA);
        manager.start(programB);

        for (int address : sampleAddresses(programA, 0))
            manager.write(programA, 0, address);
        for (int address : sampleAddresses(programB, 0))
            manager.write(programB, 0, address);

        Set<Integer> shared = mainMemory.addressesOf(programA);
        shared.retainAll(mainMemory.addressesOf(programB));
        Assertions.assertTrue(!mainMemory.addressesOf(programA).isEmpty()
                && !mainMemory.addressesOf(programB).isEmpty() && shared.isEmpty(),
                "Level 1 (protection): each program must be mapped to its own physical memory, and"
                        + " two programs cannot access the same physical address");
    }

    @Test
    public void outOfBoundsAccessesAreRejected() throws Exception {
        Program program = newProgram('A');
        manager.start(program);
        int size = program.getSectionSizes()[0];

        for (int address : new int[] { size, size + 1, size + 499 }) {
            Assertions.assertThrows(MemoryManagerException.class, () -> manager.read(program, 0, address),
                    "Level 1 (protection): a read at logical address " + address + " of a section of size "
                            + size + " must throw a MemoryManagerException");
            Assertions.assertThrows(MemoryManagerException.class, () -> manager.write(program, 0, address),
                    "Level 1 (protection): a write at logical address " + address + " of a section of size "
                            + size + " must throw a MemoryManagerException");
        }
        Assertions.assertTrue(mainMemory.accesses.isEmpty(),
                "Level 1 (protection): a rejected access must not reach main memory");
    }

    @Test
    public void endedProgramsReleaseTheirMemory() throws Exception {
        // 20 programs of at least 400 words each cannot be resident at the same time.
        for (int i = 0; i < 20; i++) {
            Program program = newProgram((char) ('A' + i));
            manager.start(program);
            manager.read(program, 0, 0);
            manager.end(program);
        }
        Assertions.assertEquals(20, mainMemory.accesses.size(),
                "Level 1 (allocation): every started program must be able to access its memory");
    }

    @Test
    public void programCanAccessMemoryAfterSleepAndAwake() throws Exception {
        Program program = newProgram('A');
        manager.start(program);
        manager.sleep(program);
        manager.awake(program);

        int size = program.getSectionSizes()[0];
        manager.read(program, 0, 0);
        manager.write(program, 0, size - 1);
        Assertions.assertEquals(2, mainMemory.accesses.size(),
                "Level 3 (virtual memory): after awake() the program must be restored in main memory"
                        + " and keep access to all its logical addresses");
    }
}
