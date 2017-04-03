import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.Arrays;
import java.lang.*;

public class CustomErrorStrategy extends DefaultErrorStrategy
{
	public void reportError(Parser recognizer, RecognitionException e)
	{
		throw e;
	}
	
}
