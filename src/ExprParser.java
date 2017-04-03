import java.util.*;

public class ExprParser
{
	public ArrayList<ExprNode> exprList = new ArrayList<ExprNode>();

	public void reset()
	{
		exprList.clear();
	}
	public String getExprString()
	{
		ArrayList<ExprNode> PONodeList = null;
		PONodeList = genPOList();
		
		StringBuilder sb = new StringBuilder();
		for (ExprNode n : PONodeList)
		{
    			sb.append(n.name);
		}
	

		return (sb.toString());
	}

	public ASTNode getExprTree()
	{
		//System.out.println(getExprString());
		ASTNode exprTree = null;
		ASTNode currRoot = null;
		ArrayList<ExprNode> PONodeList = null;
		Stack<ASTNode> poASTStack = new Stack<ASTNode>();
		PONodeList = genPOList();
		//System.out.println(PONodeL)
		for (ExprNode n : PONodeList)
		{
			if(n.type.equals("IDENTIFIER"))
			{
				String specType = Micro.currentFunction.getVarType(n.name);
				poASTStack.push(new ASTNode(n.name, "IDENTIFIER", specType));
			}
			else if(n.type.equals("INT"))
			{
				poASTStack.push(new ASTNode(n.name, "LITERAL", "INT"));
			}
			else if(n.type.equals("FLOAT"))
			{
				poASTStack.push(new ASTNode(n.name, "LITERAL", "FLOAT"));
			}
			else if(n.type.equals("FUNCTION"))
			{	
				ASTNode newNode = new ASTNode(n.name, "FUNCTION", Micro.funcTypeHashMap.get(n.name));
				newNode.params = n.params;
				poASTStack.push(newNode);
			}

			else
			{
				ASTNode exp2Tree = poASTStack.pop();
				ASTNode exp1Tree = poASTStack.pop();
				ASTNode exp = new ASTNode(n.name, "OPERATOR", n.type);
				exp.left = exp1Tree;
				exp.right = exp2Tree;
				poASTStack.push(exp);
			}
		}
		if(poASTStack.size() > 0) return poASTStack.pop();
		else return null;
	}

	public int prec(ExprNode n)
	{
		if(n.type.equals("LP")) return 1;
		else if (n.type.equals("OPADD")) return 2;
		else if (n.type.equals("OPMUL")) return 3;
		else return 0;
	}

	public ArrayList<ExprNode> genPOList()
	{
		Stack<ExprNode> exprStack = new Stack<ExprNode>();
		Stack<ExprNode> tempStack = new Stack<ExprNode>();
		ArrayList<ExprNode> PONodeList = new ArrayList<ExprNode>();

		
		for(ExprNode n : exprList)
		{
			//System.out.println("Before");
			//System.out.println(PONodeList.toString());
			//System.out.println("After");
			if(n.type.equals("IDENTIFIER") || n.type.equals("INT") || n.type.equals("FLOAT") || n.type.equals("FUNCTION"))
			{
				PONodeList.add(n);
			}
			else if (n.type.equals("LP"))
			{
				exprStack.push(n);
			}
			else if(n.type.equals("RP"))
			{
				ExprNode top = exprStack.pop();
				while(!top.type.equals("LP"))
				{
					PONodeList.add(top);
					top = exprStack.pop();
				}
			}
			else
			{
				while(!exprStack.isEmpty() && (prec(exprStack.peek()) >= prec(n)) )
				{
					PONodeList.add(exprStack.pop());
				}
				exprStack.push(n);
			}
		}
		while(!exprStack.isEmpty())
		{
				PONodeList.add(exprStack.pop());
		}
		return PONodeList;
	}
}

