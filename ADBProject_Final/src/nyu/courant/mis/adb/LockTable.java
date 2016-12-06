package nyu.courant.mis.adb;

import java.util.ArrayList;

/**
 * Holds the list of locks lockType: 1-Read lock, 2- Write lock
 * 
 * @author Aditya Kapoor, Tanmmay Mahendru
 */

public class LockTable {
	public ArrayList<LockTable> lockTable;
	
	public int lockType;
	public String transID;
	public int varID;
	
	public final static int NL = 0; // NO-LOCK
	public final static int RL = 1; // READ-LOCK
	public final static int WL = 2; // WRITE-LOCK

	public LockTable() {
		this.lockTable = new ArrayList<LockTable>();
	}
	
	public LockTable(int v, String t, int type) {
		this.lockType = type;
		this.transID = t;
		this.varID = v;
	}
	
	public int getLockType() {
		return this.lockType;
	}

	public void setLockType(int t) {
		this.lockType = t;
	}

	public String getTransID() {
		return this.transID;
	}

	public void setTransID(String s) {
		this.transID = s;
	}

	public int getVarID() {
		return this.varID;
	}

	public void setVarID(int v) {
		this.varID = v;
	}

	/**
	 * Checks if RL is available on given Variable
	 * @param varID
	 * @param transID
	 * @return
	 */
	public boolean checkRLavailability(int varID, String transID) {
		ArrayList<LockTable> locks = this.getVarLocksAll(varID);
		if (locks.size() == 0) {
			return true;
		}
		boolean hasWriteLock = false;
			
		for (LockTable lock: locks) {
			if (lock.getLockType() == LockTable.WL && !lock.getTransID().equals(transID)) {
				hasWriteLock = true;
			}
		}
		
		if (!hasWriteLock) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if lock table contains a lock on a Variable for Transaction
	 * @param varID
	 * @param transID
	 * @param type
	 * @return
	 */
	public boolean checkTableHasLock(int varID, String transID, int type) {
		for (LockTable lockt : this.lockTable) {
			if (lockt.getVarID() == varID && lockt.getTransID().equals(transID)
					&& lockt.getLockType() == type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if lock table contains a lock for particular transaction
	 * @param transID
	 * @return
	 */
	public boolean checkLockWithTransID(String transID) {
		for (LockTable lockt : this.lockTable) {
			if (lockt.getTransID().equals(transID)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if lock table contains a lock for particular variable
	 * @param varID
	 * @return
	 */
	public boolean checkLockWithVarID(int varID) {
		for (LockTable lockt : this.lockTable) {
			if (lockt.getVarID() == varID) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a Lock to Lock Table
	 * @param variable
	 * @param transID
	 * @param lockType
	 */
	public void addLock(int variable, String transID, int lockType) {
		LockTable temp = new LockTable(variable, transID, lockType);
		this.lockTable.add(temp);
	}
	
	/**
	 * Changes RL to WL
	 * @param varID
	 * @param transID
	 */
	public void changeRLtoWL(int varID, String transID) {
		for (LockTable lockt : this.lockTable) {
			if (lockt.getTransID().equals(transID) && lockt.getVarID() == varID) {
				if (lockt.getLockType() == LockTable.RL) {
					lockt.setLockType(LockTable.WL);
				}
			}
		}
	}

	/**
	 * Returns all locks on Variable
	 * @param varID
	 * @return
	 */
	public ArrayList<LockTable> getVarLocksAll(int varID) {
		ArrayList<LockTable> answer = new ArrayList<LockTable>();
		for (LockTable lockt : this.lockTable){
			if (lockt.getVarID() == varID) {
				answer.add(lockt);
			}
		}
		return answer;
	}

	/**
	 * Removes Lock on variable for given transaction
	 * @param varID
	 * @param transID
	 * @param lockType
	 */
	public void removeLock(int varID, String transID, int lockType) {
		int index = -1;
		for (int i = 0; i < this.lockTable.size(); i++) {
			if (this.lockTable.get(i).getVarID() == varID && this.lockTable.get(i).getTransID().equals(transID)
					&& this.lockTable.get(i).getLockType() == lockType) {
				index = i;
			}
		}
		if (index != -1) {
			this.lockTable.remove(index);
		} else {
		}
	}

	/**
	 * Removes Lock for given Transaction
	 * @param transID
	 */
	public void removeLockWithTransID(String transID) {
		int i = 0;
		while (i < this.lockTable.size()) {
			if (this.lockTable.get(i).getTransID().equals(transID)) {
				this.lockTable.remove(i);
			} else {
				i++;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder answer = new StringBuilder();
		for (int i = 0; i < this.lockTable.size(); i++) {
			answer.append(this.lockTable.get(i) + "\n");
		}

		return answer.toString();
	}

}
