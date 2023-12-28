import java.util.Optional;

public class DeleteNode extends StatementNode{
	Node parameter;
	
	public DeleteNode(Optional<Node> parameter) {
		this.parameter = parameter.get();
	}
	
	public Node getDelete() {
		return parameter;
	}

	public String toString() {
		return "Delete " + parameter.toString();
	}
}
