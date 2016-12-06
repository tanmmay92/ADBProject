package nyu.courant.mis.adb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Aditya Kapoor, Tanmmay Mahendru
 */

public class TransactionManager {
	private HashMap<Integer, Site> sites; // Map of SiteId and Site Object for
											// that particular SiteId
	private HashMap<String, Transaction> presentTrans; // map of TransId and
														// it's Object
	private int time = 0; // for timestamp
	public HashMap<String, ArrayList<Operation>> waitList; // mapping of TransId
															// and List of
															// operations
															// associated with
															// it
	private Graph<Integer> graph = new Graph<Integer>(true);
	public Map<String, String> _lockTransMap = new HashMap<String, String>();

	// Constructor() . Initialize all the siteId and all the variables
	public TransactionManager() {
		sites = new HashMap<Integer, Site>();
		for (int i = 1; i <= Site.SITES; i++) {
			Site site = new Site(i);
			sites.put(i, site);
		}
		presentTrans = new HashMap<String, Transaction>();
		waitList = new HashMap<String, ArrayList<Operation>>();
	}

	// Executed the line read in main() method
	public void execute(String line) {
		time++;
		String[] operation = line.split("; ");
		ArrayList<String> endTransactionList = new ArrayList<String>();
		for (String op : operation) {
			System.out.println(op);
			if (op.startsWith("dump()")) {
				dump();
			} else if (op.startsWith("dump(x")) {
				int index = Integer.parseInt(op.substring(6, op.length() - 1));
				dumpx(index);
			} else if (op.startsWith("dump(")) {
				int index = Integer.parseInt(op.substring(5, op.length() - 1));
				dumpIndex(index);
			} else if (op.startsWith("begin(")) {
				startTransaction(op.substring(6, op.length() - 1), Transaction.RW);
			} else if (op.startsWith("beginRO(")) {
				startTransaction(op.substring(8, op.length() - 1), Transaction.RO);
			} else if (op.startsWith("R(")) {
				String[] t = op.substring(2, op.length() - 1).split(",");
				readValue(t[0], Integer.parseInt(t[1].substring(t[1].indexOf("x") + 1)));
			} else if (op.startsWith("W(")) {
				String[] t = op.substring(2, op.length() - 1).split(",");
				writeToVar(t[0], Integer.parseInt(t[1].substring(t[1].indexOf("x") + 1)), Integer.parseInt(t[2]));
			} else if (op.startsWith("end(")) {
				// endTransaction(op.substring(4, op.length() - 1));
				endTransactionList.add(op.substring(4, op.length() - 1));
			} else if (op.startsWith("fail(")) {
				failSite(op.substring(5, op.length() - 1));
			} else if (op.startsWith("recover(")) {
				recoverSite(op.substring(8, op.length() - 1));
			}
		}
		for (String TransList : endTransactionList) {
			endTrans(TransList);
		}
	}

	/**
	 * Aborts transaction
	 * @param transID
	 */
	private void abort(String transID) {
		if (presentTrans.containsKey(transID)) {
			Transaction t = (Transaction) presentTrans.get(transID);
			ArrayList<Operation> ops = (ArrayList<Operation>) t.getOperations();
			for (Operation op : ops) {
				if (op.getOperationType() == Operation.WRITE) {

					int varID = op.getVarIndex();
					for (int j = 1; j <= Site.SITES; j++) {
						if (!this.sites.get(j).isDown()) {
							ArrayList<Variable> tempVar = (ArrayList<Variable>) this.sites.get(j).getVariables();
							for (Variable var : tempVar) {
								if (var.getID() == varID) {
									this.sites.get(j).setPresentValToVal(varID);
								}
							}
						} else {

						}
					}
				} else {

				}
			}

			for (int i = 1; i <= Site.SITES && !this.sites.get(i).isDown(); i++) {
				this.sites.get(i).lt.removeLockWithTransID(transID);
			}
			System.out.println("Transaction " + t.getTransID() + " is aborted");
			this.updateWaitingTrans(t.getTransID());

		} else {
			System.out.println(transID + " IS INVALID");
		}

	}

