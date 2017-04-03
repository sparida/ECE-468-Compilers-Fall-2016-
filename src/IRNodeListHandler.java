import java.util.*;
import java.util.Random;
public class IRNodeListHandler
{
	public ArrayList<Node> IRNodes = new ArrayList<Node>();
	public int regCount = 1;
	public String lastType = "INT";
	public HashMap<Integer, Node> IREnum = new HashMap<Integer, Node>();
	
	public void reset()
	{
		regCount = 1;
		lastType = "INT";
	}
	public int getNodeIndex(int c, String rep)
	{
		for(int count = 1 ; count <= IREnum.size(); count++)
		{
			Node n = IREnum.get(count);			
			//String str = n.getStringRepresentation();
			if(rep.equals(n.res) && c != count) return count;
		}
		return -1;
		
	}	
	public void getIREnum()
	{
		int count = 0;
		for (Node n : IRNodes)
		{
			String str = n.getStringRepresentation().trim();
			//if (str.length() != 0) //System.out.println(";Hello");
			//{			
			count = count + 1;
			IREnum.put(count, n);
			//}
		}
	}

	public void computeInsOuts()
	{
		HashSet<Integer> workList = new HashSet<Integer>();
		for (int count = 1;  count <= IREnum.size(); count++)
		{
			Node current = IREnum.get(count);	
			workList.add(count);
			if(current.opc.equals("RET"))
			{
				// For In Out Calculation
				for(String s : Micro.globalTable.localVariables.keySet())
				{ 
					current.outs.add(s);
				}
			}
			IREnum.put(count, current);
		}
	
		while(workList.size() > 0)
		{
			int ind = getRandomNodeIndex(workList);
			workList.remove(ind);
			Node current = IREnum.get(ind);
			
			// Get Copy of original live in
			HashSet<String> initIns = new HashSet<String>(current.ins);

			// Compute Live In and Live Out
			HashSet<String> newOuts = new HashSet<String>();
			for(Integer i : current.succ)
			{
				Node n = IREnum.get(i);
				newOuts.addAll(n.ins);				
			}
			current.outs = newOuts;
			HashSet<String> newIns = new HashSet<String>(newOuts);
			newIns.removeAll(current.kill);
			newIns.addAll(current.gen);
			current.ins = newIns;
			
			if(!initIns.equals(newIns))
			{
				for(Integer i : current.prev)
				{
					workList.add(i);				
				}
			}
			
			 
		}
	}
	
	public int getRandomNodeIndex(HashSet<Integer> workList)
	{
		ArrayList<Integer> list = new ArrayList<Integer>(workList);
		Random rand = new Random();
		int index = rand.nextInt(list.size());
		return list.get(index);
	}	
	public void computeGenKill()
	{
		for(int count = 1 ; count <= IREnum.size(); count++)
		{
			HashSet<String>  gen  = new HashSet<String>();
			HashSet<String>  kill = new HashSet<String>();
			Node current = IREnum.get(count);
			if(isConditionalOperator(current.opc))
			{
				// Add Gen Elements For Current Node
				current.gen.add(current.op1);
				current.gen.add(current.op2);

				// Add Kill Elements fo Current Node	
				//current.kill.add(current.res);
				
				// Add Node Back
				IREnum.put(count, current);
			}			

			else if(isArithmeticOperator(current.opc))
			{
				// Add Gen Elements For Current Node
				current.gen.add(current.op1);
				current.gen.add(current.op2);

				// Add Kill Elements fo Current Node	
				current.kill.add(current.res);
				
				// Add Node Back
				IREnum.put(count, current);
			}			

			else if(current.opc.contains("WRITE"))
			{
				// Add Gen Elements For Current Node
				current.gen.add(current.res);

				// Add Kill Elements fo Current Node	

				// Add Node Back
				IREnum.put(count, current);

			}			
			else if(current.opc.contains("READ"))
			{
				// Add Gen Elements For Current Node

				// Add Kill Elements fo Current Node	
				current.kill.add(current.res);

				// Add Node Back
				IREnum.put(count, current);


			}
			else if(current.opc.equals("PUSH"))
			{
				if(current.res != null)
				{

					// Add Gen Elements For Current Node
					current.gen.add(current.res);
	
					// Add Kill Elements fo Current Node	
	
					// Add Node Back
					IREnum.put(count, current);
				}
			}
			else if(current.opc.equals("POP"))
			{
				if(current.res != null)
				{
					// Add Gen Elements For Current Node
	
					// Add Kill Elements fo Current Node	
					current.kill.add(current.res);
	
					// Add Node Back
					IREnum.put(count, current);
				}

			}			
			
			else if(current.opc.equals("JSR"))
			{
				
				// Add Gen Elements For Current Node
				for(String s : Micro.globalTable.localVariables.keySet())
				{ 
					current.gen.add(s);
				}

				// Add Kill Elements fo Current Node	
				
				// Add Node Back
				IREnum.put(count, current);
				
			}
			
			else if(current.opc.contains("STORE"))
			{
				String s= "0123456789";
				// Add Gen Elements For Current Node
				if(current.op1 != null)
				{
					char c = current.op1.charAt(0);
					if(!(c >= '0' && c <= '9')) current.gen.add(current.op1);
				}

				// Add Kill Elements fo Current Node	
				current.kill.add(current.res);
				
				// Add Node Back
				IREnum.put(count, current);
			}			
			
			else
			{
			}

		}
	}



