package nyu.courant.mis.adb;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * Main class
 * 
 * @author Aditya Kapoor, Tanmmay Mahendru
 *
 */
public class Main {

	private static TransactionManager manager = new TransactionManager();

	public static void main(String[] args) {
		String input = new String();
		input = args[0];
		parseInput(input);
	}

	private static void parseInput(String input) {

		try {
			FileReader r = new FileReader(input);
			Scanner in = new Scanner(r);
			while (in.hasNext()) {
				manager.execute(in.nextLine());
			}
			System.out.println(manager.waitList.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
