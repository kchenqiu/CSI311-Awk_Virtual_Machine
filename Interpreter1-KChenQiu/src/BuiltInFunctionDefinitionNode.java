import java.util.LinkedList;

public class BuiltInFunctionDefinitionNode extends FunctionDefinitionNode{
	private boolean vardaic;
	private InterpreterArrayDataType iadt;

	public BuiltInFunctionDefinitionNode(String name, boolean vardiac ,LinkedList<String> parameters,
			LinkedList<StatementNode> statements) {
		super(name, parameters, statements);
		this.vardaic = vardiac;
	}
	
	public void setIADT(InterpreterArrayDataType iadt) {
		this.iadt = iadt;
	}
	
	public InterpreterArrayDataType getIADT() {
		return iadt;
	}

	public boolean getVardiac() {
		return vardaic;
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
