
public class TernaryNode extends Node{
	Node expression;
	Node trueCase;
	Node falseCase;
	
	public TernaryNode(Node expression, Node trueCase, Node falseCase) {
		this.expression = expression;
		this.trueCase = trueCase;
		this.falseCase = falseCase;
	}
	
	public String getExpression() {
		return expression.toString();
	}
	
	public String getTrueCase() {
		return trueCase.toString();
	}
	
	public String getFalseCase() {
		return falseCase.toString();
	}
	
	public String toString() {
		return expression + " "+ trueCase.toString() + " " + falseCase.toString();
	}
	
}
