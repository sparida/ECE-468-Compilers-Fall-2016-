import java.util.*;
public class TinyNodeListHandler
{
	public ArrayList<Node> TinyNodes = new ArrayList<Node>();
	public int regCount = 1;
	public String lastType = "INT";
	public Map<String, String> IR2TinyRegs = new HashMap<String, String>();
	public String lastComp = "";
	public String currFuncName = "";

	// Register Alllocation
	public HashMap<Integer, String> registers = new HashMap<Integer, String>();
	public HashMap<Integer, Boolean> dirty    = new HashMap<Integer, Boolean>();
	public HashSet<String> regVals = new HashSet<String>();

	//public static Stack<Integer> linkStack = new Stack<Integer>();
	public TinyNodeListHandler()
	{
		registers.put(0, null);
		registers.put(1, null);
		registers.put(2, null);
		registers.put(3, null);
		dirty.put(0, false);
		dirty.put(1, false);
		dirty.put(2, false);
		dirty.put(3, false);
	}
	
	public int ensure(String op, HashSet<String> currLiveOut)
	{
		int retVal = 0;
		//System.out.println(op);
		if(regVals.contains(op))
		{
			for (int r : registers.keySet())
			{	
				if(registers.get(r) == null) continue;
				if(registers.get(r).equals(op)) retVal = r;
			}
		}
		else
		{
			int r;
			r = allocate(op, currLiveOut);
			if(getType(op).equals("REG"))
			{
				TinyNodes.add(new Node("move", getTempMemLoc(op), String.format("r%d", r), null, "TN"));
			}
			else
			{
				TinyNodes.add(new Node("move",  tinyFormat(op), String.format("r%d", r), null, "TN"));
			}
			retVal = r;
		}
		return retVal;
	}
	public void printRegisters()
	{
		String s = "; ";
		for (int r : registers.keySet())
		{
			if(registers.get(r) == null) s = s + String.format("R%d -> BLANK  ",  r);
			else s = s + String.format("R%d -> %s  ",  r, registers.get(r));
			if(dirty.get(r)) s = s + "(*)";
		}
		System.out.println(s);
	}
	public String getTempMemLoc(String tempName)
	{
		return String.format("$-%d", Micro.currentFunction.getLocalVariablesCount() +  Integer.parseInt(tempName.substring(2)));
	}
	public void free(int r, HashSet<String> currLiveOut)
	{
		if (registers.get(r) == null) return;
		if(dirty.get(r) && currLiveOut.contains(registers.get(r)))
		{
			if(getType(registers.get(r)).contains("$T"))
			{
				TinyNodes.add(new Node("move", String.format("r%d", r), getTempMemLoc(registers.get(r)), null, "TN"));
			}
			else
			{
				TinyNodes.add(new Node("move", String.format("r%d", r), tinyFormat(registers.get(r)), null, "TN"));
			}
		}
		
		regVals.remove(registers.get(r));
		registers.put(r, null);
		dirty.put(r, false);
	}
	
	public void clearAllRegs(HashSet<String> currLiveOut)
	{
		free(1, currLiveOut); free(2, currLiveOut); free(3, currLiveOut); free(4, currLiveOut);
	}

	
	public int allocate(String op, HashSet<String> currLiveOut)
	{
		int r = 0;
		if(regVals.size() < 4)
		{
			for(int i: registers.keySet())
			{
				if(registers.get(i) == null)
				{
					r = i;
					break;
				}
			}
		}
		else
		{
			//r = chooseLatestRegister();
			r = 0;
			free(r, currLiveOut);
		}
		registers.put(r, op);
		regVals.add(op);
		return r;
	}
	
	public void generateTinyForGlobalVariabes()
	{
		for(String s: Micro.globalTable.localVariables.keySet())
		{
			String cmd = Micro.globalTable.localVariables.get(s).type.equals("STRING") ? "str" : "var";
			TinyNodes.add(new Node(cmd, s, Micro.globalTable.localVariables.get(s).value, null, "TN"));
		}
		TinyNodes.add(new Node("push", null, null, null, "TN"));
		TinyNodes.add(new Node("push", null, null, "r0", "TN"));
		TinyNodes.add(new Node("push", null, null, "r1", "TN"));
		TinyNodes.add(new Node("push", null, null, "r2", "TN"));
		TinyNodes.add(new Node("push", null, null, "r3", "TN"));
		TinyNodes.add(new Node("jsr" , null, null, "main", "TN"));
		TinyNodes.add(new Node("sys" , null, null, "halt", "TN"));
	}		
	public void printTinyRepresentation()
	{
		for(Node n : TinyNodes)
		{
			String str = n.getStringRepresentation(); 
			if(!str.equals("")) System.out.println(str);
		}
	}
	public int getCurrentLinkSize()
	{
		return Micro.funcParamSizes.get(currFuncName) + 6;	
	}
	
	
	public void makeDirty(int r, String op)
	{
		dirty.put(r, true);
	}

