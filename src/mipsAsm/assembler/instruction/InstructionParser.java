package mipsAsm.assembler.instruction;

import java.util.ArrayList;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.AsmWarningHandler;

public interface InstructionParser
{
	/**
	 * Parse the operands and put the generated instruction(s) into the list.
	 * Throw an {@link AsmError} if unable to assemble the instruction.<br />
	 * There is no restriction on how many instruction is generated.
	 * 
	 * @param operands The operands supplied by the assembler.
	 * @param warningHandler The warning handler. If any warning occur(currently
	 * mostly from {@link Operand#setWidth} method), invoke {@link AsmWarningHandler#handleWarning handleWarning}
	 * method to handle the warnings.
	 * @param instrList The list to put the instruction(s) in.
	 */
	public void parse(Operand[] operands, AsmWarningHandler warningHandler, ArrayList<Instruction> instrList) throws AsmError;
	
}
