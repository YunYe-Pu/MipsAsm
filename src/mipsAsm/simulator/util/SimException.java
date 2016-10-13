package mipsAsm.simulator.util;

public class SimException extends Exception
{
	private static final long serialVersionUID = -8677248459431022860L;

	public final SimExceptionCode excCode;
	public final int param;
	
	public SimException(SimExceptionCode excCode, int param)
	{
		this.excCode = excCode;
		this.param = param;
	}
}