	public void loadOP(String op, String reg)
	{
		if (op == null) return;
		if(getType(op).equals("REG"))
		{
			//if (Integer.parseInt(getTempMemLoc(op).substring(2)) <=  8)
			{
				TinyNodes.add(new Node("move", getTempMemLoc(op), reg, null, "TN"));
			}
		}
		else
		{
			TinyNodes.add(new Node("move",  tinyFormat(op), reg, null, "TN"));
		}	
	} 	
	public void storeReg(String reg, String op)
	{
		if (op == null) return;

		if(getType(op).equals("REG"))
		{
			//if (Integer.parseInt(getTempMemLoc(op).substring(2)) <=8  )

			{
				TinyNodes.add(new Node("move", reg, getTempMemLoc(op), null, "TN"));
			}
		}
		else
		{
			TinyNodes.add(new Node("move",  reg, tinyFormat(op), null, "TN"));
		}	
	} 	

	public void generateGetTiny4RFromIRNodesHacky()
	{
		String lopc = null, lop1 = null, lop2 = null, lres = null;
		HashSet<String> currLiveOut;
		int Rx, Ry, Rz;	
		int r0, r1, r2;
		for(int count = 1 ; count <= Micro.currentFunction.irHandler.IREnum.size(); count++)
		{
			Node n = Micro.currentFunction.irHandler.IREnum.get(count);
			currLiveOut = n.outs;
			//System.out.println(Micro.currentFunction.irHandler.IREnum.get(count).getStringRepresentation());
			


			
			String tinyOp = returnTinyOperator(n.opc);
			//printRegisters();
			
			if(tinyOp.equals("FUNC"))
			{
				if (n.opc.equals("JSR"))
				{
					TinyNodes.add(new Node("push", null , null, "r0", "TN"));
					TinyNodes.add(new Node("push", null , null, "r1", "TN"));
					TinyNodes.add(new Node("push", null , null, "r2", "TN"));
					TinyNodes.add(new Node("push", null , null, "r3", "TN")); 
					TinyNodes.add(new Node("jsr" , n.res, null, null, "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r3", "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r2", "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r1", "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r0", "TN"));

				}
				else if (n.opc.equals("PUSH"))
				{
					loadOP(n.res, "r2");
					//loadOP(n.op2, "r1");
					//loadOP(n.res, "r2");
					if(n.res != null)
					{
				 		TinyNodes.add(new Node("push", null, null, "r2", "TN"));
						//storeReg("r2",n.res);
					}
					else
					{
						TinyNodes.add(new Node("push", null, null, null, "TN"));
					}
					
				}
				else if (n.opc.equals("POP"))
				{
					
					if(n.res != null)
					{
				 		TinyNodes.add(new Node("pop", null, null, "r2", "TN"));
						storeReg("r2",n.res);
					}
					else
					{
						TinyNodes.add(new Node("pop", null, null, null, "TN"));
					}
				}

				else if (n.opc.equals("LINK")) 
				{
					//TinyNodes.add(new Node("link", Micro.funcLocalSizes.get(currFuncName).toString(), null, null, "TN"));
					TinyNodes.add(new Node("link", "100", null, null, "TN"));
					//linkStack.push(Integer.parseInt(n.res));
				}
				else if (n.opc.equals("RET"))
				{
					TinyNodes.add(new Node("unlnk", null, null, null, "TN"));
					TinyNodes.add(new Node("ret", null, null, null, "TN"));
					TinyNodes.add(new Node(";", null, null, null, "TN"));
					//linkStack.pop();
					
				}

			}
			else if(tinyOp.equals("COMPOP"))
			{
				if(n.opc.equals("COM"))
				{
					if(n.op1.equals("INT"))	lastComp = "cmpi";
					else if(n.op1.equals("FLOAT")) lastComp = "cmpr";
				}
				else if (n.opc.equals("LABEL"))
				{ 
					if(!n.res.contains("label"))
					{
						currFuncName = n.res;
					}
					TinyNodes.add(new Node("label", n.res, null, null, "TN"));
				}
				else if (n.opc.equals("JUMP")) TinyNodes.add(new Node("jmp", n.res, null, null, "TN"));
				else if (n.opc.equals("JSR")) TinyNodes.add(new Node("jsr", n.res, null, null, "TN"));
				else
				{
					loadOP(n.op1, "r0"); loadOP(n.op2, "r1"); 
					TinyNodes.add(new Node(lastComp, "r0", "r1", null, "TN"));
					TinyNodes.add(new Node(getTinyCompOp(n.opc), n.res, null, null, "TN"));			
				}
			}	
			else if(tinyOp.equals("NOTOP"))
			{
				if(n.opc.equals("WRITEI")) 
				{
					TinyNodes.add(new Node("sys writei", tinyFormat(n.res), null, null, "TN"));
				}

				else if(n.opc.equals("WRITEF"))
				{
					TinyNodes.add(new Node("sys writer", tinyFormat(n.res), null, null, "TN"));
				}
				else if(n.opc.equals("WRITES"))
				{
					TinyNodes.add(new Node("sys writes", tinyFormat(n.res), null, null, "TN"));
				}

				else if(n.opc.equals("READI"))
				{
					TinyNodes.add(new Node("sys readi", tinyFormat(n.res), null, null, "TN"));
				}
				else if(n.opc.equals("READF"))
				{
					TinyNodes.add(new Node("sys readr", tinyFormat(n.res), null, null, "TN"));

				}
				else if(n.opc.equals("STOREI") || n.opc.equals("STOREF"))
				{	
					loadOP(n.op1, "r0"); 
					//TinyNodes.add(new Node("move", "r0", N(Rz, n.res), null, "TN"));
					storeReg("r0", n.res);
				}
			}
			// Arithmetic Operations
			else
			{
				loadOP(n.op1, "r0");loadOP(n.op2, "r1");
				//System.out.println(Rx);
				//System.out.println(Ry);
				//System.out.println(Rz);

				TinyNodes.add(new Node("move", "r0", "r2", null, "TN"));
				TinyNodes.add(new Node(tinyOp, "r1", "r2", null, "TN"));
				
				storeReg("r2", n.res);
			}

		}

	}
	public String N(int r,  String op)
	{
		//System.out.println("OKOKOK");
		//System.out.println(r);
		//System.out.println(op);
		//System.out.println(getType(op));
		//System.out.println(tinyFormat(op));

		if(getType(op).equals("REG")) 
		{
			return String.format("r%d", r);
		}
		else
		{ 
			//System.out.println(tinyFormat(op));
			//return tinyFormat(op);
			return String.format("r%d", r);
		}
	}

