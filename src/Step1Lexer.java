import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.Arrays;

public class Step1Lexer 
{
	public static String getTokenName(int tokenNum)
	{
		switch(tokenNum)
		{
			case MicroLexer.STRINGLITERAL:
				return "STRINGLITERAL";
			case MicroLexer.IDENTIFIER:
				return "IDENTIFIER";
			case MicroLexer.FLOATLITERAL:
				return "FLOATLITERAL";
			case MicroLexer.INTLITERAL:
				return "INTLITERAL";
			
			// KEYWORDS
			case MicroLexer.KEY_PROGRAM:
				return "KEYWORD";
			case MicroLexer.KEY_BEGIN:
				return "KEYWORD";
			case MicroLexer.KEY_END:
				return "KEYWORD";
			case MicroLexer.KEY_FUNCTION:
				return "KEYWORD";
			case MicroLexer.KEY_READ:
				return "KEYWORD";
			case MicroLexer.KEY_WRITE:
				return "KEYWORD";
			case MicroLexer.KEY_IF:
				return "KEYWORD";
			case MicroLexer.KEY_ESLIF:
				return "KEYWORD";
			case MicroLexer.KEY_ENDIF:
				return "KEYWORD";
			case MicroLexer.KEY_DO:
				return "KEYWORD";
			case MicroLexer.KEY_WHILE:
				return "KEYWORD";
			case MicroLexer.KEY_CONTINUE:
				return "KEYWORD";
			case MicroLexer.KEY_BREAK:
				return "KEYWORD";
			case MicroLexer.KEY_RETURN:
				return "KEYWORD";
			case MicroLexer.KEY_INT:
				return "KEYWORD";
			case MicroLexer.KEY_VOID:
				return "KEYWORD";
			case MicroLexer.KEY_STRING:
				return "KEYWORD";
			case MicroLexer.KEY_FLOAT:
				return "KEYWORD";
			case MicroLexer.KEY_TRUE:
				return "KEYWORD";
			case MicroLexer.KEY_FALSE:
				return "KEYWORD";

			// OPERATORS
			case MicroLexer.OP_ASS:
				return "OPERATOR";
			case MicroLexer.OP_NEQ:
				return "OPERATOR";
			case MicroLexer.OP_LEQ:
				return "OPERATOR";
			case MicroLexer.OP_GEQ:
				return "OPERATOR";
			case MicroLexer.OP_ADD:
				return "OPERATOR";
			case MicroLexer.OP_SUB:
				return "OPERATOR";
			case MicroLexer.OP_MUL:
				return "OPERATOR";
			case MicroLexer.OP_DIV:
				return "OPERATOR";
			case MicroLexer.OP_EQL:
				return "OPERATOR";
			case MicroLexer.OP_LT:
				return "OPERATOR";
			case MicroLexer.OP_GT:
				return "OPERATOR";
			case MicroLexer.OP_LPR:
				return "OPERATOR";
			case MicroLexer.OP_RPR:
				return "OPERATOR";
			case MicroLexer.OP_SMC:
				return "OPERATOR";
			case MicroLexer.OP_COM:
				return "OPERATOR";
			default:
				return null;
		}
	}
}
 

