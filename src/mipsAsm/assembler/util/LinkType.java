package mipsAsm.assembler.util;

public interface LinkType
{
	int link(int sourceAddr, int destAddr);
	
	LinkType RELATIVE_WORD = (s, d) -> d - (s + 1);
	LinkType ABSOLUTE_WORD = (s, d) -> d;
	LinkType ABSOLUTE_BYTE = (s, d) -> d << 2;
}
