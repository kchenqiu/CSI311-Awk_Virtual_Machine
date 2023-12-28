import java.util.LinkedList;

public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNode{
	private boolean variadic;
	private InterpreterArrayDataType iadt;

	public BuiltInFunctionDefinitionNode(String name, boolean variadic ,LinkedList<Node> parameters,
			LinkedList<StatementNode> statements) {
		super(name, parameters, statements);
		this.variadic = variadic;
	}
	
	public void setIADT(InterpreterArrayDataType iadt) {
		this.iadt = iadt;
	}
	
	public InterpreterArrayDataType getIADT() {
		return iadt;
	}

	public boolean getVariadic() {
		return variadic;
	}
	
	public String toString() {
		if(!iadt.equals(null)) {
			return iadt.toString() + super.toString();
		}
		else {
			return super.toString();
		}
	}
}
