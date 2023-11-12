import java.util.HashMap;

public class InterpreterArrayDataType {
	HashMap<String, InterpreterDataType> iadt;
	
	public InterpreterArrayDataType() {
		iadt = new HashMap<>();
	}
	
	public void addEntry(String string, InterpreterDataType idt) {
		iadt.put(string, idt);
	}
	
	public InterpreterDataType getIDT(String string) {
		return iadt.get(string);
	}
	
	public String toString() {
		return iadt.toString();
	}
}
