package nyu.courant.mis.adb;

/**
 * This class holds the information of an operation
 * Type of operation, 0- read, 1-write, 2-commit
 * @author Aditya Kapoor, Tanmmay Mahendru
 */

public class Operation {

	private int value;
	private int time;
	private int varIndex;
	private int operationType;
	
	public final static int READ = 0;
	public final static int WRITE = 1;
	public final static int COMMIT = 2;

	public Operation(int value, int varIndex, int time, int operationType) {
		this.value = value;
		this.time = time;
		this.varIndex = varIndex;
		this.operationType = operationType;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getVarIndex() {
		return varIndex;
	}

	public void setVarIndex(int varIndex) {
		this.varIndex = varIndex;
	}

	public int getOperationType() {
		return operationType;
	}

	public void setOperationType(int operationType) {
		this.operationType = operationType;
	}

}