	/*
	public void generateGetTiny4RFromIRNodes()
	{
		String lopc = null, lop1 = null, lop2 = null, lres = null;
		HashSet<String> currLiveOut;
		int Rx, Ry, Rz;
		for(int count = 1 ; count <= Micro.currentFunction.irHandler.IREnum.size(); count++)
		{
			Node n = Micro.currentFunction.irHandler.IREnum.get(count);
			currLiveOut = n.outs;
			//System.out.println(Micro.currentFunction.irHandler.IREnum.get(count).getStringRepresentation());
			Rx = get
			String tinyOp = returnTinyOperator(n.opc);
			printRegisters();
			
			if(tinyOp.equals("FUNC"))
			{
				if (n.opc.equals("JSR"))
				{
					TinyNodes.add(new Node("push", null , null, "r0", "TN"));
					TinyNodes.add(new Node("push", null , null, "r1", "TN"));
					TinyNodes.add(new Node("push", null , null, "r2", "TN"));
					TinyNodes.add(new Node("push", null , null, "r3", "TN")); 
					TinyNodes.add(new Node("jsr" , n.res, null, null, "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r3", "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r2", "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r1", "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r0", "TN"));

				}
				else if (n.opc.equals("PUSH"))
				{
					if(n.res != null)
					{
						Rx = ensure(n.res, currLiveOut);
						if(!currLiveOut.contains(n.res)) free(Rx, currLiveOut);
				 		TinyNodes.add(new Node("push", null, null, N(Rx, n.res), "TN"));
					}
					else
					{
						TinyNodes.add(new Node("push", null, null, null, "TN"));
					}
					
				}
				else if (n.opc.equals("POP"))
				{
					if(n.res != null)
					{
						Rz = allocate(n.res, currLiveOut);
				 		TinyNodes.add(new Node("pop", null, null, N(Rz, n.res), "TN"));
						makeDirty(Rz, n.res);
					}
					else
					{
						TinyNodes.add(new Node("pop", null, null, null, "TN"));
					}
				}

				else if (n.opc.equals("LINK")) 
				{
					TinyNodes.add(new Node("link", Micro.funcLocalSizes.get(currFuncName).toString(), null, null, "TN"));
					//linkStack.push(Integer.parseInt(n.res));
				}
				else if (n.opc.equals("RET"))
				{
					TinyNodes.add(new Node("unlnk", null, null, null, "TN"));
					TinyNodes.add(new Node("ret", null, null, null, "TN"));
					TinyNodes.add(new Node(";", null, null, null, "TN"));
					//linkStack.pop();
					
				}

			}
			else if(tinyOp.equals("COMPOP"))
			{
				if(n.opc.equals("COM"))
				{
					if(n.op1.equals("INT"))	lastComp = "cmpi";
					else if(n.op1.equals("FLOAT")) lastComp = "cmpr";
				}
				else if (n.opc.equals("LABEL"))
				{ 
					if(!n.res.contains("label"))
					{
						currFuncName = n.res;
					}
					TinyNodes.add(new Node("label", n.res, null, null, "TN"));
				}
				else if (n.opc.equals("JUMP")) TinyNodes.add(new Node("jmp", n.res, null, null, "TN"));
				else if (n.opc.equals("JSR")) TinyNodes.add(new Node("jsr", n.res, null, null, "TN"));
				else
				{ 
					Rx = ensure(n.op1, currLiveOut);
					Ry = ensure(n.op2, currLiveOut);
					if(!currLiveOut.contains(n.op1)) free(Rx, currLiveOut);
					if(!currLiveOut.contains(n.op2)) free(Ry, currLiveOut);

					TinyNodes.add(new Node(lastComp, N(Rx, n.op1), N(Ry, n.op2), null, "TN"));
					TinyNodes.add(new Node(getTinyCompOp(n.opc), n.res, null, null, "TN"));			
				}
			}	
			else if(tinyOp.equals("NOTOP"))
			{
				if(n.opc.equals("WRITEI")) 
				{
					Rx = ensure(n.res, currLiveOut);
					if(!currLiveOut.contains(n.res)) free(Rx, currLiveOut);
					TinyNodes.add(new Node("sys writei", N(Rx, n.res), null, null, "TN"));
				}

				else if(n.opc.equals("WRITEF"))
				{
					Rx = ensure(n.res, currLiveOut);
					if(!currLiveOut.contains(n.res)) free(Rx, currLiveOut);
					TinyNodes.add(new Node("sys writer", N(Rx, n.res), null, null, "TN"));
				}
				else if(n.opc.equals("WRITES"))
				{
					TinyNodes.add(new Node("sys writes", tinyFormat(n.res), null, null, "TN"));
				}

				else if(n.opc.equals("READI"))
				{
					Rz = allocate(n.res, currLiveOut);
					TinyNodes.add(new Node("sys readi", N(Rz, n.res), null, null, "TN"));
					makeDirty(Rz, n.res);
				}
				else if(n.opc.equals("READF"))
				{
					Rz = allocate(n.res, currLiveOut);
					TinyNodes.add(new Node("sys readr", N(Rz, n.res), null, null, "TN"));
					makeDirty(Rz, n.res);

				}
				else if(n.opc.equals("STOREI") || n.opc.equals("STOREF"))
				{	
					Rx = ensure(n.op1, currLiveOut);
					if(!currLiveOut.contains(n.op1)) free(Rx, currLiveOut);
					Rz = allocate(n.res, currLiveOut);
					TinyNodes.add(new Node("move", N(Rx, n.op1), N(Rz, n.res), null, "TN"));
					makeDirty(Rz, n.res);
				}
			}
			// Arithmetic Operations
			else
			{
				Rx = ensure(n.op1, currLiveOut);
				Ry = ensure(n.op2, currLiveOut);
				if(!currLiveOut.contains(n.op1)) free(Rx, currLiveOut);
				if(!currLiveOut.contains(n.op2)) free(Ry, currLiveOut);

				Rz = allocate(n.res, currLiveOut);
				//System.out.println(Rx);
				//System.out.println(Ry);
				//System.out.println(Rz);

				TinyNodes.add(new Node("move", N(Rx, n.op1), N(Rz, n.res), null, "TN"));
				TinyNodes.add(new Node(tinyOp, N(Ry, n.op2), N(Rz, n.res), null, "TN"));
				
				makeDirty(Rz, n.res);
			}

		}

	}
	public String N(int r,  String op)
	{
		//System.out.println("OKOKOK");
		//System.out.println(r);
		//System.out.println(op);
		//System.out.println(getType(op));
		//System.out.println(tinyFormat(op));

		if(getType(op).equals("REG")) 
		{
			return String.format("r%d", r);
		}
		else
		{ 
			//System.out.println(tinyFormat(op));
			//return tinyFormat(op);
			return String.format("r%d", r);
		}
	}
	*/

