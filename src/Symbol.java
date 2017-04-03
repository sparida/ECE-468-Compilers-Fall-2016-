public class Symbol
{
	public String type;
	public String name;
	public String value;
	
	public Symbol(String type, String name, String value)
	{
		this.type = type;
		this.name = name;
		this.value = value;
	}
	
	public String getPrintRepresentation()
	{
		String printVal = String.format("name %s type %s", this.name, this.type);
		printVal = printVal + ((this.type == "STRING") ? (String.format(" value %s", this.value)) : "");
		return printVal;
	}
}
