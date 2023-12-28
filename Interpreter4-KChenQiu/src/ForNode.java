import java.util.LinkedList;

public class ForNode extends StatementNode{
	LinkedList<Node> condition;
	LinkedList<StatementNode> statements;

	public ForNode(LinkedList<Node> condition, LinkedList<StatementNode> statements) {
		this.condition = condition;
		this.statements = statements;
	}
	
	public LinkedList<Node> getCondition(){
		return condition;
	}
	
	public LinkedList<StatementNode> getStatements(){
		return statements;
	}

	public String toString() {
		return "For " + condition.toString() + " " + statements.toString();
	}
}
