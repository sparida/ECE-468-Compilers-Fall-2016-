import java.util.*;
public class ExprNode
{
	public String name;
	public String type;
	public ArrayList<ASTNode> params = new ArrayList<ASTNode>(); 
	public ExprNode(String name, String type)
	{
		this.name = name;
		this.type = type;
	}
}
