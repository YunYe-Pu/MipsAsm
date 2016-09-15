package mipsAsm.assembler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mipsAsm.assembler.exception.AsmError;

public class Tokenizer implements AutoCloseable
{
	private final FileReader stream;
	private final List<String> lineToken = new ArrayList<>();
	private final StringBuilder tokenBuffer = new StringBuilder();

	public Tokenizer(File file) throws IOException
	{
		this.stream = new FileReader(file);
	}

	@Override
	public void close() throws Exception
	{
		this.stream.close();
	}

	public List<String> nextLine()
	{
		
		//TODO
		return null;
	}
	
	public boolean hasNextLine()
	{
		return false;
	}
	
	private void prepareNextLine() throws IOException, AsmError
	{
		boolean comment = false;
		boolean quotation = false;
		int prevChar = 0;
		int currChar = 0;
		
		this.tokenBuffer.setLength(0);
		
		while(true)
		{
			currChar = this.stream.read();
			
			if(comment)
			{
				if(currChar == '\n' && prevChar != '\\')
					comment = false;
			}
			else
			{
				if(quotation)
				{
					if(prevChar != '\\')
					{
						if(currChar == '\"')
							quotation = false;
						else if(currChar == '\n')
							throw new AsmError("Unclosed quotation", "Reached end of line with an open quotation");
					}
				}
			}
			prevChar = currChar;
		}
		
	}
}
