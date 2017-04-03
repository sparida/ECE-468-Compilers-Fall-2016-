import java.util.*;

public class SymbolTable
{
	public String scope;
	public HashMap<String, Symbol> localVariables = new HashMap<String, Symbol>();
	

	public SymbolTable(String scope)
	{
		this.scope = scope;
	}
	public void printDebug()
	{
		System.out.println("STATEMENTS");
	}

	public void printSymbols()
	{
		System.out.println("Symbol table " + scope);
		for(String s: Micro.globalTable.localVariables.keySet())
		{
			System.out.println(localVariables.get(s).getPrintRepresentation());
		}
		
		System.out.println();
	}
	
	public void addSymbol(String type, String name, String value)
	{
		List<String> vars = Arrays.asList(name.split(","));
		for(String v: vars)
		{
			String trimmed = v.trim();		
			if(alreadyDeclared(trimmed))
			{
				System.out.println("DECLARATION ERROR" + trimmed);
				System.exit(0);
				continue;
			}
			localVariables.put(trimmed, new Symbol(type, getCurrentVariableName(), value));
		}
	}

	public String getCurrentVariableName()
	{
		return String.format("$L%d", localVariables.size() + 1);
	}
	
	public boolean alreadyDeclared(String name)
	{
		return localVariables.containsKey(name);
	}
	
}
