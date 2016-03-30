package mipsAsm.assembler.operand;

import java.util.HashMap;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.util.AsmWarning;
import mipsAsm.assembler.util.LabelOccurence;
import mipsAsm.assembler.util.LinkType;

/**
 * This class allows splitting an operand into two or more separate operands,
 * to be used in different instructions. Example is the <b>LI</b> macro-instruction, where
 * a 32-bit immediate should be split into two 16-bit immediates.
 * @author YunYe Pu
 *
 */
public class OpSplit extends Operand implements LinkableOperand
{
	public final Operand operand;
	private int shift;
	private int mask;

	public OpSplit(Operand operand, int shift, int mask)
	{
		this.operand = operand;
		this.shift = shift;
		this.mask = mask;
	}
	
	public OpSplit(Operand operand, int shift)
	{
		this(operand, shift, 0xffffffff);
	}
	
	@Override
	public int getEncoding()
	{
		return (this.operand.getEncoding() >> this.shift) & this.mask;
	}

	@Override
	public AsmWarning setWidth(int width)
	{
		this.mask = (1 << width) - 1;
		return null;
	}

	@Override
	public void link(HashMap<String, LabelOccurence> labelMap, int sourceAddr, LinkType linkType) throws AsmError
	{
		if(this.operand instanceof LinkableOperand)
			((LinkableOperand)this.operand).link(labelMap, sourceAddr, linkType);
	}

}
