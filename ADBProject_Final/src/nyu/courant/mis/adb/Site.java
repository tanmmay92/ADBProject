package nyu.courant.mis.adb;

import java.util.ArrayList;
import java.util.List;

/**
 * Gives us all information associated with a site
 * @author Aditya Kapoor, Tanmmay Mahendru
 */

public class Site {
	private int ID;
	private ArrayList<Variable> variables;
	private boolean siteDown;
	public LockTable lt;
	
	public final static int SITES = 10;

	public Site(int id) {
		this.ID = id;
		variables = new ArrayList<Variable>();
		AddVarToSite();
		lt = new LockTable();
		siteDown = false;
	}

	public int getID() {
		return this.ID;
	}

	public List<Variable> getVariables() {
		return this.variables;
	}

	public boolean isDown() {
		return this.siteDown;
	}

	/**
	 * Assigns variables at each site
	 */
	public void AddVarToSite() {
		for (int i = 1; i <= Variable.VARIABLES; i++) {
			if (i % 2 != 0) {
				if (this.ID == ((i + 1) % 10)) {
					Variable var = new Variable(i);
					this.variables.add(var);
				}
				if (i % 10 == 9 && this.ID % 10 == 0) {
					Variable var = new Variable(i);
					this.variables.add(var);
				}
			} else {
				Variable var = new Variable(i);
				this.variables.add(var);
			}
		}
	}

	/**
	 * To check if site contains variable
	 * 
	 * @param varID
	 * @return
	 */
	public boolean checkVariableAtSite(int varID) {
		ArrayList<Variable> list = new ArrayList<Variable>();
		list = (ArrayList<Variable>) this.getVariables();
		for (Variable var: list) {
			if (var.getID() == varID) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns variable object for variable id
	 * 
	 * @param varID
	 * @return
	 */
	public Variable getVariableObj(int varID) {
		ArrayList<Variable> list = new ArrayList<Variable>();
		list = (ArrayList<Variable>) this.getVariables();
		for (Variable var: list) {
			if (var.getID() == varID) {
				return var;
			}
		}
		return null;
	}

	/**
	 * Sets present value as final value
	 * 
	 * @param varID
	 */
	public void setPresentValToVal(int varID) {
		ArrayList<Variable> list = (ArrayList<Variable>) this.getVariables();
		for (Variable var: list) {
			if (var.getID() == varID) {
				var.setPresentVal(var.getValue());
			}
		}
	}

	/**
	 * Sets final value as present value
	 * 
	 * @param varID
	 */
	public void setValToPresentVal(int varID) {
		ArrayList<Variable> list = (ArrayList<Variable>) this.getVariables();
		for (Variable var: list) {
			if (var.getID() == varID) {
				var.setValue(var.getPresentVal());
			}
		}
	}

	/**
	 * Writes variable value to site
	 * 
	 * @param varID
	 * @param value
	 */

	public void writeToSite(int varID, int value) {
		ArrayList<Variable> tempVariable = (ArrayList<Variable>) this.getVariables();
		for (Variable var: tempVariable) {
			if (var.getID() == varID) {
				var.setPresentVal(value);
			}
		}
	}

	/**
	 * Fails a site
	 */
	public void siteFail() {
		System.out.println("Site" + this.getID() + " Down = true");
		this.siteDown = true;
		for (LockTable ltable: this.lt.lockTable) {
			getVariableObj(ltable.getVarID())
					.setPresentVal(getVariableObj(ltable.getVarID()).getValue());
		}
		System.out.println("Truncating lock table of site" + this.getID());
		System.out.println("Locktable Size = " + this.lt.lockTable.size());
		this.lt.lockTable.clear();
		System.out.println("Locktable Size after clearing =  " + this.lt.lockTable.size());
	}

	/**
	 * Recovers a site
	 */
	public void siteRecover() {
		if (this.isDown()) {
			System.out.println("Site " + this.getID() + " Down = false");
			this.siteDown = false;
			//System.out.println("Exclusive variables read available, Non-exclusive variables read unavailable");
			ArrayList<Variable> var = (ArrayList<Variable>) this.getVariables();
			for (Variable v:var) {
				if (v.isCopied()) {
					// variable !exclusive
					v.setAvailableForRead(false);
				} else {
					// variable exclusive
					v.setAvailableForRead(true);
				}
			}
		} else {
			System.out.println("Site " + this.getID() + " is UP, recover call is invalid");
		}

	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < variables.size(); i++) {
			sb.append("x").append(variables.get(i).getID()).append(" = ").append(variables.get(i).getValue())
					.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Enhanced toString
	 * 
	 * @return
	 */
	public String toStringPlusExtraInfo() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < variables.size(); i++) {
			sb.append("x").append(variables.get(i).getID()).append(" = ").append(variables.get(i).getValue())
					.append(variables.get(i).getAvailableForRead()).append(variables.get(i).getPresentVal())
					.append("\n");
		}
		return sb.toString();
	}

}
