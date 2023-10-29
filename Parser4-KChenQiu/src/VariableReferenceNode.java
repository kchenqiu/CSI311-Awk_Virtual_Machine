import java.util.LinkedList;
import java.util.Optional;

public class VariableReferenceNode extends Node{
	String name;
	LinkedList<Node> index;
	
	//constructor for variable reference node with a name and index
	public VariableReferenceNode(String name, LinkedList<Node> index) {
		this.name = name;
		this.index= index;
	}
	
	//constructor for variable reference node with only a name
	public VariableReferenceNode(String name) {
		this.name = name;
	}
	
	//accessor method for the name
	public String getName() {
		return name;
	}
	
	//accessor method for the Index
	public LinkedList<Node> getIndex(){
		return index;
	}
	
	//to string method that returns the name and index in a string
	public String toString() {
		if(index != null ) {
			return name + index.toString();
		}
		else {
			return name;
		}
	}
}
