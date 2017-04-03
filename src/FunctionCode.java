import java.util.*;

public class FunctionCode
{
	public Stack<SymbolTable> scopeStack = new Stack<SymbolTable>();
	public Stack<CodeBlock> codeStack = new Stack<CodeBlock>();
	public Stack<CodeBlock> finalStack = new Stack<CodeBlock>();
	public Stack<String> nextLabelIFStack = new Stack<String>();	
	public Stack<String> nextLabelDOStack = new Stack<String>();	
	public HashSet<String> calledBy = new HashSet<String>();	
	public HashSet<String> funcsCalled = new HashSet<String>();
	public IRNodeListHandler irHandler;
	public TinyNodeListHandler tnHandler;
	public String functionName = null;
	public HashMap<String, Symbol> parameters = new HashMap<String, Symbol>();
	public String retType = null;
	public int scopeDepth = 1;
	public int linkSize = 0;
	public int ifDepth = 0;
	
	public FunctionCode(String fn, String type)
	{
		functionName = fn; 
		retType = type;
		scopeStack.push(new SymbolTable(fn));
		irHandler = new IRNodeListHandler();
		tnHandler = new TinyNodeListHandler();

	}
	public int getLocalVariablesCount()
	{
		int count = 0;
		Stack<SymbolTable> tempStack = new Stack<SymbolTable>();
		while(scopeStack.size() > 0)
		{
			SymbolTable st = scopeStack.pop();
			tempStack.push(st);			
		}
		SymbolTable st = tempStack.pop();
		count = st.localVariables.size();
		scopeStack.push(st);			
		
		while(tempStack.size() > 0)
		{
			scopeStack.push(tempStack.pop());
		}
		return count;
	}

	public SymbolTable getCurrentTable()
	{	
		return scopeStack.peek();
	}

	public CodeBlock getCurrentCodeBlock()
	{	
		return codeStack.peek();
	}

	public void emptyCodeStack()
	{
		while(codeStack.size() > 0)
		{
			finalStack.push(codeStack.pop());
		}
	}

	public void insertIFBlock()
	{	
		String block = String.format("BLOCK %d", scopeDepth++);
		SymbolTable st = new SymbolTable(block);
		scopeStack.push(st);
		CodeBlock cb = new CodeBlock(block, new AST(block, LabelTracker.genNewLabel(), LabelTracker.currentLabel));
		cb.ast.astCondType = "IF";
		nextLabelIFStack.push(LabelTracker.currentLabel);
		//cb.ast.depthIF = nextLabelIFStack.size();
		cb.ast.depthIF = ++ifDepth;
		//System.out.println("IFFFF");
		//System.out.println(cb.ast.depthIF);
		codeStack.push(cb);


	}
	public void updateIFStack()
	{
		String currentOutLabel = nextLabelIFStack.pop();
		insertOUTLABELBlock(currentOutLabel);
		//Update outlabels for others
		--ifDepth;
		updateCurrentIFOutLabels(currentOutLabel, ifDepth + 1);

	}
	public void insertOUTLABELBlock(String outLabel)
	{	String block = String.format("%s", scopeStack.peek().scope);
		CodeBlock cb = new CodeBlock(block, new AST(block, LabelTracker.genNewLabel(), LabelTracker.currentLabel));
		cb.ast.outLabel = outLabel;		
		cb.ast.astCondType = "OUTLABEL";
		codeStack.push(cb);
	}

	public void insertELSIFBlock()
	{	String block = String.format("BLOCK %d", scopeDepth++);
		SymbolTable st = new SymbolTable(block);
		scopeStack.push(st);
		CodeBlock cb = new CodeBlock(block, new AST(block, LabelTracker.genNewLabel(), LabelTracker.currentLabel));
		cb.ast.astCondType = "ELSIF";
		cb.ast.prevNextLabel = nextLabelIFStack.pop();
		nextLabelIFStack.push(LabelTracker.currentLabel);
		//cb.ast.depthIF = nextLabelIFStack.size();
		cb.ast.depthIF = ifDepth;

		//System.out.println("ELSEEEE");
		//System.out.println(cb.ast.depthIF);		
		codeStack.push(cb);
	}
	public void insertBaseBlock()
	{	String block = String.format("%s", scopeStack.peek().scope);
		CodeBlock cb = new CodeBlock(block, new AST(block, null, null));
		cb.ast.astCondType = "BASE";
		codeStack.push(cb);
	}
	
