import java.util.Optional;

public class OperationNode extends Node{
	//enums of operations
	enum Operations{
        EQ, NE, LT, LE, GT, GE, AND, OR, NOT, MATCH, NOTMATCH, DOLLAR,
        PREINC, POSTINC, PREDEC, POSTDEC, UNARYPOS, UNARYNEG, IN,
        EXPONENT, ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULAR, CONCATENATION
	}
	
	Node left;
	Optional<Node> right;
	Operations operation;

	//constructor for operation node with a left right and operation
	public OperationNode(Node left, Optional<Node> right, Operations operation) {
		this.left = left;
		this.right = right;
		this.operation = operation;
	}
	
	//constructor for operation node with only a left and operation
	public OperationNode(Node left, Operations operation) {
		this.left = left;
		this.operation = operation;
	}
	
	//accessor method for the left node
	public Node getLeftNode() {
		return left;
	}
	
	//accessor method for the right node
	public Node getRightNode(){
		if(right != null && right.isPresent()) {
			return right.get();
		}
		else {
			return null;
		}
	}
	
	//accessor method for the operation
	public String getOperation() {
		return operation.toString();
	}
	
	//returns the left then operation then the right
	public String toString() {
		if(right != null && right.isPresent()) {
			return left.toString()+ " " + operation.toString()+ " " + right.get().toString();
		}
		else {
			return left.toString() + " " + operation.toString();
		}
	}
}
