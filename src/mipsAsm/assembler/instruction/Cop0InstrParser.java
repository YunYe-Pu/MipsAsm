package mipsAsm.assembler.instruction;

import java.util.ArrayList;

import mipsAsm.assembler.Assembler;
import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.operand.OpConstant;
import mipsAsm.assembler.operand.OpRegister.OpCp0Register;
import mipsAsm.assembler.operand.Operand;
import mipsAsm.assembler.util.AsmWarning;
import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.assembler.util.OperandFmt;

public enum Cop0InstrParser implements InstructionParser
{
	MFC0(0x0),
	MTC0(0x4)
	;
	
	private final int code;
	
	private Cop0InstrParser(int code)
	{
		this.code = code;
	}
	
	@Override
	public void parse(Operand[] operands, Assembler assembler, ArrayList<Instruction> instrList) throws AsmError
	{
		Operand[] ops = new Operand[3];
		if(operands.length == 2)
		{
			OperandFmt.RR.matches(operands);
			ops[1] = operands[0];
			if(operands[1] instanceof OpCp0Register)
				ops[2] = operands[1];
			else
				ops[2] = new OpCp0Register(operands[1].getEncoding(), 0);
		}
		else if(operands.length == 3)
		{
			OperandFmt.RRI.matches(operands);
			ops[1] = operands[0];
			if(operands[1] instanceof OpCp0Register)
			{
				AsmWarning w = new AsmWarning("Deprecated operand type",
						"The sel field provided will override the sel field specified by cop0 register name.");
				assembler.handleWarning(w);
				((OpCp0Register)operands[1]).setSelField(operands[2].getEncoding() & 7);
			}
			ops[2] = operands[1];
		}
		else
			throw new AsmError("Operand number mismatch", "Expected 2 or 3 operands but provided " + operands.length + ".");
		ops[0] = new OpConstant(code);
		instrList.add(InstructionFmt.I.newInstance(0x10, ops, null, assembler));
	}

}
