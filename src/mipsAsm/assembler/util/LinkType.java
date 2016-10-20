package mipsAsm.assembler.util;

public interface LinkType
{
	int link(int sourceAddr, int destAddr);
	
	LinkType RELATIVE_WORD = (s, d) -> d - (s + 1);
	LinkType REGIONAL_WORD = (s, d) -> d - (s & 0xfc000000);
	LinkType ABSOLUTE_BYTE = (s, d) -> d << 2;
}
