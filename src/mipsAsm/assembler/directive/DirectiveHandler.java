package mipsAsm.assembler.directive;

import java.util.ArrayList;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.instruction.Instruction;
import mipsAsm.assembler.operand.Operand;

public interface DirectiveHandler
{
	public void handle(Operand[] arguments, Assembler assember, ArrayList<Instruction> instructionList) throws AsmError;
}