	// For begin operations, this method gets object for that transId and
	// initializes in a new Transaction
	private void startTransaction(String transID, int transType) {
		if (!presentTrans.containsKey(transID)) {
			Transaction transaction = new Transaction(transID, time, transType);
			presentTrans.put(transID, transaction);
		}
	}

	/**
	 * Returns number of UP Sites containing Variable
	 */
	private int siteCountWithVar(int varID) {
		int answer = 0;
		for (int i = 1; i <= Site.SITES; i++) {
			if (!this.sites.get(i).isDown()) {
				// Site is UP
				ArrayList<Variable> vars = (ArrayList<Variable>) this.sites.get(i).getVariables();
				for (Variable var : vars) {
					if (var.getID() == varID) {
						answer++;
					}
				}
			}
		}
		return answer;
	}

	/**
	 * Checks if Transaction has RL on Var
	 * 
	 * @param trans
	 * @param varID
	 * @return
	 */
	private boolean checkIfTransHasRLonVar(Transaction trans, int varID) {
		ArrayList<LockTable> lockExists = trans.getLockedList();
		for (LockTable ltable : lockExists) {
			if (ltable.getLockType() == LockTable.RL && ltable.getVarID() == varID) {
				return true;
			}
		}
		return false;
	}

	/**
	 * prints values of all variables on all sites
	 */
	public void dump() {
		for (int i = 1; i <= Site.SITES; i++) {
			System.out.print("Site " + i + "\n");
			System.out.print(sites.get(i).toString());
		}
		System.out.print("\n");
	}

	/**
	 * prints the values at site with index i
	 * 
	 * @param index
	 */
	public void dumpIndex(int index) {
		if (sites.containsKey(index)) {
			System.out.print("Site " + index + "\n");
			System.out.print(sites.get(index).toString());
		}
		System.out.print("\n");
	}

	/**
	 * prints value of variable i across all sites
	 * 
	 * @param index
	 */
	public void dumpx(int index) {
		for (int i = 1; i <= Site.SITES; i++) {
			ArrayList<Variable> var = (ArrayList<Variable>) sites.get(i).getVariables();
			for (Variable v : var) {
				if (v.getID() == index) {
					System.out.print("Site " + i + "\n");
					System.out.print(v.toString());
				}
			}
		}
		System.out.print("\n");
	}

	/**
	 * Ends the given transaction
	 * 
	 * @param transID
	 */
	private void endTrans(String transID) {
		if (this.presentTrans.containsKey(transID)) {
			Transaction tToEnd = this.presentTrans.get(transID);
			if (this.presentTrans.get(transID).getTransType() == Transaction.RO) {
				System.out.println("End RO-transaction " + transID);
			} else if (this.presentTrans.get(transID).getTransType() == Transaction.RW) {
				boolean ifAccessedSitesActive = true;
				for (int i = 1; i <= Site.SITES; i++) {
					if (tToEnd.sitesVisited.contains(i)
							&& (this.sites.get(i).isDown() || !this.sites.get(i).lt.checkLockWithTransID(transID))) {
						ifAccessedSitesActive = false;
					}
				}
				if (ifAccessedSitesActive) {
					System.out.println("End RW-Transaction " + transID);
					Transaction tempTransaction = (Transaction) presentTrans.get(transID);
					ArrayList<LockTable> lockExistsByTempTrans = tempTransaction.getLockedList();

					for (LockTable ltable : lockExistsByTempTrans) {
						if (ltable.getLockType() == LockTable.WL) {
							
							int varID = ltable.getVarID();
							for (int i = 1; i <= Site.SITES; i++) {
								if (!this.sites.get(i).isDown() && this.sites.get(i).checkVariableAtSite(varID)
										&& this.sites.get(i).lt.checkLockWithTransID(transID)) {
									// S(i) UP
									this.sites.get(i).setValToPresentVal(varID);
									Variable temp = this.sites.get(i).getVariableObj(varID);
									if (temp.getAvailableForRead() == false && temp != null) {
										temp.setAvailableForRead(true);
									}
									ArrayList<Variable> allVars = (ArrayList<Variable>) this.sites.get(i)
											.getVariables();
									for (Variable var : allVars) {
										if (var.getID() == varID) {
											var.getVariableHistory().add(new Variable(time, var.getValue()));
											this.sites.get(i).lt.removeLock(varID, transID, LockTable.WL);
										}
									}

								}
							}
						} else {
							// RL
							int varID = ltable.getVarID();
							for (int i = 1; i <= Site.SITES; i++) {
								Site s = this.sites.get(i);
								s.lt.removeLock(varID, transID, LockTable.RL);
								;
							}
						}
					}
					this.updateWaitingTrans(transID);
				} else {
					// All sites not 				
					this.abort(transID);
					this.updateWaitingTrans(transID);
				}
			}
			presentTrans.remove(transID);
			waitList.remove(transID);
		}
	}

