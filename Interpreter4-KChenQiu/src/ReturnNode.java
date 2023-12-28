import java.util.Optional;

public class ReturnNode extends StatementNode{
	Node parameter;
	
	public ReturnNode(Optional<Node> parameter) {
		this.parameter = parameter.get();
	}
	
	public Node getParameter() {
		return parameter;
	}
	
	public String toString() {
		return "Return: " + parameter.toString();
	}
}
