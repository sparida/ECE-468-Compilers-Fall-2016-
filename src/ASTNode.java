import java.util.*;
public class ASTNode
{
	public String name;
	public String genType;
	public String specType;
	public String dest = null;
	public String dest2 = null;

	public ASTNode parent = null;
	public ASTNode left = null;
	public ASTNode right = null;
	public ArrayList<ASTNode> params = new ArrayList<ASTNode>();
	public ArrayList<String> destArray = new ArrayList<String>(); 
	
	public ASTNode(String name, String genType, String specType)
	{
		this.name = name;
		this.genType = genType;
		this.specType = specType;
	}
	public void printASTNode()
	{
		System.out.println(name);
		System.out.println(genType);
		System.out.println(specType);
		System.out.println();
	}
	public void postOrderASTNode()
	{
		if(left != null) left.postOrderASTNode();
		if(right != null) right.postOrderASTNode();
		printASTNode();
		
	}
	public String addIRNodesFromASTNode()
	{
		// String opc, String op1, String op2, String res, String type
		String leftDest = null;
		String rightDest = null;
		if(left != null) leftDest = left.addIRNodesFromASTNode();
		if(right != null) rightDest = right.addIRNodesFromASTNode();
		if(genType.equals("FUNCTION"))
		{
			destArray.clear();
			for(ASTNode a: params)
			{
				destArray.add(a.addIRNodesFromASTNode());
			}
		}

		if(genType.equals("IDLIST")) 
		{
			dest = name;
			Micro.currentFunction.irHandler.lastType = specType;
			return dest;
		}	
		if(genType.equals("IDENTIFIER")) 
		{
			dest = Micro.currentFunction.getRegName(name);
			Micro.currentFunction.irHandler.lastType = specType;
			//System.out.println(name);
			//System.out.println(specType);

			return dest;
		}
		else if (genType.equals("RETURN"))
		{

			String opc = (Micro.currentFunction.irHandler.lastType.equals("INT")) ? "STOREI" : "STOREF";
			if(left.genType.equals("LITERAL"))
			{
				Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, leftDest, null, "$R", "IR"));
				Micro.currentFunction.irHandler.IRNodes.add(new Node("RET", null, null, null, "IR"));
			}
			else
			{	
				Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, leftDest, null , String.format("$T%d", Micro.currentFunction.irHandler.regCount++), "IR"));
				Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, String.format("$T%d", Micro.currentFunction.irHandler.regCount - 1), null, "$R", "IR"));
				Micro.currentFunction.irHandler.IRNodes.add(new Node("RET", null, null, null, "IR"));
			}				
			return leftDest;		
		}
		else if (genType.equals("FUNCTION"))
		{
			dest = String.format("$T%d", Micro.currentFunction.irHandler.regCount++);
			Micro.currentFunction.irHandler.lastType = specType;
			Micro.currentFunction.irHandler.IRNodes.add(new Node("PUSH", null, null, null, "IR"));
			for(String d: destArray)
			{
				
				Micro.currentFunction.irHandler.IRNodes.add(new Node("PUSH", null, null, d, "IR"));
		
			}

			Micro.currentFunction.irHandler.IRNodes.add(new Node("JSR", null, null, name, "IR"));
			for(String d: destArray)
			{
				Micro.currentFunction.irHandler.IRNodes.add(new Node("POP", null, null, null, "IR"));
		
			}
			Micro.currentFunction.irHandler.IRNodes.add(new Node("POP", null, null, dest, "IR"));
			return dest;
		}

		else if (genType.equals("LITERAL"))
		{
			dest = String.format("$T%d", Micro.currentFunction.irHandler.regCount++);
			String opc = (specType.equals("INT")) ? "STOREI" : "STOREF";
			Micro.currentFunction.irHandler.lastType = specType;
			Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, name, null, dest, "IR"));
			return dest;
		}
		else if (genType.equals("ASSIGNMENT"))
		{
			//System.out.println("InsideAssigbment");
			//if(Micro.currentFunction.irHandler.lastType == null) System.out.println("WTF");
			String opc = (Micro.currentFunction.irHandler.lastType.equals("INT")) ? "STOREI" : "STOREF";
			//System.out.println("LeavinAssigbment");
			
			if(left.genType.equals("IDENTIFIER") && right.genType.equals("IDENTIFIER"))
			{
				dest = String.format("$T%d", Micro.currentFunction.irHandler.regCount++);
				Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, rightDest, null, dest, "IR"));
				Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, dest, null, leftDest, "IR"));				
			}
			else
			{
				Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, rightDest, null, leftDest, "IR"));
			}
			return dest;		
		}
		else if (name.equals("READ") || name.equals("WRITE"))
		{
			
		
			List<String> vars = Arrays.asList(leftDest.split(","));
			for(String v: vars)
			{
				String trimmed = v.trim();
				String varType = Micro.currentFunction.getVarType(trimmed);

				String opc = name;
				if(varType.equals("INT")) opc = opc + "I";
				else if(varType.equals("FLOAT")) opc = opc + "F";
				else if(varType.equals("STRING")) opc = opc + "S";
				Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, null, null, Micro.currentFunction.getRegName(trimmed), "IR"));
		
			}	
			return dest;		
		}
		else if (genType.equals("OPERATOR"))
		{
			String opc = Micro.currentFunction.irHandler.getIROPC(name);
			opc = opc + (Micro.currentFunction.irHandler.lastType.equals("INT") ? "I" : "F");
			dest = String.format("$T%d", Micro.currentFunction.irHandler.regCount++);
			Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, leftDest, rightDest, dest, "IR"));
			return dest;
		}
		else if(genType.equals("COND"))
		{
			String opc = (Micro.currentFunction.irHandler.lastType.equals("INT")) ? "STOREI" : "STOREF";
			if(left.genType.equals("IDENTIFIER") && right.genType.equals("IDENTIFIER"))
			{
				dest = String.format("$T%d", Micro.currentFunction.irHandler.regCount++);
				Micro.currentFunction.irHandler.IRNodes.add(new Node(opc, rightDest, null, dest, "IR"));
				dest2 = dest;
				dest = leftDest;

				
			}
			else
			{
				dest = leftDest;
				dest2 = rightDest;
			}
		}

		return null;
		
	}
}