	/**
	 * Fails given site
	 * 
	 * @param str
	 */
	private void failSite(String str) {
		int siteID = Integer.parseInt(str);
		if (this.sites.containsKey(siteID)) {
			Site s = (Site) sites.get(siteID);
			s.siteFail();
		} else {
			System.out.println(str + "Site does not exist");
		}
	}

	/**
	 * returns locks on Variable across all Sites
	 * all sites
	 * 
	 * @param varID
	 * @return
	 */
	private ArrayList<LockTable> retrieveLocksOnVarFromAllSites(int varID) {
		ArrayList<LockTable> ans = new ArrayList<>();
		for (int i = 1; i <= Site.SITES; i++) {
			ArrayList<LockTable> lockForThisSite = this.sites.get(i).lt.getVarLocksAll(varID);
			for (LockTable ltable : lockForThisSite) {
				ans.add(ltable);
			}
		}
		return ans;
	}
	
	private int retrieveWriteLocksOnVarFromAllSites(String transID, int varID) {
		ArrayList<LockTable> ans = new ArrayList<>();
		int count = 0;
		for (int i = 1; i <= Site.SITES; i++) {
			ArrayList<LockTable> lockForThisSite = this.sites.get(i).lt.getVarLocksAll(varID);
			for (LockTable ltable : lockForThisSite) {
				if (ltable.getLockType() == LockTable.WL && ltable.getTransID() == transID) {
					ans.add(ltable);
					count++;	
				}
			}
		}
		//System.out.println("Arraylist size"+ans.size()+count);
		return count;
	}

	/**
	 * Returns all WL of a Transaction on a variable
	 * 
	 * @param transID
	 * @param varID
	 */
	private void retrieveAllWLonVar(String transID, int varID) {
		Transaction transaction = this.presentTrans.get(transID);
		for (int i = 1; i <= Site.SITES; i++) {
			if (!this.sites.get(i).isDown()) {
				Site s = this.sites.get(i);
				ArrayList<Variable> v = (ArrayList<Variable>) s.getVariables();
				boolean answer = false;
				for (Variable var : v) {
					if (var.getID() == varID) {
						answer = true;
					}
				}
				if (answer) {
					if (this.sites.get(i).lt.checkLockWithVarID(varID)) {
						// table contains lock with given variable ID
						// Transaction transID cannot get write lock on the
						// Variable varID at this site
					} else {// table does not contain any lock with given varID
						// we can get write lock on this site
						this.sites.get(i).lt.addLock(varID, transID, LockTable.WL);
						transaction.addLockToExistingLock(varID, LockTable.WL);
						transaction.sitesVisited.add(i);
					}
				}
			}
		}
	}

