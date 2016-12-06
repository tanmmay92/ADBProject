package nyu.courant.mis.adb;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds Variable Information
 * 
 * @author Aditya Kapoor, Tanmmay Mahendru
 */
public class Variable {
	private int ID;
	private int value;
	private int presentVal;
	private int time;
	
	public final static int VARIABLES = 20;

	private ArrayList<Variable> vh;

	private boolean availableForRead;
	private boolean copy;

	public Variable(int id) {
		this.ID = id;
		this.value = id * 10;
		vh = new ArrayList<Variable>();
		Variable initial = new Variable(0, this.value);
		vh.add(initial);
		this.availableForRead = true;
		if (id % 2 != 0) {
			this.copy = false;
		} else {
			this.copy = true;
		}
		this.setPresentVal(this.getValue());
	}
	
	public Variable(int time, int value) {
		this.time = time;
		this.value = value;
	}

	public int getPresentVal() {
		return presentVal;
	}

	public void setPresentVal(int presentVal) {
		this.presentVal = presentVal;
	}

	public boolean getAvailableForRead() {
		return this.availableForRead;
	}

	public void setAvailableForRead(boolean val) {
		this.availableForRead = val;
	}

	public boolean isCopied() {
		return copy;
	}

	public void setCopied(boolean copy) {
		this.copy = copy;
	}

	public int getID() {
		return this.ID;
	}

	public int getValue() {
		return this.value;
	}

	public void setValue(int v) {
		this.value = v;
	}

	public List<Variable> getVariableHistory() {
		return this.vh;
	}
	
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void addVariableHistory(int time, int value) {
		Variable newData = new Variable(time, value);
		this.vh.add(newData);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Variable)) {
			return false;
		}
		Variable data = (Variable) o;
		return this.getID() == data.getID();
	}

	@Override
	public String toString() {
		return "x" + this.ID + " " + this.value + "\n";
	}

	/**
	 * Enhanced toString
	 * 
	 * @return
	 */
	public String stringVariableTimeVal() {
		StringBuilder answer = new StringBuilder();
		for (int i = 0; i < this.vh.size(); i++) {
			answer.append("|");
			answer.append(vh.get(i).toString());
			answer.append("|");
		}
		answer.append("\n");
		return answer.toString();
	}
}