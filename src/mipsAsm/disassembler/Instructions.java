package mipsAsm.disassembler;

import java.util.function.Function;

import mipsAsm.assembler.util.InstructionFmt;
import mipsAsm.disassembler.util.DisassemblyEntry;

public enum Instructions implements Function<Integer, String>
{
	J    (0x02, "%i", InstructionFmt.J, 0),
	JAL  (0x03, "%i", InstructionFmt.J, 0),
	BEQ  (0x04, "%r, %r, %i", 0, 1, 2),
	BNE  (0x05, "%r, %r, %i", 0, 1, 2),
	BLEZ (0x06, "%r, %i", 0, 2),
	BGTZ (0x07, "%r, %i", 0, 2),
	ADDI (0x08, "%r, %r, %i", 1, 0, 2),
	ADDIU(0x09, "%r, %r, %i", 1, 0, 2),
	SLTI (0x0a, "%r, %r, %i", 1, 0, 2),
	SLTIU(0x0b, "%r, %r, %i", 1, 0, 2),
	ANDI (0x0c, "%r, %r, %i", 1, 0, 2),
	ORI  (0x0d, "%r, %r, %i", 1, 0, 2),
	XORI (0x0e, "%r, %r, %i", 1, 0, 2),
	LUI  (0x0f, "%r, %i", 1, 2),
	BEQL (0x14, "%r, %r, %i", 0, 1, 2),
	BNEL (0x15, "%r, %r, %i", 0, 1, 2),
	BLEZL(0x16, "%r, %i", 0, 2),
	BGTZL(0x17, "%r, %i", 0, 2),
	
	LB   (0x20, "%r, %i(%r)", 1, 2, 0),
	LH   (0x21, "%r, %i(%r)", 1, 2, 0),
	LWL  (0x22, "%r, %i(%r)", 1, 2, 0),
	LW   (0x23, "%r, %i(%r)", 1, 2, 0),
	LBU  (0x24, "%r, %i(%r)", 1, 2, 0),
	LHU  (0x25, "%r, %i(%r)", 1, 2, 0),
	LWR  (0x26, "%r, %i(%r)", 1, 2, 0),

	SB   (0x28, "%r, %i(%r)", 1, 2, 0),
	SH   (0x29, "%r, %i(%r)", 1, 2, 0),
	SWL  (0x2a, "%r, %i(%r)", 1, 2, 0),
	SW   (0x2b, "%r, %i(%r)", 1, 2, 0),
	SWR  (0x2e, "%r, %i(%r)", 1, 2, 0),
	CACHE(0x2f, "%i, %i(%r)", 1, 2, 0),
	LL   (0x30, "%r, %i(%r)", 1, 2, 0),
	SC   (0x38, "%r, %i(%r)", 1, 2, 0),
	PREF (0x33, "%i, %i(%r)", 1, 2, 0),
	;

	public final int opCode;
	public final InstructionFmt binaryFormat;
	public final DisassemblyEntry disassembly;
	
	private Instructions(int opCode, String format, InstructionFmt binaryFormat, int... order)
	{
		this.opCode = opCode;
		this.binaryFormat = binaryFormat;
		this.disassembly = new DisassemblyEntry(this.name().toLowerCase() + "\t" + format, order);
	}
	
	private Instructions(int opCode, String format, int... order)
	{
		this(opCode, format, InstructionFmt.I, order);
	}
	
	@Override
	public String apply(Integer binary)
	{
		int[] param = this.binaryFormat.splitBinary(binary);
		return this.disassembly.apply(param);
	}

}