	/*
	public void generateGetTinyFromIRNodes()
	{
		String lopc = null, lop1 = null, lop2 = null, lres = null;
		HashSet<String> currLiveOut;
		int Rx, Ry, Rz;
		for(int count = 1 ; count <= Micro.currentFunction.irHandler.IREnum.size(); count++)
		{
			Node n = Micro.currentFunction.irHandler.IREnum.get(count);
			currLiveOut = n.outs;
			//System.out.println(Micro.currentFunction.irHandler.IREnum.size());
			String tinyOp = returnTinyOperator(n.opc);

			if(tinyOp.equals("FUNC"))
			{
				if (n.opc.equals("JSR"))
				{
					TinyNodes.add(new Node("push", null , null, "r0", "TN"));
					TinyNodes.add(new Node("push", null , null, "r1", "TN"));
					TinyNodes.add(new Node("push", null , null, "r2", "TN"));
					TinyNodes.add(new Node("push", null , null, "r3", "TN")); 
					TinyNodes.add(new Node("jsr" , n.res, null, null, "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r3", "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r2", "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r1", "TN"));
					TinyNodes.add(new Node("pop" , null , null, "r0", "TN"));

				}
				else if (n.opc.equals("PUSH")) 	TinyNodes.add(new Node("push", null, null, tinyFormat(n.res), "TN"));
				else if (n.opc.equals("POP")) 	TinyNodes.add(new Node("pop", null, null, tinyFormat(n.res), "TN"));

				else if (n.opc.equals("LINK")) 
				{
					TinyNodes.add(new Node("link", Micro.funcLocalSizes.get(currFuncName).toString(), null, null, "TN"));
					//linkStack.push(Integer.parseInt(n.res));
				}
				else if (n.opc.equals("RET"))
				{
					TinyNodes.add(new Node("unlnk", null, null, null, "TN"));
					TinyNodes.add(new Node("ret", null, null, null, "TN"));
					TinyNodes.add(new Node(";", null, null, null, "TN"));
					//linkStack.pop();
					
				}

			}
			else if(tinyOp.equals("COMPOP"))
			{
				if(n.opc.equals("COM"))
				{
					if(n.op1.equals("INT"))	lastComp = "cmpi";
					else if(n.op1.equals("FLOAT")) lastComp = "cmpr";
				}
				else if (n.opc.equals("LABEL"))
				{ 
					if(!n.res.contains("label")) currFuncName = n.res;
					TinyNodes.add(new Node("label", n.res, null, null, "TN"));
				}
				else if (n.opc.equals("JUMP")) TinyNodes.add(new Node("jmp", n.res, null, null, "TN"));
				else if (n.opc.equals("JSR")) TinyNodes.add(new Node("jsr", n.res, null, null, "TN"));
				else
				{ 
					TinyNodes.add(new Node(lastComp, tinyFormat(n.op1), tinyFormat(n.op2), null, "TN"));
					TinyNodes.add(new Node(getTinyCompOp(n.opc), n.res, null, null, "TN"));			
				}
			}	
			else if(tinyOp.equals("NOTOP"))
			{
				if(n.opc.equals("WRITEI")) TinyNodes.add(new Node("sys writei", tinyFormat(n.res), null, null, "TN"));
				else if(n.opc.equals("WRITEF")) TinyNodes.add(new Node("sys writer", tinyFormat(n.res), null, null, "TN"));
				else if(n.opc.equals("WRITES")) TinyNodes.add(new Node("sys writes", tinyFormat(n.res), null, null, "TN"));

				else if(n.opc.equals("READI")) TinyNodes.add(new Node("sys readi", tinyFormat(n.res), null, null, "TN"));
				else if(n.opc.equals("READF")) TinyNodes.add(new Node("sys readr", tinyFormat(n.res), null, null, "TN"));

				else if(n.opc.equals("STOREI") || n.opc.equals("STOREF"))
				{	

					TinyNodes.add(new Node("move", tinyFormat(n.op1), tinyFormat(n.res), null, "TN"));
				}
			}
			// Arithmetic Operations
			else
			{
				TinyNodes.add(new Node("move", tinyFormat(n.op1), tinyFormat(n.res), null, "TN"));
				TinyNodes.add(new Node(tinyOp, tinyFormat(n.op2), tinyFormat(n.res), null, "TN"));
			}

		}

	}
	*/
	public String tinyFormat(String irReg)
	{
		String tinyReg = null;
		if (irReg == null) return tinyReg;
		if(Micro.globalTable.localVariables.containsKey(irReg)) return irReg;
		else if(getType(irReg).equals("REG")) tinyReg = "r" + String.format("%d", Integer.parseInt(irReg.substring(2)) - 1);
		else if(getType(irReg).equals("LOC")) tinyReg = "$-" + irReg.substring(2);
		else if(getType(irReg).equals("PAR"))
		{
			//System.out.println(getCurrentLinkSize());
			//System.out.println(Integer.parseInt(irReg.substring(2)));
			tinyReg = String.format("$%d",  getCurrentLinkSize() - Integer.parseInt(irReg.substring(2)) );
		}
		else if(getType(irReg).equals("RET")) tinyReg = String.format("$%d", getCurrentLinkSize());

		else tinyReg = irReg;
		return tinyReg;
	}
	public String getTinyCompOp(String irOperator)
	{
		if     (irOperator.equals("GT"))   return "jgt";
		else if(irOperator.equals("LT"))   return "jlt";
		else if(irOperator.equals("GE"))   return "jge";
		else if(irOperator.equals("LE"))   return "jle";
		else if(irOperator.equals("NE"))   return "jne";
		else if(irOperator.equals("EQ"))   return "jeq";
		else return "jeq";
	}

