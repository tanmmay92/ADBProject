package nyu.courant.mis.adb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds transaction information
 * @author Aditya Kapoor, Tanmmay Mahendru
 *
 */
public class Transaction {

	private String transID;
	private int transType;
	private int startTime;
	private List<Operation> operations;
	public Set<Integer> sitesVisited;
	public ArrayList<LockTable> existingLock;
	public Set<String> presentTransWaitsFor;
	public String waitsForPresentTrans;
	
	public final static int RW = 0; // READ-WRITE
	public final static int RO = 1; // READ-ONLY

	public Transaction(String transID, int startTime, int transType) {
		this.transID = transID;
		this.transType = transType;
		this.startTime = startTime;
		this.operations = new ArrayList<Operation>();
		this.sitesVisited = new HashSet<Integer>();
		this.existingLock = new ArrayList<LockTable>();
		presentTransWaitsFor = new HashSet<String>();
		waitsForPresentTrans = null;
	}

	public String getTransID() {
		return transID;
	}

	public void setTransID(String transID) {
		this.transID = transID;

	}

	public int getTransType() {
		return transType;
	}

	public void setTransType(int transType) {
		this.transType = transType;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	public void addOperation(Operation op) {
		this.operations.add(op);
	}

	public ArrayList<LockTable> getLockedList() {
		return this.existingLock;
	}

	/**
	 * Returns number of locks on Transaction
	 * @param type
	 * @param varID
	 * @return
	 */
	public int NumOfLocksOnTrans(int type, int varID) {
		int number = 0;
		for (LockTable ltable:this.existingLock) {
			if (ltable.getVarID() == varID && ltable.getLockType() == type) {
				number++;
			}
		}
		return number;
	}

	/**
	 * Returns if Transaction has WL on given variable
	 * @param varID
	 * @return
	 */
	public boolean checkTransHasWL(int varID) {
		for (LockTable ltable:this.existingLock) {
			if (ltable.getLockType() == LockTable.WL && ltable.getVarID() == varID) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds Lock to existing lock on Variable
	 * @param varID
	 * @param type
	 */
	public void addLockToExistingLock(int varID, int type) {
		LockTable temp = new LockTable(varID, this.transID, type);
		this.existingLock.add(temp);
	}

	/**
	 * Removes lock from existing lock on variable
	 * @param varID
	 */
	public void removeLockFromExistingLock(int varID) {
		int index = -1;
		for (int i = 0; i < this.existingLock.size(); i++) {
			if (this.existingLock.get(i).getVarID() == varID) {
				index = i;
			}
		}
		if (index != -1) {
			this.existingLock.remove(index);
		}
	}

	@Override
	public String toString() {
		StringBuilder value = new StringBuilder();
		value.append("Transaction ID-> " + this.transID + "\n");
		value.append("Transaction Type-> " + this.transType + "\n");
		value.append("Start time-> " + this.startTime + "\n");
		return value.toString();
	}
}
