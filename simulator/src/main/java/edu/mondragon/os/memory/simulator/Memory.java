package edu.mondragon.os.memory.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collector;
import java.util.stream.IntStream;

public class Memory {

    private final int printLineWidth = 80;

    private int delay;
    private int size;

    private String name;
    private List<Block> blocks;

    public Memory(int size, int speed, String name) {
        this.size = size;
        this.delay = 1000 / speed;
        this.name = name;
        blocks = new ArrayList<Block>();
        blocks.add(new Block(0, size - 1));
    }

    public void allocate(Program owner, Block newBlock) throws MemoryException {
        // Find a gap that contains the new block
        Block foundGap;
        try {
            foundGap = blocks.stream().filter(block -> !(block instanceof OwnedBlock))
                    .filter(block -> block.contains(newBlock)).findFirst().get();
        } catch (NoSuchElementException e) {
            throw new MemoryException("No gap for " + newBlock);
        }

        modifyBlock(foundGap, newBlock, owner);

        try {
            Thread.sleep(delay);
            System.out.println(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void free(Program owner, Block block2remove) throws MemoryException {
        // Find the owned block that contains the block to remove
        OwnedBlock foundBlock;
        try {
            foundBlock = blocks.stream().filter(block -> block instanceof OwnedBlock)
                    .map(block -> (OwnedBlock) block)
                    .filter(block -> block.owner() == owner && block.contains(block2remove))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new MemoryException("Block not found: " + block2remove);
        }

        modifyBlock(foundBlock, block2remove, null);

        try {
            Thread.sleep(delay);
            System.out.println(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void modifyBlock(Block targetBlock, Block targetRange, Program owner)
            throws MemoryException {
        int blockPos = blocks.indexOf(targetBlock);
        if (blockPos == -1) {
            throw new MemoryException("Target block not found in memory");
        }

        // Remove the target block
        blocks.remove(blockPos);

        // Determine the final range after consolidation
        int finalStart = targetRange.start();
        int finalEnd = targetRange.end();

        // Check and consolidate with previous block
        if (blockPos > 0) {
            Block preBlock = blocks.get(blockPos - 1);
            boolean shouldConsolidate = false;

            if (owner == null) {
                shouldConsolidate = !(preBlock instanceof OwnedBlock);
            } else {
                shouldConsolidate = (preBlock instanceof OwnedBlock)
                        && ((OwnedBlock) preBlock).owner() == owner;
            }

            if (shouldConsolidate && preBlock.end() + 1 == finalStart) {
                finalStart = preBlock.start();
                blocks.remove(blockPos - 1);
                blockPos--;
            }
        }

        // Check and consolidate with next block
        if (blockPos < blocks.size()) {
            Block postBlock = blocks.get(blockPos);
            boolean shouldConsolidate = false;

            if (owner == null) {
                shouldConsolidate = !(postBlock instanceof OwnedBlock);
            } else {
                shouldConsolidate = (postBlock instanceof OwnedBlock)
                        && ((OwnedBlock) postBlock).owner() == owner;
            }

            if (shouldConsolidate && finalEnd + 1 == postBlock.start()) {
                finalEnd = postBlock.end();
                blocks.remove(blockPos);
            }
        }

        // Create the pre-gap/block if there's space before targetRange
        try {
            Block preBlock = new Block(targetBlock.start(), targetRange.start() - 1);
            if (targetBlock instanceof OwnedBlock) {
                blocks.add(blockPos, new OwnedBlock(preBlock.start(), preBlock.end(),
                        ((OwnedBlock) targetBlock).owner()));
            } else {
                blocks.add(blockPos, preBlock);
            }
            blockPos++;
        } catch (IllegalArgumentException e) {
            // No space before, ignore
        }

        // Create the new block (owned or free) with consolidated range
        if (owner == null) {
            blocks.add(blockPos, new Block(finalStart, finalEnd));
        } else {
            blocks.add(blockPos, new OwnedBlock(finalStart, finalEnd, owner));
        }
        blockPos++;

        // Create the post-gap/block if there's space after targetRange
        try {
            Block postBlock = new Block(targetRange.end() + 1, targetBlock.end());
            if (targetBlock instanceof OwnedBlock) {
                blocks.add(blockPos, new OwnedBlock(postBlock.start(), postBlock.end(),
                        ((OwnedBlock) targetBlock).owner()));
            } else {
                blocks.add(blockPos, postBlock);
            }
        } catch (IllegalArgumentException e) {
            // No space after, ignore
        }
    }

    public void read(Program program, int physical_address) throws MemoryException {

        checkOwner(program, physical_address);
        try {
            Thread.sleep(delay);
            System.out.println(getStringAccess(physical_address, program.getName(), 'r'));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void write(Program program, int physical_address) throws MemoryException {

        checkOwner(program, physical_address);
        try {
            Thread.sleep(delay);
            System.out.println(getStringAccess(physical_address, program.getName(), 'w'));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return name + " Memory:\n" + getCharPrograms();
    }

    private void checkOwner(Program owner, int physical_address) throws MemoryException {
        OwnedBlock foundBlock;

        if (owner == null)
            throw new MemoryException("Cannot access empty memory blocks");

        try {
            foundBlock = blocks.stream().filter(block -> block instanceof OwnedBlock)
                    .map(block -> (OwnedBlock) block)
                    .filter(block -> block.contains(physical_address)).findFirst().get();
        } catch (NoSuchElementException e) {
            throw new MemoryException(owner.getName() + " operating out of memory");
        }

        if (foundBlock.owner() != owner)
            throw new MemoryException(owner.getName() + " operating out of owned blocks");
    }

    private String getStringAccess(int physical_address, String program, char action) {

        int marks[] =
                IntStream.range(0, printLineWidth).map(i -> i * size / printLineWidth).toArray();

        String str = IntStream.range(1, marks.length).mapToObj(i -> {
            if (marks[i - 1] <= physical_address && physical_address < marks[i])
                return action;
            return ' ';
        }).collect(Collector.of(StringBuilder::new, StringBuilder::append, StringBuilder::append,
                StringBuilder::toString));

        return program + " " + (action == 'r' ? "reading" : "writing") + " " + this + "\n" + str;
    }

    private String getCharPrograms() {

        return IntStream.range(0, printLineWidth).map(i -> i * size / printLineWidth)
                .mapToObj(i -> {
                    Block foundBlock =
                            blocks.stream().filter(block -> block.contains(i)).findFirst().get();
                    if (foundBlock instanceof OwnedBlock) {
                        OwnedBlock foundOwnedBlock = (OwnedBlock) foundBlock;
                        return foundOwnedBlock.owner().getPid();
                    }
                    return '-';
                }).collect(Collector.of(StringBuilder::new, StringBuilder::append,
                        StringBuilder::append, StringBuilder::toString));
    }
}
