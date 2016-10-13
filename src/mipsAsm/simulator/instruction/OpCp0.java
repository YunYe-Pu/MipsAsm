package mipsAsm.simulator.instruction;

import mipsAsm.simulator.util.SimExceptionCode;

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
			case 0x02://TLBWI
			case 0x06://TLBWR
			case 0x08://TLBP
			case 0x18://ERET
			}
		}
		else if(((p[0] >> 21) & 0x1f) == 4)//MTC0
		{
			
		}
		else if(((p[0] >> 21) & 0x1f) == 0)//MFC0
		{
			
		}
		else
			sim.signalException(SimExceptionCode.ReservedInstruction);
	};
	
	
}
