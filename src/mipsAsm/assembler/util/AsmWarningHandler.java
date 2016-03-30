package mipsAsm.assembler.util;

import mipsAsm.assembler.exception.AsmError;

/**
 * Class implementing this interface should accept the assembly warnings,
 * and either print appropriate warning messages, or throw an assembly error.
 * 
 * @author YunYe Pu
 */
public interface AsmWarningHandler
{
	public void handleWarning(AsmWarning e) throws AsmError;
}
