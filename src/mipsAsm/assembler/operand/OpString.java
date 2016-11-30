package mipsAsm.assembler.operand;

import mipsAsm.assembler.exception.AsmError;
import mipsAsm.assembler.util.AsmWarning;

/**
 * An operand representing a string enclosed in quotes. This type of operand
 * is used exclusively in assembler directives, and cannot be directly converted
 * to bit stream.
 * 
 * @author YunYe Pu
 */
public class OpString extends Operand
{
	public static final String format = "\".*\"";
	
	public final char[] content;
	public final int length;
	
	public OpString(String token) throws AsmError
	{
//		System.out.println(token);
		char[] s1 = token.toCharArray();
		char[] s2 = new char[s1.length];
		int i, j;
		boolean escapeSeq = false;
		for(i = 1, j = 0; i < s1.length - 1; i++)
		{
			
			if(escapeSeq)
			{
				switch(s1[i]) {
				case 'b':
					s2[j++] = '\b';
					break;
				case 't':
					s2[j++] = '\t';
					break;
				case 'n':
					s2[j++] = '\n';
					break;
				case 'f':
					s2[j++] = '\f';
					break;
				case 'r':
					s2[j++] = '\r';
					break;
				case '\"':
					s2[j++] = '\"';
					break;
				case '\'':
					s2[j++] = '\'';
					break;
				case '\\':
					s2[j++] = '\\';
					break;
//				case 's':
//					s2[j++] = ' ';
//					break;
				default:
					throw new AsmError("Invalid escape sequence", "The escape sequence \\" + s1[i]
							+ " is invalid; valid ones are \\b \\t \\n \\f \\r \\\" \\\' \\\\");
				}
				escapeSeq = false;
			}
			else
			{
				if(s1[i] == '\\')
					escapeSeq = true;
				else
					s2[j++] = s1[i];
			}
		}
		
		this.content = s2;
		this.length = j;
	}
	
	public OpString(char[] content, int length)
	{
		this.content = content;
		this.length = length;
	}
	

	@Override
	@Deprecated
	public int getEncoding()
	{
		return 0;
	}

	@Override
	@Deprecated
	public AsmWarning setWidth(int width)
	{
		return null;
	}

}
