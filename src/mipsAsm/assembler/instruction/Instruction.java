package mipsAsm.assembler.instruction;

public interface Instruction
{
	/**
	 * Generate a hexadecimal string representing the machine instruction.
	 * It is not necessary to add leading 0s; the assembler will do this.
	 * 
	 * @return A hexadecimal string representing the machine instruction, 8 characters long at most.
	 */
	int toBinary();
	
}