	public void updateCurrentIFOutLabels(String outLabel, int depth)
	{
		Stack<CodeBlock> tempStack = new Stack<CodeBlock>();
		while(codeStack.size() > 0)
		{
			CodeBlock cb = codeStack.pop();
			if(cb.ast.astCondType == null) continue;
			
			if(cb.ast.astCondType.equals("ELSIF"))
			{
				if(cb.ast.depthIF == depth)
				{
					cb.ast.outLabel = outLabel;
				}
			}
			tempStack.push(cb);
		}		
		while(tempStack.size() > 0)
		{
			codeStack.push(tempStack.pop());
		}
		
	}

	public void insertDOBlock()
	{	String block = String.format("BLOCK %d", scopeDepth++);
		SymbolTable st = new SymbolTable(block);
		scopeStack.push(st);
		CodeBlock cb = new CodeBlock(block, new AST(block, LabelTracker.genNewLabel(), LabelTracker.currentLabel));
		cb.ast.astCondType = "DO";
		cb.ast.depthIF = ++ifDepth;
		nextLabelDOStack.push(LabelTracker.currentLabel);
		codeStack.push(cb);
	}
	public void insertWHILEBlock()
	{	
		String block = String.format("%s", scopeStack.peek().scope);
		String nextLabel = nextLabelDOStack.pop();
		CodeBlock cb = new CodeBlock(block, new AST(block, nextLabel, nextLabel));
		cb.ast.astCondType = "WHILE";
		--ifDepth;
		codeStack.push(cb);
	}

	public void insertSymbolInTop(String type, String name, String value)
	{	
		SymbolTable top = scopeStack.pop();
		List<String> vars = Arrays.asList(name.split(","));
		for(String v: vars)
		{
			String trimmed = v.trim();		
			if(getRegName(trimmed) != null)
			{
				System.out.println("DECLARATION ERROR" + trimmed);
				System.exit(0);
			}
			top.localVariables.put(trimmed, new Symbol(type, getCurrentVariableName(top), value));
		}
		scopeStack.push(top);
	}
	
	public String getCurrentVariableName(SymbolTable st)
	{
		return String.format("$L%d", st.localVariables.size() + 1);
	}
	public void addParameter(String type, String varName, String value)
	{
		String trimmed = varName.trim();		
		if(getRegName(trimmed) != null)
		{
			System.out.println("DECLARATION ERROR" + trimmed);
			System.exit(0);
		}
		else
		{
			parameters.put(trimmed, new Symbol(type, String.format("$P%d", parameters.size() + 1), value));
		}
		
	}
	public String getRegName(String varName)
	{
		boolean exists = false;
		String regName = null;
		String trimmed = varName.trim();
		Stack<SymbolTable> tempStack = new Stack<SymbolTable>();
		while(scopeStack.size() > 0)
		{
			SymbolTable st = scopeStack.pop();
			tempStack.push(st);			
			if(st.localVariables.containsKey(trimmed))
			{
				exists = true;
				regName = st.localVariables.get(trimmed).name;
				break;	
			}
		}
		while(tempStack.size() > 0)
		{
			scopeStack.push(tempStack.pop());
		}
		if(exists) return regName;
		if(parameters.containsKey(trimmed)) return parameters.get(trimmed).name;
		if(Micro.globalTable.localVariables.containsKey(trimmed)) return trimmed;
		return regName;
			
	}
	public String getVarType(String varName)
	{
		boolean exists = false;
		String varType = null;
		String trimmed = varName.trim();
		Stack<SymbolTable> tempStack = new Stack<SymbolTable>();
		while(scopeStack.size() > 0)
		{
			SymbolTable st = scopeStack.pop();
			tempStack.push(st);			
			if(st.localVariables.containsKey(trimmed))
			{
				exists = true;
				varType = st.localVariables.get(trimmed).type;
				break;	
			}
		}
		while(tempStack.size() > 0)
		{
			scopeStack.push(tempStack.pop());
		}
		if(exists) return varType;
		if(parameters.containsKey(trimmed)) return parameters.get(trimmed).type;
		if(Micro.globalTable.localVariables.containsKey(trimmed)) return Micro.globalTable.localVariables.get(trimmed).type;
		return varType;
			
	}
	

	
}