	/**
	 * gets WL on all UP sites having variable
	 * 
	 * @param varID
	 * @param transID
	 */
	private void getWLonVar(int varID, String transID) {
		for (int i = 1; i <= Site.SITES; i++) {
			if (!this.sites.get(i).isDown()) {
				if (this.sites.get(i).checkVariableAtSite(varID)) {
					// site contains variable on which to get lock
					Transaction t = this.presentTrans.get(transID);
					t.sitesVisited.add(i);
					this.sites.get(i).lt.addLock(varID, transID, LockTable.WL);
				}
			}
		}
	}

	/**
	 * Adds Transaction to wait list
	 * @param op
	 * @param transID
	 */
	private void addToWaitList(Operation op, String transID) {
		ArrayList<Operation> ops;
		if (waitList.containsKey(transID)) {
			ops = (ArrayList<Operation>) waitList.get(transID);
			ops.add(op);
		} else {
			ops = new ArrayList<Operation>();
			ops.add(op);
			waitList.put(transID, ops);
		}
	}

	/**
	 * Create edges using this function.
	 */

	private void createGraphEdge(String lockedBy, String transID) {
		graph.addEdge(Long.parseLong(transID.substring(1)), Long.parseLong(lockedBy.substring(1)));
		_lockTransMap.put(transID, lockedBy);
	}

	/**
	 * Implementation for Cycle Detection.
	 */

