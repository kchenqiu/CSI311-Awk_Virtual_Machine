
public class ReturnType {
	enum Type{NONE, NORMAL, BREAK, CONTINUE, RETURN};
	
	Type type;
	String string;
	
	public ReturnType(Type type) {
		this.type = type;
	}
	
	public ReturnType(String string, Type type) {
		this.string = string;
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
	public String getString() {
		return string;
	}
	
	public String toString() {
		return type.toString() + " " + string;
	}
}
