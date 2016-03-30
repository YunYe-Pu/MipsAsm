package mipsAsm.assembler.operand;

import java.util.HashMap;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.exception.LabelNotDeclaredError;
import mipsAsm.assembler.util.AsmWarning;
import mipsAsm.assembler.util.LabelOccurence;
import mipsAsm.assembler.util.LinkType;
import mipsAsm.assembler.util.Occurence;

public class OpLabel extends Operand implements LinkableOperand
{
	public static final String format = "[a-zA-Z_][\\w]*";

	public final String labelName;
	protected int value;
	protected int mask = 0xffffffff;
	
	//For error signaling
	protected Occurence occurence;
	
	protected OpLabel(String labelName)
	{
		this.labelName = labelName;
	}
	
	@Override
	public void link(HashMap<String, LabelOccurence> labelMap, int sourceAddr, LinkType linkType) throws AsmError
	{
		LabelOccurence destAddr = labelMap.get(this.labelName);
		if(destAddr == null)
		{
			AsmError ex = new LabelNotDeclaredError(this.labelName);
			ex.setOccurence(this.occurence);
			throw ex;
		}
		this.value = linkType.link(sourceAddr, destAddr.address);
		int mask = ~this.mask;
		if((this.value & mask) != 0 && (this.value & mask) != mask)
		{
			AsmError ex = new AsmError("Label address overflow", "Actual value of label \"" + this.labelName + "\" overflows.");
			ex.setOccurence(this.occurence);
			throw ex;
		}
	}
	
	@Override
	public int getEncoding()
	{
		return this.value & this.mask;
	}
	
	@Override
	public AsmWarning setWidth(int width)
	{
		if(width < 32)
			this.mask = ((1 << width) - 1);
		else
			this.mask = 0xffffffff;
		return null;
	}
	
	
	public void setOccurence(Occurence occurence)
	{
		this.occurence = occurence;
	}

}
