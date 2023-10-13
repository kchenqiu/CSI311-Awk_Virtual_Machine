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
	public Optional<Node> getIndex(){
		return index;
	}
	
	//to string method that returns the name and index in a string
	public String toString() {
		return name + index.toString();
	}
}
