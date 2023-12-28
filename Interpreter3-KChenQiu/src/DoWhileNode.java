import java.util.LinkedList;
import java.util.Optional;

public class DoWhileNode extends StatementNode{
	Node condition;
	LinkedList<StatementNode> statements;
	
	public DoWhileNode(Optional<Node> condition, LinkedList<StatementNode> statements) {
		this.condition = condition.get();
		this.statements = statements;
	}
	
	public Node getCondition() {
		return condition;
	}
	
	public LinkedList<StatementNode> getStatements(){
		return statements;
	}
	
	public String toString() {
		return "Do While: " + condition.toString() + ": " + statements.toString();
	}
}
