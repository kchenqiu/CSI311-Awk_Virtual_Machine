public class StringHandler
{
	//initializes private variables
	private String docHold;
	private int index;
	
	//constructor so the string handler has access to the document
	public StringHandler(String docHold) {
		this.docHold = docHold;
		this.index = 0;
	}
	
	//method to see an i amount of characters ahead without moving the position of the string handler
	public char peek(int i) {
		if(index + i < docHold.length()) {
			return docHold.charAt(index + i);
		}
		else
		{
			return ' ';
		}
	}
	
	//method to see an i amount of strings ahead without moving the position of the string handler
	public String PeekString(int i) {
		if(index +i < docHold.length()) {
			return docHold.substring(index, index+i);
		}
		else
		{
			return " ";
		}
	}
	
	//returns the character the string handler is on and moves to the next character
	public char GetChar() {
		if(index < docHold.length()) {
			char next = docHold.charAt(index);
			index++;
			return next;
		}
		else
		{
			return ' ';
		}
	}
	
	//moves the string handler i amount of characters ahead
	public void Swallow(int i) {
		index = index + i;
		if(index > docHold.length()) {
			index = docHold.length();
		}
	}
	
	//returns true if the string handler is at the end of the document
	public boolean IsDone() {
		if(index >= docHold.length()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	//returns whatever is left in the document
	public String Remainder() {
		if(index < docHold.length()) {
			return docHold.substring(index);
		}
		else
		{
			return "";
		}
	}

}