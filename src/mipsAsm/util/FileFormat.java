package mipsAsm.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class FileFormat implements BinaryIOFunction
{
	public final String extension;
	public final String description;
	public final String prefix;
	public final String separator;
	public final String suffix;
	public final Pattern[] prefixPattern;
	public final Pattern wordPattern;
	public final int wordOffset;
	
	public FileFormat(JsonObject json)
	{
		this.extension = json.get("extension").getAsString();
		this.description = json.get("description").getAsString();
		this.prefix = json.get("prefix").getAsString();
		this.separator = json.get("separator").getAsString();
		this.suffix = json.get("suffix").getAsString();
		JsonObject wordPattern = json.get("wordPattern").getAsJsonObject();
		this.wordPattern = Pattern.compile(wordPattern.get("regex").getAsString(), wordPattern.get("opt").getAsInt());
		this.wordOffset = wordPattern.get("offset").getAsInt();
		JsonArray prefixPattern = json.get("prefixPattern").getAsJsonArray();
		this.prefixPattern = new Pattern[prefixPattern.size()];
		for(int i = 0; i < this.prefixPattern.length; i++)
		{
			JsonObject obj = prefixPattern.get(i).getAsJsonObject();
			this.prefixPattern[i] = Pattern.compile(obj.get("regex").getAsString(), obj.get("opt").getAsInt());
		}
	}

	public FileFormat(String extension, String description, String prefix, String separator, String suffix,
		String wordPattern, int wordPatternOpt, int wordPatternOffset, String[] prefixPattern, int[] prefixPatternOpt)
	{
		this.extension = extension;
		this.description = description;
		this.prefix = prefix;
		this.separator = separator;
		this.suffix = suffix;
		this.wordPattern = Pattern.compile(wordPattern, wordPatternOpt);
		this.wordOffset = wordPatternOffset;
		this.prefixPattern = new Pattern[prefixPattern.length];
		for(int i = 0; i < prefixPattern.length; i++)
			this.prefixPattern[i] = Pattern.compile(prefixPattern[i], prefixPatternOpt[i]);
	}
	
	public JsonObject toJson()
	{
		JsonObject ret = new JsonObject();
		ret.add("description", new JsonPrimitive(this.description));
		ret.add("extension", new JsonPrimitive(this.extension));
		ret.add("prefix", new JsonPrimitive(this.prefix));
		ret.add("separator", new JsonPrimitive(this.separator));
		ret.add("suffix", new JsonPrimitive(this.suffix));
		JsonObject pattern = new JsonObject();
		pattern.add("regex", new JsonPrimitive(this.wordPattern.pattern()));
		pattern.add("opt", new JsonPrimitive(this.wordPattern.flags()));
		pattern.add("offset", new JsonPrimitive(this.wordOffset));
		ret.add("wordPattern", pattern);
		JsonArray array = new JsonArray();
		for(Pattern p : this.prefixPattern)
		{
			pattern = new JsonObject();
			pattern.add("regex", new JsonPrimitive(p.pattern()));
			pattern.add("opt", new JsonPrimitive(p.flags()));
			array.add(pattern);
		}
		ret.add("prefixPattern", array);
		return ret;
	}
	
	@Override
	public void write(File target, int[] data, boolean endian) throws IOException
	{
		try(PrintWriter output = new PrintWriter(target))
		{
			String strAppend = this.prefix;
			for(int val : data)
			{
				output.print(strAppend);
				String s = Integer.toHexString(val);
				for(int j = s.length(); j < 8; j++)
					output.print('0');
				output.print(s);
				strAppend = this.separator;
			}
			output.print(this.suffix);
		}
	}
	
	@Override
	public List<Integer> read(File target, boolean endian) throws IOException
	{
		ArrayList<Integer> binary = new ArrayList<>();
		try(Scanner scanner = new Scanner(target))
		{
			for(Pattern p : this.prefixPattern)
				scanner.next(p);
			while(scanner.hasNext(this.wordPattern))
				binary.add(Integer.parseUnsignedInt(scanner.next(this.wordPattern).substring(this.wordOffset, this.wordOffset + 8), 16));
		}
		return binary;
	}
}
