import java.util.*;

public class CodeBlock
{
	public String scope;
	public AST ast = new AST(null, null, null);
	
	public CodeBlock(String scope, AST ast)
	{
		this.scope = scope;
		this.ast = ast;
	}
	public void printDebug()
	{
		System.out.println("STATEMENTS");
	}
	public void addIRNodes()
	{
		if(ast.astCondType != null)
		{

			if(ast.astCondType.equals("BASE"))
			{
				//System.out.println("EnteringBase");
				//System.out.println(ast.ASTList.size());
				//System.out.println("LeavigBase");
				for(ASTNode node : ast.ASTList) node.addIRNodesFromASTNode();
			}
			else if(ast.astCondType.equals("DO"))
			{
				Micro.currentFunction.irHandler.IRNodes.add(new Node("LABEL", null, null, ast.nextLabel, "IR"));	
			}
			else if(ast.astCondType.equals("WHILE"))
			{
				
				if(ast.astCondVal == null)
				{
					ast.astCondNode.addIRNodesFromASTNode();
					Micro.currentFunction.irHandler.IRNodes.add(new Node("COM", Micro.currentFunction.irHandler.lastType, null, null, "IR"));
					Micro.currentFunction.irHandler.IRNodes.add(
					new Node(Micro.currentFunction.irHandler.getWHILECONDOPC(ast.astCondNode.name), ast.astCondNode.dest, ast.astCondNode.dest2, ast.nextLabel, "IR"));
				}
				else
				{
					if(ast.astCondVal.equals("TRUE")) Micro.currentFunction.irHandler.IRNodes.add(new Node("JUMP", null, null, ast.nextLabel, "IR"));
					
				}

			}


			else if(ast.astCondType.equals("OUTLABEL"))
			{
				Micro.currentFunction.irHandler.IRNodes.add(new Node("LABEL", null, null, ast.outLabel, "IR"));
				for(ASTNode node : ast.ASTList) node.addIRNodesFromASTNode();
			}

		
			else if(ast.astCondType.equals("IF"))
			{
				
				if(ast.astCondVal == null)
				{
					ast.astCondNode.addIRNodesFromASTNode();
					Micro.currentFunction.irHandler.IRNodes.add(new Node("COM", Micro.currentFunction.irHandler.lastType, null, null, "IR"));
					Micro.currentFunction.irHandler.IRNodes.add(
					new Node(Micro.currentFunction.irHandler.getIFCONDOPC(ast.astCondNode.name), ast.astCondNode.dest, ast.astCondNode.dest2, ast.nextLabel, "IR"));
				}
				else
				{
					if(ast.astCondVal.equals("FALSE")) Micro.currentFunction.irHandler.IRNodes.add(new Node("JUMP", null, null, ast.nextLabel, "IR"));
					
				}
			}
			else if(ast.astCondType.equals("ELSIF"))
			{
				// For debugging
				Micro.currentFunction.irHandler.IRNodes.add(new Node("JUMP", null, null, ast.outLabel, "IR"));
				Micro.currentFunction.irHandler.IRNodes.add(new Node("LABEL", null, null, ast.prevNextLabel, "IR"));

				if(ast.astCondVal == null)
				{
					ast.astCondNode.addIRNodesFromASTNode();
					Micro.currentFunction.irHandler.IRNodes.add(new Node("COM", Micro.currentFunction.irHandler.lastType, null, null, "IR"));
					Micro.currentFunction.irHandler.IRNodes.add(
					new Node(Micro.currentFunction.irHandler.getIFCONDOPC(ast.astCondNode.name), ast.astCondNode.dest, ast.astCondNode.dest2, ast.nextLabel, "IR"));
				}
				else
				{
					if(ast.astCondVal.equals("FALSE")) Micro.currentFunction.irHandler.IRNodes.add(new Node("JUMP", null, null, ast.nextLabel, "IR"));
					
				}
			}
		}
	}
	
}
