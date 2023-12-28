import java.util.LinkedList;
import java.util.Optional;

public class VariableReferenceNode extends Node{
	String name;
	Optional<Node> index;
	
	//constructor for variable reference node with a name and index
	public VariableReferenceNode(String name, Optional<Node> index) {
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
	public Node getIndex(){
		if(index != null && index.isEmpty()) {
			return index.get();
		}
		else {
			return null;
		}
	}
	
	//to string method that returns the name and index in a string
	public String toString() {
		if(index != null ) {
			return name + index.get().toString();
		}
		else {
			return name;
		}
	}
}
