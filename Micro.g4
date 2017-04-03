grammar Micro;

/* Program */
program
	: 'PROGRAM'
	  {
		Micro.exprParserStack.push(new ExprParser());
	  }
	  id 'BEGIN' pgm_body { Micro.currentFunction.emptyCodeStack(); } 'END' EOF
	;
id:
	IDENTIFIER
	;
pgm_body
	: 
	  {
	  }
	  decl
	  func_declarations
	  { 
	  } 

	;
decl
	: string_decl decl
	| var_decl decl
	|
	;

/* Global String Declaration */
string_decl
	: 'STRING' id ':=' str ';' 
	  { 
		if(Micro.currentFunction == null) Micro.globalTable.addSymbol("STRING", $id.text, $str.text);
		else 				  Micro.currentFunction.insertSymbolInTop("STRING", $id.text, $str.text);			   
	  }
	;
str
	: STRINGLITERAL
	;

/* Variable Declaration */
var_decl
	: var_type id_list ';' 
	  { 
		if(Micro.currentFunction == null) Micro.globalTable.addSymbol($var_type.text, $id_list.text, null); 
	  	else				  {Micro.currentFunction.insertSymbolInTop($var_type.text, $id_list.text, null);}  				  
	  }	
	;
var_type
	: 'FLOAT'
	| 'INT'
	;
any_type
	: var_type
	| 'VOID' 
	;
id_list
	: id id_tail
	;
id_tail
	: ',' id id_tail
	| //empty
	;

/* Function Paramater List */
param_decl_list
	: param_decl param_decl_tail
	| // empty
	;
param_decl
	: var_type id
	  { Micro.currentFunction.addParameter($var_type.text, $id.text, null); }
	;
param_decl_tail
	: ',' param_decl param_decl_tail
	| //empty
	;

/* Function Declarations */
func_declarations
	: func_decl func_declarations
	| //empty
	;
func_decl
	: 'FUNCTION' 
	  any_type 
	  id
	  {  
		FunctionCode newFunc = new FunctionCode($id.text, $any_type.text);
		Micro.functions.add(newFunc);
		Micro.currentFunction = newFunc;
		Micro.funcTypeHashMap.put(Micro.currentFunction.functionName, Micro.currentFunction.retType);

		//System.out.println("HE");
		//System.out.println(Micro.currentFunction.functionName);
		//System.out.println(Micro.currentFunction.retType);
	  } 
	  // The contructor adds the base scope
	  '(' 
		
		param_decl_list
		
	  ')' 'BEGIN' 
	  func_body 
	  { 
		//System.out.println("Hello");
		//System.out.println(Micro.currentFunction.functionName);
		//System.out.println($func_body.text);
			
		Micro.currentFunction.emptyCodeStack(); 
		Micro.currentFunction.linkSize = Micro.currentFunction.scopeStack.peek().localVariables.size();
	  } 
	  'END'
	 	
	
	;
func_body
	: decl 
	  stmt_list
	  { //SymbolTableStack.finalStack.push(SymbolTableStack.tableStack.pop());/*.printSymbols()*/; 
	  }
	; 

/* Statement List */
stmt_list
	: stmt stmt_list
	| //empty
	;
stmt
	: base_stmt 
	| if_stmt
	| do_while_stmt
	;
base_stmt
	:
	  assign_stmt
	| read_stmt
	| write_stmt
	| return_stmt
	;

/* Basic Statements */
assign_stmt
	: 
	  { 
		Micro.currentFunction.insertBaseBlock(); 
		//System.out.println("READHELLO"); 
	  } 
	  assign_expr ';'
	;
assign_expr
	: id
	  { Micro.currentFunction.getCurrentCodeBlock().ast.addTerminalNode("IDENTIFIER", $id.text) ;} 
	  ':='
	  { 
		ExprParser currParser = Micro.exprParserStack.pop();
		currParser.reset();
		Micro.exprParserStack.push(currParser); 
	  } 
	  expr  
	  { Micro.currentFunction.getCurrentCodeBlock().ast.addAssignmentNode(Micro.exprParserStack.peek().getExprTree()) ;}
	;

read_stmt
	: 
	  'READ'
	  { 
		Micro.currentFunction.insertBaseBlock(); 
		//System.out.println("READHELLO"); 
	  } 
	  '(' id_list ')'  {  Micro.currentFunction.getCurrentCodeBlock().ast.addIO("READ", $id_list.text) ;}
	  ';'
	; 
