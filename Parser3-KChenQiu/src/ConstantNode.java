
public class ConstantNode extends Node{
	
	String value;
	//constructor
	public ConstantNode(String value) {
		this.value = value;	
	}
	//prints out the string value
	public String toString() {
		return value;
	}
}
