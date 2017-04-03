import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.*;
import java.lang.*;

public class Micro 
{
	public static FunctionCode mainFunction = new FunctionCode("MAIN", "VOID");
	public static ArrayList<FunctionCode> functions = new ArrayList<FunctionCode>();
	public static ExprParser exprParser  = new ExprParser();
	public static ExprParser paramParser  = new ExprParser();
	public static Stack<ExprParser> exprParserStack  = new Stack<ExprParser>();

	public static Stack<ExprNode> funcNodeStack = new Stack<ExprNode>();
	public static boolean isFuncExpr = false;
	public static FunctionCode currentFunction = null;
	public static SymbolTable globalTable = new SymbolTable("GLOBAL");
	public static HashMap<String, String>  funcTypeHashMap = new HashMap<String, String>();
	public static HashMap<String, Integer> funcLinkSizes = new HashMap<String, Integer>();
	public static HashMap<String, Integer> funcLocalSizes = new HashMap<String, Integer>();
	public static HashMap<String, Integer> funcParamSizes = new HashMap<String, Integer>();
	public static HashMap<String, HashSet<String>> calledBy = new HashMap<String, HashSet<String>>();
	public static TinyNodeListHandler tnHandler = new TinyNodeListHandler();
	public static void main(String[] args) throws Exception 
	{
		ANTLRFileStream input = new ANTLRFileStream(args[0]);
                MicroLexer lexer = new MicroLexer(input);
		Step1Lexer step1lexer = new Step1Lexer();
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		MicroParser parser = new MicroParser(tokenStream);
		CustomErrorStrategy es = new CustomErrorStrategy();
		parser.setErrorHandler(es); 
		try
		{
			parser.program();
			localParamSizeAnalysis();
			//calledByTransform();
			//evaluateLinkSizes();
			//for (String s : calledBy.keySet())
			//{
			//	System.out.println(String.format("FuctionName:%s", s));
			//	System.out.println(String.format("Link Size:  %d", funcLinkSizes.get(s)));
			//	HashSet<String> hs = calledBy.get(s);
			//	for(String h : hs) System.out.println(String.format("CB:%s", h));
			//}
			
			System.out.println(";IR code");
				
			for (FunctionCode f : functions)
			{
				
				f.irHandler.IRNodes.add(new Node("LABEL", null, null, f.functionName, "IR"));
				f.irHandler.IRNodes.add(new Node("LINK", null, null, null, "IR"));
				currentFunction = f;
				Stack<CodeBlock> tempStack = new Stack<CodeBlock>(); 
				while(f.finalStack.size() > 0)
				{
					//System.out.println(f.finalStack.size());
					CodeBlock cb = f.finalStack.pop();
					cb.addIRNodes();
					tempStack.push(cb);

				}
				while(tempStack.size() > 0)
				{
					f.finalStack.push(tempStack.pop());
				}

				f.irHandler.getIREnum();
				f.irHandler.computeSuccPrev();
				f.irHandler.computeGenKill();
				f.irHandler.computeInsOuts();
				f.irHandler.printIRRepresentation();
				f.tnHandler.generateGetTiny4RFromIRNodesHacky();
				System.out.println("");
				//IRNodeListHandler.reset();	
			}

			// Print Tiny For Global Variables
			System.out.println(";tiny code");
			tnHandler.generateTinyForGlobalVariabes();
			tnHandler.printTinyRepresentation();

			//Print Tiny For Functions
			for (FunctionCode f : functions)
			{					
				f.tnHandler.printTinyRepresentation();
			}
			
			
			// Do not uncomment
			//System.out.println("Accepted");
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			//System.out.println("Not Accepted");
		}
		
	}

	public static boolean isFunctionLabel(String label)
	{	
		boolean res = false;
		for (FunctionCode f : functions)
		{
			if(f.functionName.equals(label)) res = true;
		}
		return res;
	}
	public static void localParamSizeAnalysis()
	{
		for (FunctionCode f : functions)
		{
			funcLocalSizes.put(f.functionName, f.getLocalVariablesCount());
			funcParamSizes.put(f.functionName, f.parameters.size());
		}
		
	}
	public static void calledByTransform()
	{
				
		for (FunctionCode f : functions)
		{
			for(String s: f.funcsCalled)
			{
				if(calledBy.containsKey(s))
				{
					HashSet<String> hs = calledBy.get(s); 
					hs.add(f.functionName);
					calledBy.put(s, hs);
				}
				else
				{
					HashSet<String> hs = new HashSet<String>(); 
					hs.add(f.functionName);
					calledBy.put(s, hs);
				}
			}
		}

	}
	
	public static void evaluateLinkSizes() 
	{
		for (String s : calledBy.keySet())
		{
			funcLinkSizes.put(s, 0);
		}
		for (String s : calledBy.keySet())
		{
			HashSet<String> hs = calledBy.get(s);
			for(String h : hs)
			{
				int size = funcLinkSizes.get(s);
				size = size + funcLocalSizes.get(h);
				funcLinkSizes.put(s, size);
			}
			int size = funcLinkSizes.get(s);
			size = size + funcLocalSizes.get(s) + funcParamSizes.get(s);
			funcLinkSizes.put(s, size);

		}
		funcLinkSizes.put("main", 6);
	}
	
	public static String findGlobalType(String varName)
	{
		if(globalTable.alreadyDeclared(varName))
		{
			return globalTable.localVariables.get(varName).type;
		}
		else
		{
			System.out.println("Unidentified Variable");
			System.exit(0);
		}
		return "";
	}
}
 

