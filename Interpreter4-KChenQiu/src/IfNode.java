import java.util.LinkedList;
import java.util.Optional;

public class IfNode extends StatementNode{
	private Optional<Node> condition;
	private LinkedList<StatementNode> statements;
	
	public IfNode(Optional<Node> condition, LinkedList<StatementNode> statements) {
		this.condition = condition;
		this.statements = statements;
	}
	
	public Node getCondition() {
		return condition.get();
	}
	
	public LinkedList<StatementNode> getStatements(){
		return statements;
	}

	public String toString() {
		if(condition.isPresent() && !condition.equals(null)) {
			return "If: " + condition.get().toString() + " " + statements.toString();
		}
		else {
			return "Else: " + statements.toString();
		}
	}
}
