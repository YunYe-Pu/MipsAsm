package mipsAsm.simulator.instruction;

import mipsAsm.simulator.util.SimExceptionCode;

import static mipsAsm.simulator.util.SimExceptionCode.*;

public final class OpCp0
{
	private OpCp0() {}
	
	
	public static final Instruction executor = (sim, p) -> {
		//Check permission
		if((sim.cp0.get(12, 0) & 0x10) != 0)
			sim.signalException(SimExceptionCode.CopUnusable, 0);

		if((p[0] & 0x2000000) != 0)
		{
			switch(p[0] & 0x3f) {
			case 0x01://TLBR
				sim.tlb.readEntry();
				break;
			case 0x02://TLBWI
				sim.tlb.writeIndexed();
				break;
			case 0x06://TLBWR
				sim.tlb.writeRandom();
				break;
			case 0x08://TLBP
				sim.tlb.probe();
				break;
			case 0x18://ERET
				if((sim.cp0.hardGet(12) & 4) != 0)//statusERL
				{
					sim.cp0.hardSet(12, 0, 4);
					sim.jumpTo(sim.cp0.hardGet(30));//ErrorEPC
				}
				else
				{
					sim.cp0.hardSet(12, 0, 2);
					sim.jumpTo(sim.cp0.hardGet(14));//EPC
				}
				sim.signalException(ERET);//Signal an exception to inhibit program counter increment in step() method
				break;
			default:
				sim.signalException(ReservedInstruction);
			}
		}
		else if(((p[0] >> 21) & 0x1f) == 4)//MTC0
		{
			sim.cp0.set(p[0] >> 11, p[0] & 7, sim.gpr.get(p[0] >> 16));
		}
		else if(((p[0] >> 21) & 0x1f) == 0)//MFC0
		{
			sim.gpr.set(p[0] >> 16, sim.cp0.get(p[0] >> 11, p[0]));
		}
		else
			sim.signalException(ReservedInstruction);
	};
	
	
}
