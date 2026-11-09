# Homework 3: Memory management

Form teams of 4 or 5 students to implement the memory manager (`MemoryManager.java`) for the simulator. Use the project to practice the concepts of the memory management presentation: the individual MUdle quiz is based on them.

## Deliverables

- Upload the implemented `MemoryManager.java` (and any additional classes) to MUdle.

## Constraints

- If you want to modify any of the provided classes (apart from `MemoryManager.java`), inform the instructor.
- Only one version of the solution may be submitted.
- Programs (threads) are not allowed to enter the WAITING state. This implies that the use of acquire/wait/await (or similar blocking synchronization operations) is not permitted.
- You cannot catch MemoryExceptions; you must prevent them from occurring.

## Evaluation

The project is evaluated as pass/no pass. Every member of a team that passes can take the individual MUdle quiz. The project passes if your memory manager reaches at least Level 1 and runs without MemoryExceptions.

## Levels

The levels match the three levels of the quiz. Aim as high as you can: each level exercises the concepts that the quiz asks about.

- Level 1: Implement a basic algorithm to allocate memory for programs. You will need a logical-to-physical address mapping. Memory access must be protected so that each program cannot access memory allocated for others. An access outside the bounds of a section must be rejected by throwing a `MemoryManagerException`.
- Level 2: Use segmentation or paging techniques.
- Level 3: Use at least one advanced memory-management technique, for example:
  - segmentation + paging
  - virtual memory
  - ...