write_stmt
	: 'WRITE'
	  { 
		Micro.currentFunction.insertBaseBlock(); 
		//System.out.println("READHELLO"); 
	  } 	
	  '(' id_list ')'  
	  { 
		Micro.currentFunction.getCurrentCodeBlock().ast.addIO("WRITE", $id_list.text) ;
		//System.out.println(";MicroHELLO");
		//System.out.println(Micro.currentFunction.functionName);
		//System.out.println(Micro.currentFunction.functionName);
	  }
	  ';' 
	;
return_stmt
	: 'RETURN'
	  { 
		Micro.currentFunction.insertBaseBlock(); 
		//System.out.println("READHELLO");
		ExprParser currParser = Micro.exprParserStack.pop();
		currParser.reset();
		Micro.exprParserStack.push(currParser);  
	  } 	  
	   expr 
	  { Micro.currentFunction.getCurrentCodeBlock().ast.addReturnNode(Micro.exprParserStack.peek().getExprTree()) ;}
	  ';'
	;

/* Expressions */
expr
	: 
	  expr_prefix factor
	;
expr_prefix
	: expr_prefix factor addop 
	| //empty
	;
factor
	: factor_prefix postfix_expr 
	;
factor_prefix
	: factor_prefix
	  postfix_expr
	  
	  mulop
	| // empty
	;
postfix_expr
	: primary 
	| call_expr
	;
call_expr
	: 
	  //{Micro.currentFunction.insertBaseBlock(); System.out.println("CALLEXPRHELLO");}
	  id
	  { 
		Micro.isFuncExpr = true;	 
		ExprNode func = new ExprNode($id.text, "FUNCTION");
		Micro.funcNodeStack.push(func); 
		//Caller cal = new Caller($id.text, Micro.currentFuction.getLocalVariablesCount());
		Micro.currentFunction.funcsCalled.add($id.text);
	  } 
 
	  '('
	  {Micro.exprParserStack.push(new ExprParser());} 
	  expr_list
	  {Micro.exprParserStack.pop(); } 
	  ')'
	  {  
		ExprParser currParser = Micro.exprParserStack.pop();
		currParser.exprList.add(new ExprNode("(", "LP"));
		currParser.exprList.add(Micro.funcNodeStack.pop());
		currParser.exprList.add(new ExprNode(")", "RP"));
		Micro.exprParserStack.push(currParser);
		Micro.isFuncExpr = false;
	  }
	;
expr_list
	: 
	  {
		ExprParser currParser = Micro.exprParserStack.pop();
		currParser.reset();
		Micro.exprParserStack.push(currParser);
	  }
	  expr
	  {
		if(Micro.exprParserStack.peek().getExprTree() != null) 
		{
			ExprNode funcNode = Micro.funcNodeStack.pop(); 
			funcNode.params.add(Micro.exprParserStack.peek().getExprTree());
			Micro.funcNodeStack.push(funcNode);
		}
	  }
	  expr_list_tail
	| // empty
	;
expr_list_tail
	: ',' 
	  {
		ExprParser currParser = Micro.exprParserStack.pop();
		currParser.reset();
		Micro.exprParserStack.push(currParser);
	  }
	  expr
	  {
		ExprNode funcNode = Micro.funcNodeStack.pop(); 
		funcNode.params.add(Micro.exprParserStack.peek().getExprTree());
		Micro.funcNodeStack.push(funcNode);
	  }
	  expr_list_tail
	| // empty
	;
primary
	: '(' 
	  { 
		ExprParser currParser = Micro.exprParserStack.pop();
		currParser.exprList.add(new ExprNode("(", "LP")); 
		Micro.exprParserStack.push(currParser);
	  }  
	  expr 
	  ')' 
	  { 
		ExprParser currParser2 = Micro.exprParserStack.pop();
		currParser2.exprList.add(new ExprNode(")", "RP")); 
		Micro.exprParserStack.push(currParser2);
	  } 
	| id 
	  { 
		ExprParser currParser3 = Micro.exprParserStack.pop();
		currParser3.exprList.add(new ExprNode($id.text, "IDENTIFIER"));
		Micro.exprParserStack.push(currParser3);
	  }
	| INTLITERAL 
	  { 
		ExprParser currParser4 = Micro.exprParserStack.pop();
		currParser4.exprList.add(new ExprNode($INTLITERAL.text, "INT"));
		Micro.exprParserStack.push(currParser4);
	  }
	| FLOATLITERAL 
	  { 
		ExprParser currParser5 = Micro.exprParserStack.pop();
		currParser5.exprList.add(new ExprNode($FLOATLITERAL.text, "FLOAT")); 
		Micro.exprParserStack.push(currParser5); 
	  }
	;