	public String returnTinyOperator(String irOperator)
	{
		if     (irOperator.equals("ADDI"))   return "addi";
		else if(irOperator.equals("ADDF"))   return "addr";
		else if(irOperator.equals("MULTI"))  return "muli";
		else if(irOperator.equals("MULTF"))  return "mulr";
		else if(irOperator.equals("SUBI"))   return "subi";
		else if(irOperator.equals("SUBF"))   return "subr";
		else if(irOperator.equals("DIVI"))   return "divi";
		else if(irOperator.equals("DIVF"))   return "divr";

		else if(irOperator.equals("LABEL")) return "COMPOP";
		else if(irOperator.equals("JUMP"))  return "COMPOP";
		else if(irOperator.equals("GE"))    return "COMPOP";
		else if(irOperator.equals("LE"))    return "COMPOP";
		else if(irOperator.equals("GT"))    return "COMPOP";
		else if(irOperator.equals("LT"))    return "COMPOP";
		else if(irOperator.equals("NE"))    return "COMPOP";
		else if(irOperator.equals("EQ"))    return "COMPOP";
		else if(irOperator.equals("COM"))   return "COMPOP";

		else if(irOperator.equals("LINK"))  return "FUNC";
		else if(irOperator.equals("JSR"))   return "FUNC";
		else if(irOperator.equals("RET"))   return "FUNC";
		else if(irOperator.equals("PUSH"))  return "FUNC";
		else if(irOperator.equals("POP"))   return "FUNC";


		else return "NOTOP";
	}

	public String getType(String param)
	{
		if(param == null) return "NULL";
		else if(param.contains(".")) return "FLOAT";
		else if(param.contains("$T")) return "REG";
		else if(param.contains("$L")) return "LOC";
		else if(param.contains("$P")) return "PAR";
		else if(param.contains("$R")) return "RET";

		else if(param.charAt(0) >= '0' && param.charAt(0) <= '9') return "INT"; 
		else return "ID";
	}
}
