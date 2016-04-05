package mipsAsm.assembler.util;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.exception.OpCountMismatchError;
import mipsAsm.assembler.exception.OpTypeMismatchError;
import mipsAsm.assembler.operand.OpImmediate;
import mipsAsm.assembler.operand.OpLabel;
import mipsAsm.assembler.operand.OpRegister;
import mipsAsm.assembler.operand.Operand;

public class OperandFmt
{
	public static final OperandFmt NONE = new OperandFmt();
	public static final OperandFmt R3 = new OperandFmt(OpRegister.class, OpRegister.class, OpRegister.class);
	public static final OperandFmt RRI = new OperandFmt(OpRegister.class, OpRegister.class, OpImmediate.class);
	public static final OperandFmt I = new OperandFmt(OpImmediate.class);
	public static final OperandFmt RI = new OperandFmt(OpRegister.class, OpImmediate.class);
	public static final OperandFmt RR = new OperandFmt(OpRegister.class, OpRegister.class);
	public static final OperandFmt IIR = new OperandFmt(OpImmediate.class, OpImmediate.class, OpRegister.class);
	public static final OperandFmt RIR = new OperandFmt(OpRegister.class, OpImmediate.class, OpRegister.class);
	public static final OperandFmt R = new OperandFmt(OpRegister.class);
	
	public static final OperandFmt L = new OperandFmt(OpLabel.class);
	public static final OperandFmt RL = new OperandFmt(OpRegister.class, OpLabel.class);
	
	private Class<? extends Operand>[] opTypes;
	
	@SafeVarargs
	public OperandFmt(Class<? extends Operand>... opTypes)
	{
		this.opTypes = opTypes;
	}
	
	public void matches(Operand[] ops) throws AsmError
	{
		if(this.opTypes.length != ops.length)
			throw new OpCountMismatchError(this.opTypes.length, ops.length);
		
		int i = 0;
		for(Class<? extends Operand> t : this.opTypes)
		{
			if(!t.isInstance(ops[i]))
				throw new OpTypeMismatchError(i, ops[i], this.opTypes[i]);
			i++;
		}
	}
}
