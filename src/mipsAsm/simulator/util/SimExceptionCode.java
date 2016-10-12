package mipsAsm.simulator.util;

public enum SimExceptionCode
{
	IntegerOverflow,
	UnimplementedInstruction,
	ReservedInstruction,
	SystemCall,
	Breakpoint,
	Trap,
	AddressError_L,
	AddressError_S,
	//TLB invalid and TLB modified are indistinguishable
	TLB_L,
	TLB_S,
	TLBModified
}
