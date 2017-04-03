import java.util.*;

public class AST
{
	public String name;
	public String outLabel;
	public String nextLabel;
	public String prevNextLabel;
	
	public ASTNode astCondNode = null;
	public String astCondVal = null;
	public String astCondType = null;
	public int depthIF = 0;
	
	public String isIFLastBlock = "False";
	public ASTNode firstNode = null;
	public ASTNode lastNode = null;
	
	public ASTNode parentNode = null;
	public ASTNode lastTerminal = null;
	public List<ASTNode> ASTList = new ArrayList<ASTNode>();
	
	public AST(String name, String outLabel, String nextLabel)
	{
		this.name = name;
		this.outLabel = outLabel;
		this.nextLabel = nextLabel;
		
	}
	
	public void addCond(ASTNode exprTree, String op, String exprType)
	{
		if(exprType.equals("VAL"))
		{
			astCondVal = op;
		}
		else if(exprType .equals("1")) 
		{
			
			astCondNode = new ASTNode(null, "COND", null);
			astCondNode.left = exprTree;
		}	
		else if(exprType .equals("2"))
		{
			astCondNode.name = op;
			astCondNode.right = exprTree;
		}	
	}

	
	public void addTerminalNode(String type, String id)
	{	
		String scopeTableType = null;
		scopeTableType = Micro.currentFunction.getVarType(id);
		String nodeSpecType = type.equals("IDENTIFIER") ? scopeTableType : null;
		lastTerminal = new ASTNode(id, type, nodeSpecType);  		
	}
	
	public void addAssignmentString(String expr)
	{
		ASTNode assNode = new ASTNode(":=", "ASSIGNMENT", null);

		ASTNode idNode = lastTerminal;
		ASTNode exprNode = new ASTNode(expr, "Expression", null);
		assNode.left = idNode;	
		assNode.right = exprNode;

		ASTList.add(assNode);
		if(firstNode == null) firstNode = assNode;
		lastNode = assNode;		
	}

	public void addAssignmentNode(ASTNode exprTree)
	{
		ASTNode assNode = new ASTNode(":=", "ASSIGNMENT", null);

		ASTNode idNode = lastTerminal;
		assNode.left = idNode;	
		assNode.right = exprTree;

		ASTList.add(assNode);

		if(firstNode == null) firstNode = assNode;
		lastNode = assNode;		
	}

	public void addReturnNode(ASTNode exprTree)
	{
		ASTNode retNode = new ASTNode("RET", "RETURN", null);

		retNode.left = exprTree;

		ASTList.add(retNode);

		if(firstNode == null) firstNode = retNode;
		lastNode = retNode;		
	}
	
	public void addIO(String name, String idList)
	{
		String type = null;
		for(String id : idList.split(","))
		{
			type = Micro.currentFunction.getVarType(id.trim());
		}
		ASTNode ioNode = new ASTNode(name, type, null);
		ASTNode idListNode = new ASTNode(idList, "IDLIST", type); //type
		ioNode.left = idListNode;	
		ioNode.right = null;

		ASTList.add(ioNode);
		if(firstNode == null) firstNode = ioNode;
		lastNode = ioNode;		
	}
}
