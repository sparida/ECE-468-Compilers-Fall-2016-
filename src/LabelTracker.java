public class LabelTracker
{
	public static int labelNum = 0;
	public static String currentLabel  = null;
	public static String genNewLabel()
	{
		labelNum++;
		currentLabel =  String.format("label%s", labelNum);
		return currentLabel;
	}
}
