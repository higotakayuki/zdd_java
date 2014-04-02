package zdd;


public class ValueState {
	public int value;
	public State state;
	public enum State{
		ZERO,ONE,ANY
	}
	public ValueState(int value, State state) {
		super();
		this.value = value;
		this.state = state;
	}
	@Override
	public String toString() {
		return ""+value+state.toString();
	}	
}