	public boolean checkForCycleDetection() {
		String youngerTrans = new String();
		int youngestTime = 0;
		boolean hasCycle = graph.hasCycle(graph);
		if (hasCycle) {
			System.out.println("Cycle Detected");
			Iterator<Entry<String, String>> it = _lockTransMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
				Transaction T = presentTrans.get(pair.getKey());
				if (T.getStartTime() > youngestTime) {
					youngestTime = T.getStartTime();
					youngerTrans = T.getTransID();
				}
			}
			System.out.println("Aborting transaction " + youngerTrans);
			abort(youngerTrans);
			_lockTransMap.remove(youngerTrans);
			return true;
		} else
			return false;
	}

	/**
	 * Checks for older transaction with lock
	 */
	private boolean checkOlderTransWithLock(int tStartTime, int varID) {
		for (int i = 1; i <= Site.SITES; i++) {
			Site tempSite = this.sites.get(i);
			ArrayList<LockTable> lockForVariable = tempSite.lt.getVarLocksAll(varID);
			for (LockTable ltable : lockForVariable) {
				String currentTID = ltable.getTransID();
				int currentStartTime = this.presentTrans.get(currentTID).getStartTime();
				if (currentStartTime < tStartTime) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * 
	 * @param transID1
	 * @param transID2
	 * @return
	 */
	private boolean makeTrans1WaitForTrans2(String transID1, String transID2) {
		if (transID1.equals(transID2)) {// both transID's cannot be same
			return true;
		} else {
			Transaction transaction2 = this.presentTrans.get(transID2);
			Transaction transaction1 = this.presentTrans.get(transID1);
			if (transaction2.waitsForPresentTrans == null) {
				// no transaction waits for transaction2
				// directly make transaction1 wait for transaction2
				transaction2.waitsForPresentTrans = transID1;
				transaction1.presentTransWaitsFor.add(transID2);
			} else if (transaction2.waitsForPresentTrans.equals(transaction1.getTransID())) {
				// transaction1 is already waiting on transaction2, don't do anything
			} else {
				if (transaction1.getStartTime() < transaction2.getStartTime()) {
					// t1 is older than t2 some transaction waits for transaction2
					Transaction tWaitingForT2 = this.presentTrans.get(transaction2.waitsForPresentTrans);
					if (transaction1.getStartTime() <= tWaitingForT2.getStartTime()) {
						// transaction 1 is older than tWaitingForT2
						// so transaction 1 should wait for tWaitingForT2
						makeTrans1WaitForTrans2(transaction1.getTransID(), tWaitingForT2.getTransID());
					} else {
						// transaction1 is younger than tWaitingForT2 transaction1 should abort
						this.abort(transaction1.getTransID());
						return false;
					}
				} else {
					// t1 is younger than t2
					// t1 should abort
					this.abort(transID1);
					return false;
				}
			}
			
		}
		return true;
	}

	/**
	 * a transaction operation from waitlist is executed which was waiting for another 
	 * transaction to end
	 * @param transID
	 */
	private void updateWaitingTrans(String transID) {
		Transaction tr = this.presentTrans.get(transID);
		if (tr.waitsForPresentTrans != null) {

			// waitingTransaction waits for transID
			Transaction waitingTransaction = this.presentTrans.get(tr.waitsForPresentTrans);
			if (this.waitList.size() > 0 && this.waitList.containsKey(waitingTransaction.getTransID())) {
				waitingTransaction.presentTransWaitsFor.remove(transID);
				int variableThatWaitingTransactionNeeds;
				ArrayList<Operation> opList = this.waitList.get(waitingTransaction.getTransID());
				Operation op = opList.get(0);
				variableThatWaitingTransactionNeeds = op.getVarIndex();
				if (op.getOperationType() == 1) {
					this.retrieveAllWLonVar(waitingTransaction.getTransID(), variableThatWaitingTransactionNeeds);
				}

				if (waitingTransaction.presentTransWaitsFor.size() == 0) {
					this.waitList.remove(waitingTransaction.getTransID());
					if (op.getOperationType() == 0) {
						// read operation
						this.readValue(waitingTransaction.getTransID(), op.getVarIndex());
					} else {// write operation
						this.writeToVar(waitingTransaction.getTransID(), op.getVarIndex(), op.getValue());
					}
				}
			}

		} else {
			// no action taken as no transaction waiting for present transaction
		}
	}

	/**
	 * Updates the wait for list of Transaction transID.
	 * 
	 * @param transID
	 * @param varID
	 */
	private void updateWaitList(String transID, int varID) {
		Transaction transaction = this.presentTrans.get(transID);
		boolean val = true;
		for (int i = 1; i <= Site.SITES && val; i++) {
			if (!this.sites.get(i).isDown()) {
				Site site = this.sites.get(i);
				if (site.lt.checkLockWithVarID(varID)) {
					for (int j = 0; j < site.lt.lockTable.size() && val; j++) {
						if (site.lt.lockTable.get(j).getVarID() == varID) {
							String str = site.lt.lockTable.get(j).getTransID();
							Transaction otr = this.presentTrans.get(str);
							
							if (otr.waitsForPresentTrans == null) {
								// no transaction waits for the transaction that
								// has the lock
								otr.waitsForPresentTrans = transID;
								transaction.presentTransWaitsFor.add(str);
							} else {
								// some transaction already waits for the
								// transaction that has the lock
								val = makeTrans1WaitForTrans2(transID, otr.getTransID());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Detailing all conditions for read-only and read-write operations to take place
	 * 
	 * @param transID
	 * @param variable
	 * @param transType
	 */
	private void read(String transID, int variable, int transType) {
		Transaction transactionForReading = this.presentTrans.get(transID);
		if (transType == Transaction.RO) {// for read only transaction
			boolean hasItBeenRead = false;
			for (int i = 1; i <= Site.SITES && !hasItBeenRead; i++) {
				Site tempSite = sites.get(i);
				if (tempSite.isDown()) {
					// site is down
					// go to next site
				} else {
					// site is up
					ArrayList<Variable> variablesInTempSite = (ArrayList<Variable>) tempSite.getVariables();
					for (Variable var : variablesInTempSite) {
						if (var.getID() == variable && var.getAvailableForRead()) {
							
							ArrayList<Variable> history = (ArrayList<Variable>) var.getVariableHistory();
							int currentMax = -1;// time counter
							int maxIndex = -1;
							for (int j = 0; j < history.size(); j++) {
								if (history.get(j).getTime() > currentMax
										&& history.get(j).getTime() < transactionForReading.getStartTime()) {
									maxIndex = j;
									currentMax = history.get(j).getTime();
								}
							}
							if (maxIndex != -1) {
								hasItBeenRead = true;
								System.out.println("The value that is read is " + history.get(maxIndex).getValue());
								Operation op = new Operation(0, variable, 0, Operation.READ);
								transactionForReading.addOperation(op);
							}
						}
					}
				}
			}
			if (hasItBeenRead == false) {
				// operation waits as variable could not readable from any site
				System.out.println(transID + "(R,x" + variable + ") has to wait");
				Operation op = new Operation(0, variable, 0, Operation.READ);
				addToWaitList(op, transID);
			}
			// for read-write transaction
		} else {

			if (this.checkIfTransHasRLonVar(transactionForReading, variable)) {
				// lock already exists by transaction on site
				int index = -1;
				for (int q = 1; q <= Site.SITES; q++) {
					if (!this.sites.get(q).isDown()) {
						if (this.sites.get(q).lt.checkTableHasLock(variable, transID, LockTable.RL)) {
							index = q;
						}
					}
				}
				ArrayList<Variable> varsInSite = (ArrayList<Variable>) this.sites.get(index).getVariables();
				Variable var = varsInSite.get(0);
				for (Variable v : varsInSite) {
					if (v.getID() == variable) {
						var = v;
					}
				}
				System.out.println("The value read is " + var.getValue());
			} else {
				// Read lock doesn't exists by transaction on site
				boolean hasItBeenRead = false;
				for (int i = 1; i <= Site.SITES && !hasItBeenRead; i++) {
					Site tempSite = sites.get(i);
					if (tempSite.isDown()) {
					} else {
						ArrayList<Variable> variablesInTempSite = (ArrayList<Variable>) tempSite.getVariables();
						for (int j = 0; j < variablesInTempSite.size(); j++) {
							if (variablesInTempSite.get(j).getID() == variable
									&& variablesInTempSite.get(j).getAvailableForRead()) {
								
								if (tempSite.lt.checkRLavailability(variable, transID)) {
									// we can get read lock on the variable
									if (tempSite.lt.checkTableHasLock(variable, transID, LockTable.WL)) {
										
										hasItBeenRead = true;
										Variable currentVariable = tempSite.getVariableObj(variable);
										Operation op = new Operation(0, variable, 0, Operation.READ);
										transactionForReading.addOperation(op);
										transactionForReading.sitesVisited.add(i);
										System.out.println(
												"The value that is read is " + currentVariable.getPresentVal());
										
									} else {
										// No WL
										hasItBeenRead = true;
										tempSite.lt.addLock(variable, transID, LockTable.RL);
										Variable currentVariable = tempSite.getVariableObj(variable);
										Operation op = new Operation(0, variable, 0, Operation.READ);
										transactionForReading.addOperation(op);
										transactionForReading.sitesVisited.add(i);
										transactionForReading.addLockToExistingLock(variable, LockTable.RL);
										System.out.println("The value that is read is " + currentVariable.getValue());
									}
								} else {
									
									hasItBeenRead = true;
									ArrayList<LockTable> tempList = tempSite.lt.getVarLocksAll(variable);
									LockTable writeLock = tempList.get(0);
									String tidWithWriteLock = writeLock.getTransID();
									String tidWithoutWriteLock = transID;

									if (presentTrans.containsKey(tidWithWriteLock)
											&& presentTrans.containsKey(tidWithoutWriteLock)) {
										createGraphEdge(tidWithWriteLock, tidWithoutWriteLock);
									}

									boolean answer = checkForCycleDetection();

									if (answer == false) {
										// This operation has to wait
										this.updateWaitList(transID, variable);
										Operation op = new Operation(0, variable, 0, Operation.READ);
										addToWaitList(op, transID);
										System.out.println(transID + "(R,x" + variable + ") has to wait");
									}
								}
							}
						}
					}
				}
				if (hasItBeenRead == false) {				
					Operation op = new Operation(0, variable, 0, Operation.READ);
					addToWaitList(op, transID);
					System.out.println(transID + "(R,x" + variable + ") has to wait");
				}
			}
		}
	}

	/**
	 * said transaction reads value
	 * 
	 * @param transID
	 * @param variable
	 */
	private void readValue(String transID, int variable) {
		if (presentTrans.containsKey(transID)) {
			Transaction t = (Transaction) presentTrans.get(transID);
			if (t.getTransType() == Transaction.RO) {
				read(transID, variable, Transaction.RO);
			} else {
				read(transID, variable, -1);
			}
		}
	}

	/**
	 * operation site recovery
	 * 
	 * @param siteID
	 */
	private void recoverSite(String siteID) {
		int site = Integer.parseInt(siteID);
		if (this.sites.containsKey(site)) {
			this.sites.get(site).siteRecover();
		} else {
			System.out.println("INVALID SITE ID:" + siteID);
		}
	}

	/**
	 * generates count of locks on Variable varID across all sites
	 * 
	 * @param varID
	 * @return
	 */
	public int getNumLocksAllSites(int varID) {
		int answer = 0;
		for (int i = 1; i <= Site.SITES; i++) {
			answer += sites.get(i).lt.getVarLocksAll(varID).size();
		}
		return answer;
	}

	/**
	 * generates count of sites that are UP with Variable varID
	 * 
	 * @param varID
	 * @return
	 */
	private int upSiteCountContainingVariable(int varID) {
		int answer = 0;
		for (int i = 1; i <= Site.SITES; i++) {
			if (!this.sites.get(i).isDown()) {
				// site is not down
				ArrayList<Variable> vList = (ArrayList<Variable>) this.sites.get(i).getVariables();
				for (Variable var : vList) {
					if (var.getID() == varID) {
						answer++;
					}
				}
			} else {
				// site down
			}
		}
		return answer;
	}

	/**
	 * use available copies logic to write operation on sites
	 * 
	 * @param varID
	 * @param val
	 * @param transID
	 */
	private void writeToSites(int varID, int val, String transID) {
		for (int i = 1; i <= Site.SITES; i++) {
			if (this.sites.get(i).checkVariableAtSite(varID) && !this.sites.get(i).isDown()
					&& this.sites.get(i).lt.checkLockWithTransID(transID)) {
				this.sites.get(i).writeToSite(varID, val);
			}
		}
	}

	/**
	 * @param transID
	 * @param varID
	 * @param value
	 * 
	 *           transaction writes the value implemented
	 */
	public void writeToVar(String transID, int varID, int value) {
		Transaction transaction;
		if (presentTrans.containsKey(transID)) {
			transaction = (Transaction) presentTrans.get(transID);
			if (transaction.getTransType() == Transaction.RW) {
				if (transaction.checkTransHasWL(varID)) {
					// check for atleast one write lock on varID
					int numberOfLocksWithTransactionForVariable = transaction.NumOfLocksOnTrans(LockTable.WL, varID);
					int upSiteContainingVariable = this.siteCountWithVar(varID);
					if (upSiteContainingVariable == numberOfLocksWithTransactionForVariable) {
						this.writeToSites(varID, value, transID);
						Operation op = new Operation(value, varID, this.time, Operation.WRITE);
						transaction.addOperation(op);
					} else {
						this.retrieveAllWLonVar(transID, varID);
						this.updateWaitList(transID, varID);
					}
				} else {
					// no write locks present on the variable
					int count = this.upSiteCountContainingVariable(varID);
					if (count > 0) {
						// the active sites that contain the variable to be written to			
/*						int sitesUp = 0;
						String tempTrans = "";
						for (int i = 1; i <= Site.SITES && !this.sites.get(i).isDown(); i++) {
							Site tempSite = this.sites.get(i);
							sitesUp++;
							ArrayList<LockTable> lock = tempSite.lt.getVarLocksAll(varID);
							if (lock.size() != 0) {
								for (LockTable l : lock) {
									if (l.getTransID() != transID && l.getTransID()!= "") {
										if (l.getLockType() == LockTable.WL) {
											// another transaction has lock acquired 
											if (tempTrans != l.getTransID()) {
												tempTrans = l.getTransID();
											}
										}										
									}
								}
							}							
						}
						
						if (tempTrans != "") {
							int countlock = this.retrieveWriteLocksOnVarFromAllSites(tempTrans, varID);
							if (sitesUp == countlock) {
								createGraphEdge(tempTrans, transID);
								boolean check = checkForCycleDetection();
								if (check == false) {
									this.updateWaitList(transID, varID);
									Operation op = new Operation(value, varID, time, Operation.WRITE);
									addToWaitList(op, transID);
									System.out.println(
											"W(" + transID + ",x" + varID + "," + value + ")" + " has to wait");
								}
							}
						}*/
						
						for (int i = 1; i <= Site.SITES && !this.sites.get(i).isDown(); i++) {
							Site tempSite = this.sites.get(i);
							ArrayList<LockTable> lock = tempSite.lt.getVarLocksAll(varID);
							lab1: for (LockTable l : lock) {
								if (l.getTransID() != transID) {
									this.updateWaitList(transID, varID);
									Operation op = new Operation(value, varID, time, Operation.WRITE);
									addToWaitList(op, transID);
									System.out.println(
											"W(" + transID + ",x" + varID + "," + value + ")" + " has to wait");
									createGraphEdge(l.getTransID(), transID);
									boolean check = checkForCycleDetection();									
									if (check == false) {
										break lab1;
										}
								}
							}
						}

						boolean areThereOlderTransactions = this.checkOlderTransWithLock(transaction.getStartTime(),
								varID);
						if (areThereOlderTransactions) {
							

						} else {
							
							int numberOfLocks = this.getNumLocksAllSites(varID);
							if (numberOfLocks == 0) {
								
								this.getWLonVar(varID, transID);
								transaction.addLockToExistingLock(varID, LockTable.WL);
								this.writeToSites(varID, value, transID);
								Operation op = new Operation(value, varID, this.time, Operation.WRITE);
								transaction.addOperation(op);
							} else if (numberOfLocks == 1) {
								
								ArrayList<LockTable> allLocks = this.retrieveLocksOnVarFromAllSites(varID);
								if (allLocks.get(0).getTransID().equals(transID)
										&& allLocks.get(0).getLockType() == LockTable.RL) {
									// same transaction has lock 
									for (int p = 1; p <= Site.SITES; p++) {
										this.sites.get(p).lt.removeLock(varID, transID, LockTable.RL);
									}
									transaction.removeLockFromExistingLock(varID);
									this.getWLonVar(varID, transID);
									transaction.addLockToExistingLock(varID, LockTable.WL);
									this.writeToSites(varID, value, transID);
									Operation op = new Operation(value, varID, this.time, Operation.WRITE);
									transaction.addOperation(op);
								} else {
									// different transaction has lock
									this.updateWaitList(transID, varID);
									Operation op = new Operation(value, varID, time, Operation.WRITE);
									addToWaitList(op, transID);
									this.retrieveAllWLonVar(transID, varID);
									System.out.println(
											"W(" + transID + ",x" + varID + "," + value + ")" + " has to wait");
								}
							} else {
								// Locks on the variable on other sites
								this.updateWaitList(transID, varID);
								Operation op = new Operation(value, varID, time, Operation.WRITE);
								addToWaitList(op, transID);
								this.retrieveAllWLonVar(transID, varID);
								System.out.println("W(" + transID + ",x" + varID + "," + value + ")" + " has to wait");
							}
						}
					} else {
						// all sites containing the given variable are down
						this.updateWaitList(transID, varID);
						System.out.println("W(" + transID + ",x" + varID + "," + value + ")" + " has to wait");
						Operation op = new Operation(0, varID, 0, Operation.READ);
						addToWaitList(op, transID);
						this.retrieveAllWLonVar(transID, varID);
					}
				}
			}
		}
}}