	public void computeSuccPrev()
	{
		for(int count = 1 ; count <= IREnum.size(); count++)
		{
			Node current = IREnum.get(count);
			Node fallThrough = null;;
			if(isConditionalOperator(current.opc))
			{
				// Add succ For Current Node
				int ind = getNodeIndex(count, current.res);
				current.succ.add(ind);
				if(count != IREnum.size()) current.succ.add(count + 1);

				// Add current node as prev for successor nodes
				Node jumpTarget = IREnum.get(ind);
				jumpTarget.prev.add(count);
				if(count != IREnum.size()) 
				{
					fallThrough = IREnum.get(count + 1);
					fallThrough.prev.add(count);
				}

				// Update IREnum
				IREnum.put(count, current);
				if(fallThrough != null) IREnum.put(count + 1, fallThrough);
				IREnum.put(ind, jumpTarget);				
			}			
			else if(current.opc.equals("JUMP"))
			{
				// Add succ For Current Node
				int ind = getNodeIndex(count, current.res);
				current.succ.add(ind);

				// Add current node as prev for successor nodes
				Node jumpTarget = IREnum.get(ind);
				jumpTarget.prev.add(count);

				// Update IREnum
				IREnum.put(count, current);
				IREnum.put(ind, jumpTarget);			
			}			
			else if(current.opc.equals("RET"))
			{

			}			
			else
			{
				// Add succ For Current Node
				if(count != IREnum.size()) current.succ.add(count + 1);

				// Add current node as prev for successor nodes
				if(count != IREnum.size()) 
				{
					fallThrough = IREnum.get(count + 1);
					fallThrough.prev.add(count);
				}

				// Update IREnum
				IREnum.put(count, current);
				if(fallThrough != null) IREnum.put(count + 1, fallThrough);
			}

		}
	}

	public boolean isConditionalOperator(String op)
	{
		if     (op.equals("LT")) return true;
		else if(op.equals("GT")) return true;
		else if(op.equals("LE")) return true;
		else if(op.equals("GE")) return true;
		else if(op.equals("NE")) return true;
		else if(op.equals("EQ")) return true;
		else                     return false;
		
	}	
	public boolean isArithmeticOperator(String op)
	{
		if     (op.contains("ADD"))  return true;
		else if(op.contains("SUB"))  return true;
		else if(op.contains("DIV"))  return true;
		else if(op.contains("MULT")) return true;
		else                         return false;
		
	}	

	public void printIRRepresentation()
	{
		for(int count = 1 ; count <= IREnum.size(); count++)
		{
			Node n = IREnum.get(count);			
			String str = n.getStringRepresentation();
			System.out.println(String.format("; %d : %s", count, str));
			System.out.println("; Prev: " + n.prev + " Succ: " + n.succ + " Gen: " + n.gen + " Kill: " + n.kill);
			System.out.println("; Ins: "  + n.ins +  " Outs: " + n.outs);
		}
	}
	
	public String getIROPC(String operand)
	{
		switch(operand)
		{
			case "+": return "ADD";
			case "-": return "SUB";
			case "*": return "MULT";
			case "/": return "DIV";
			default : return "ADD";
		}
	}

	public String getIFCONDOPC(String operand)
	{
		switch(operand)
		{
			case "<": return "GE";
			case ">": return "LE";
			case "=": return "NE";
			case "!=": return "EQ";
			case ">=": return "LT";
			case "<=": return "GT";
			default : return "EQ";
		}
	}

	public String getWHILECONDOPC(String operand)
	{
		switch(operand)
		{
			case "<": return "LT";
			case ">": return "GT";
			case "=": return "EQ";
			case "!=": return "NE";
			case ">=": return "GE";
			case "<=": return "LE";
			default : return "EQ";
		}
	}

	
	public void addIRNodes(ArrayList<Node> nodes)
	{
		for(Node n : nodes)
		{
			IRNodes.add(n);
		}
	}
}
