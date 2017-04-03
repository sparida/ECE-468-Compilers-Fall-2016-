import java.util.*;

public class Node
{
	public String opc;
	public String op1;
	public String op2;
	public String res;
	public String type;
	public HashSet<Integer> succ = new HashSet<Integer>();
	public HashSet<Integer> prev = new HashSet<Integer>();
	public HashSet<String>  gen  = new HashSet<String>();
	public HashSet<String>  kill = new HashSet<String>();
	public HashSet<String>  ins  = new HashSet<String>();
	public HashSet<String>  outs = new HashSet<String>();


	public Node(String opc, String op1, String op2, String res, String type)
	{
		this.opc = opc;
		this.op1 = op1;
		this.op2 = op2;
		this.res = res;
		this.type = type;
	}

	public String getStringRepresentation()
	{
		// For Dummy Node for comp of tiny

		//if(opc.equals("COM") && type.equals("IR")) return "";
		String s = new String();
		s  = "";
		//s = s + ((type == "IR") ? ";" : "");
		s = s + opc + " ";
		s = s + ((op1 != null) ? op1 + " " : ""); 
		s = s + ((op2 != null) ? op2 + " " : ""); 
		s = s + ((res != null) ? res + " " : "");
		return s.trim(); 
	}

}
