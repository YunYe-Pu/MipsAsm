package mipsAsm.simulator.util;

public enum SimulatorException
{
	IntegerOverflow,
	UnimplementedInstruction,
	ReservedInstruction,
	SystemCall,
	Breakpoint,
	Trap,
	DebugBreakpoint,
}