addop
	:
	  '+' 
	  { 
		ExprParser currParser = Micro.exprParserStack.pop();
		currParser.exprList.add(new ExprNode("+", "OPADD"));
		Micro.exprParserStack.push(currParser);
	  }
	| '-' 
	  { 
		ExprParser currParser2 = Micro.exprParserStack.pop();
		currParser2.exprList.add(new ExprNode("-", "OPADD"));
		Micro.exprParserStack.push(currParser2);
	  }
	;
mulop
	: 
	  '*' 
	  { 
		ExprParser currParser = Micro.exprParserStack.pop();
		currParser.exprList.add(new ExprNode("*", "OPMUL"));
		Micro.exprParserStack.push(currParser);
	  }
	| '/' 
	  { 
		ExprParser currParser2 = Micro.exprParserStack.pop();
		currParser2.exprList.add(new ExprNode("/", "OPMUL"));
		Micro.exprParserStack.push(currParser2);
	  }
	;

/* Complex Statements and Condition */ 
if_stmt
	: 'IF'
	  { Micro.currentFunction.insertIFBlock() ;}
	  '(' cond ')'
	  decl
	  stmt_list
	  else_part
	  'ENDIF'
	  {
		 Micro.currentFunction.updateIFStack();	  
	  }
	 
	;
else_part
	: 'ELSIF'
	  { Micro.currentFunction.insertELSIFBlock() ;}
	  '(' cond ')'
	  decl 
	  stmt_list 
	  else_part
	| // empty
	;
cond
	: 
	  { 
		ExprParser currParser = Micro.exprParserStack.pop();
		currParser.reset(); 
		Micro.exprParserStack.push(currParser); 	  
	  }
	  expr 
	  { Micro.currentFunction.getCurrentCodeBlock().ast.addCond(Micro.exprParserStack.peek().getExprTree(), null, "1") ;}
	  compop 
	  {
		ExprParser currParser2 = Micro.exprParserStack.pop();
		currParser2.reset(); 
		Micro.exprParserStack.push(currParser2); 
	  }
	  expr
	  { Micro.currentFunction.getCurrentCodeBlock().ast.addCond(Micro.exprParserStack.peek().getExprTree(), $compop.text, "2") ;}
	| 'TRUE'  { Micro.currentFunction.getCurrentCodeBlock().ast.addCond(null, "TRUE", "VAL") ;}
	| 'FALSE' { Micro.currentFunction.getCurrentCodeBlock().ast.addCond(null, "FALSE", "VAL") ;}
	;

compop
	: '<' 
	| '>' 
	| '=' 
	| '!=' 
	| '<=' 
	| '>='
	;
do_while_stmt
	: 'DO'
	  { Micro.currentFunction.insertDOBlock() ;}
	  decl
	  stmt_list 
	  'WHILE' 
	  { Micro.currentFunction.insertWHILEBlock() ;}
	  '(' cond ')' ';'
	;

// Lexer Tokens


COMMENT: '--'(~('\r'|'\n'))* -> skip;

// Keywords
KEY_PROGRAM	: 'PROGRAM';
KEY_BEGIN	: 'BEGIN';
KEY_END		: 'END';
KEY_FUNCTION	: 'FUNCTION';
KEY_READ	: 'READ';
KEY_WRITE	: 'WRITE';
KEY_IF		: 'IF';
KEY_ESLIF	: 'ELSIF';
KEY_ENDIF	: 'ENDIF';
KEY_DO		: 'DO';
KEY_WHILE	: 'WHILE';
KEY_CONTINUE	: 'CONTINUE';
KEY_BREAK	: 'BREAK';
KEY_RETURN	: 'RETURN';
KEY_INT		: 'INT';
KEY_VOID	: 'VOID';
KEY_STRING	: 'STRING';
KEY_FLOAT	: 'FLOAT';
KEY_TRUE	: 'TRUE';
KEY_FALSE	: 'FALSE';


STRINGLITERAL: '"'(~('\n'|'\r'))*?'"';

// Operators
OP_ASS		: ':=';
OP_NEQ		: '!=';
OP_LEQ		: '<=';
OP_GEQ		: '>=';

OP_ADD		: '+';
OP_SUB		: '-';
OP_MUL		: '*';
OP_DIV		: '/';

OP_EQL		: '=';
OP_LT		: '<';
OP_GT		: '>';
OP_LPR		: '(';
OP_RPR		: ')';
OP_SMC		: ';';
OP_COM		: ',';

IDENTIFIER: [A-z]([A-z0-9])*;
INTLITERAL: [0-9]+;
FLOATLITERAL: [0-9]*?['.'][0-9]*;

WHITESPACE:  (' ' | '\t' | '\n' | '\r' | '\f')+ -> skip; 


