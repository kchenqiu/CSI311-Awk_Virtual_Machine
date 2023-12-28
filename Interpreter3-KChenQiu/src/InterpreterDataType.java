
public class InterpreterDataType {
	String string;
	
	public InterpreterDataType(String string) {
		this.string = string;
	}
	
	public InterpreterDataType() {
		
	}
	
	public String toString() {
		if(!string.equals(null)) {
			return string;
		}
		else {
			return null;
		}
	}

}
