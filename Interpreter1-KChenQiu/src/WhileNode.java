import java.util.LinkedList;
import java.util.Optional;

public class WhileNode extends StatementNode{
	Node condition;
	LinkedList<StatementNode> statements;
	
	public WhileNode(Optional<Node> condition, LinkedList<StatementNode> statements) {
		this.condition = condition.get();
		this.statements = statements;
	}

	public String toString() {
		return  "While: " + condition.toString() + " " + statements.toString();
	}
}
