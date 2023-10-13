
public class AssignmentNode extends Node{
	Node target;
	Node expression;
	
	public AssignmentNode(Node target, Node expression){
		this.target = target;
		this.expression = expression;
	}
	
	public Node getTarget() {
		return target;
	}
	
	public Node getExpression() {
		return expression;
	}
	
	public String toString() {
		return target.toString() + " = " + expression.toString();
	}
	
}
