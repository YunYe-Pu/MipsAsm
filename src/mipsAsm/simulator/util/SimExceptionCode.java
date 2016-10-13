package mipsAsm.simulator.util;

import java.util.function.ObjIntConsumer;

import mipsAsm.simulator.Simulator;

public enum SimExceptionCode
{
	IntegerOverflow(0xc),
	UnimplementedInstruction(0xa),
	ReservedInstruction(0xa),
	SystemCall(0x8),
	Breakpoint(0x9),
	Trap(0xd),
	AddressError_L(4),
	AddressError_S(5),
	CopUnusable(0xb),
	TLBInvalid_L(2),
	TLBInvalid_S(3),
	TLBRefill_L(2),
	TLBRefill_S(3),
	TLBModified(1),
	Interrupt(0);
	
	private int causeCode;
	private ObjIntConsumer<Simulator> additionalOp = (sim, i) -> {};
	
	private SimExceptionCode(int causeCode)
	{
		this.causeCode = causeCode;
	}
	
	private SimExceptionCode()
	{
		this(-1);
	}
	
	public void process(Simulator sim, int param)
	{
		int offset;
		if((sim.cp0.hardGet(12) & 2) == 0)
		{
			if(sim.inDelaySlot())
			{
				sim.cp0.hardSet(14, sim.getPC() - 4, -1);
				sim.cp0.hardSet(13, -1, 0x80000000);
			}
			else
			{
				sim.cp0.hardSet(14, sim.getPC(), -1);
				sim.cp0.hardSet(13, 0, 0x80000000);
			}
			if(this == TLBRefill_L || this == TLBRefill_S)
				offset = 0;
			else if(this == Interrupt && (sim.cp0.hardGet(13) & 0x00800000) != 0)
				offset = 0x200;
			else
				offset = 0x180;
		}
		else
			offset = 0x180;
		if(this.causeCode >= 0)
			sim.cp0.hardSet(13, causeCode << 2, 0x7c);
		sim.cp0.hardSet(12, -1, 2);//Status_EXL = 1
		if((sim.cp0.hardGet(12) & (1<<22)) != 0)
			sim.scheduleAbsoluteJump(0xbfc00000 + offset, 0xbfc00004 + offset, -1);
		else
			sim.scheduleAbsoluteJump(0x80000000 + offset, 0x80000004 + offset, -1);
		this.additionalOp.accept(sim, param);
	}
	
	static
	{
		ObjIntConsumer<Simulator> TLBProc = (sim, p) -> {
			sim.cp0.hardSet(8, p, -1);
			sim.cp0.hardSet(4, p >> 9, 0x007ffff0);
			sim.cp0.hardSet(10, p, 0xffffe000);
		};
		TLBInvalid_L.additionalOp = TLBProc;
		TLBInvalid_S.additionalOp = TLBProc;
		TLBRefill_L.additionalOp = TLBProc;
		TLBRefill_S.additionalOp = TLBProc;
		TLBModified.additionalOp = TLBProc;
		
		ObjIntConsumer<Simulator> adErProc = (sim, p) -> {
			sim.cp0.hardSet(8, p, -1);
		};
		AddressError_L.additionalOp = adErProc;
		AddressError_S.additionalOp = adErProc;
		
		CopUnusable.additionalOp = (sim, p) -> sim.cp0.hardSet(13, p << 28, 3 << 28);
		Interrupt.additionalOp = (sim, p) -> sim.cp0.hardSet(13, p << 8, 0xff00);
	}
	
}
