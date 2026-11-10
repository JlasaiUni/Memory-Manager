package edu.mondragon.os.memory.simulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryManager {

    private Memory mainMemory;

    // Guarda qué bloque pertenece a cada programa
    private Map<Program, Block> programBlocks;

    // Guarda los huecos libres de memoria
    private List<Block> freeBlocks;

    public MemoryManager(Memory mainMemory, Memory secondaryMemory) {

        this.mainMemory = mainMemory;

        programBlocks = new HashMap<>();

        freeBlocks = new ArrayList<>();

        // Al principio toda la memoria principal está libre
        freeBlocks.add(new Block(0, mainMemory.getSize() - 1));
    }

    public synchronized void start(Program program)
            throws MemoryException, MemoryManagerException {

        // Calculamos cuánto ocupa el programa
        int programSize = 0;
        // FIRST FIT:
        // buscamos el primer hueco donde quepa el programa
        Block selectedGap = null;
        int selectedIndex = -1;
        Block gap;

        for (int sectionSize : program.getSectionSizes()) {
            programSize += sectionSize;
        }

        for (int i = 0; i < freeBlocks.size(); i++) {

            gap = freeBlocks.get(i);

            if (gap.getSize() >= programSize) {
                selectedGap = gap;
                selectedIndex = i;
                break;
            }
        }

        if (selectedGap == null) {

            throw new MemoryManagerException(
                    "Not enough memory for " + program.getName());
        }

        // El programa ocupa un bloque contiguo
        Block programBlock = new Block(
                selectedGap.start(),
                selectedGap.start() + programSize - 1);

        // Quitamos el hueco que vamos a utilizar
        freeBlocks.remove(selectedIndex);

        // Si sobra memoria en ese hueco,
        // guardamos el resto como hueco libre
        if (programBlock.end() < selectedGap.end()) {

            Block remainingGap = new Block(
                    programBlock.end() + 1,
                    selectedGap.end());

            freeBlocks.add(selectedIndex, remainingGap);
        }

        // Guardamos dónde está el programa
        programBlocks.put(program, programBlock);

        // Reservamos realmente ese bloque en Memory
        mainMemory.allocate(program, programBlock);
    }

    public synchronized void write(
            Program program,
            int section,
            int logical_address)
            throws MemoryException, MemoryManagerException {
        int physicalAddress = translate(program, section, logical_address);
        mainMemory.write(program, physicalAddress);
    }

    public synchronized void read(
            Program program,
            int section,
            int logical_address)
            throws MemoryException, MemoryManagerException {

        int physicalAddress = translate(program, section, logical_address);

        mainMemory.read(program, physicalAddress);
    }

    public synchronized void sleep(Program program)
            throws MemoryException, MemoryManagerException {


    }

    public synchronized void awake(Program program)
            throws MemoryException, MemoryManagerException {


    }

    public synchronized void end(Program program)
            throws MemoryException, MemoryManagerException {

        // Buscamos el bloque del programa
        Block programBlock = programBlocks.remove(program);

        if (programBlock == null) {

            throw new MemoryManagerException(
                    program.getName() + " has no allocated memory");
        }

        // Liberamos realmente la memoria
        mainMemory.free(program, programBlock);

        // Nuestro MemoryManager también debe recordar
        // que ese espacio vuelve a estar libre
        freeBlocks.add(programBlock);

        // Unimos huecos libres consecutivos
        mergeFreeBlocks();
    }

    private int translate(
            Program program,
            int section,
            int logicalAddress)
            throws MemoryManagerException {

        Block programBlock = programBlocks.get(program);

        if (programBlock == null) {

            throw new MemoryManagerException(
                    program.getName() + " has no allocated memory");
        }

        int[] sectionSizes = program.getSectionSizes();

        // Comprobar que la sección existe
        if (section < 0 || section >= sectionSizes.length) {

            throw new MemoryManagerException(
                    "Invalid section " + section);
        }

        // PROTECTION:
        // No permitir salir fuera de la sección
        if (logicalAddress < 0
                || logicalAddress >= sectionSizes[section]) {

            throw new MemoryManagerException(
                    "Logical address "
                            + logicalAddress
                            + " outside section "
                            + section);
        }

        // Calculamos dónde empieza esta sección
        // dentro del bloque del programa
        int sectionOffset = 0;

        for (int i = 0; i < section; i++) {
            sectionOffset += sectionSizes[i];
        }

        // LOGICAL -> PHYSICAL
        return programBlock.start()
                + sectionOffset
                + logicalAddress;
    }

    private void mergeFreeBlocks() {

        // Ordenamos los huecos por dirección
        freeBlocks.sort(
                Comparator.comparingInt(Block::start));

        List<Block> merged = new ArrayList<>();

        for (Block block : freeBlocks) {

            if (merged.isEmpty()) {

                merged.add(block);

            } else {

                Block previous = merged.get(merged.size() - 1);

                // Si los dos huecos están juntos,
                // los convertimos en uno
                if (previous.end() + 1 == block.start()) {

                    Block joined = new Block(
                            previous.start(),
                            block.end());

                    merged.set(
                            merged.size() - 1,
                            joined);

                } else {

                    merged.add(block);
                }
            }
        }

        freeBlocks = merged;
    }
}