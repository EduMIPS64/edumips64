package edumips64.core;

import edumips64.utils.*;
import java.util.*;
import edumips64.core.is.*;

public interface MemoryAccessor{
        public int getMemorySize();
        public int getInstructionsNumber();
        /*Mi pare che in una interfaccia non possono essere messi metodi statici, dunque lo ometto?! 
        public static Memory getInstance();*/
        public int getInstructionIndex(Instruction i);
        public MemoryElement getCell(int address) throws MemoryElementNotFoundException;
        public void reset();
        public String toString();
        public void addInstruction(Instruction i, int address) throws SymbolTableOverflowException;
        public Instruction getInstruction(int address);
        public Instruction getInstruction(BitSet64 address) throws HaltException, IrregularStringOfBitsException;
        public List<Instruction> getInstructions();
        
        public int readB(int address) throws MemoryElementNotFoundException;
        public int readBU(int address) throws MemoryElementNotFoundException;
        public int readH(int address) throws MemoryElementNotFoundException,IrregularStringOfBitsException,NotAlingException;
        public int readHU(int address) throws MemoryElementNotFoundException,NotAlingException;
        public int readW(int address) throws MemoryElementNotFoundException,NotAlingException;
        public long readWU(int address) throws MemoryElementNotFoundException,NotAlingException;
        public String readD(int address) throws MemoryElementNotFoundException;
        
        public void writeB(int address, int data) throws MemoryElementNotFoundException,IrregularWriteOperationException;
        public void writeD(int address, String data) throws MemoryElementNotFoundException,IrregularStringOfBitsException;;
        public void writeH(int address,int data) throws MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException;
        public void writeW(int address,int data) throws MemoryElementNotFoundException,IrregularWriteOperationException,NotAlingException;
        
        }