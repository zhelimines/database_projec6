import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;

/* SpellCheck.java
 * This program accepts as its sole argument a text file containing
 * common misspellings and their corrections. The program attempts
 * to correct each misspelled word, and prints the percent of accurate
 * corrections. The -verbose flag can be passed to print the
 * correction generated for each line.
 *
 * Before performing the correction process, the program prompts the
 * user for their flowers database login information. This is because
 * the program uses our group's 5-gram dataset to train the spell checker.
 */

public class SpellCheck {
    public static Connection DB;
    public static boolean verbose = false;
    public static File inputFile;

    public static void main(String[] args) throws ClassNotFoundException {
	// Check command line args
	if (!parseFlags(args)) {
	    return;
	}

	// Get database login info
	Class.forName("org.postgresql.Driver");
	String connectString = "jdbc:postgresql://flowers.mines.edu/csci403";
	System.out.print("Username: ");
	String username = System.console().readLine();
	System.out.print("Password: ");
	String password = new String(System.console().readPassword());

	// Connect to database
	try {
	    DB = DriverManager.getConnection(connectString, username, password);
	} catch (SQLException e) {
	    System.err.println("Error connecting to database: " + e);
	    return;
	}

	// Train spellchecker from data

	// Process the given file
    }

    public static boolean parseFlags(String[] args) {
	int i = 0;
	String arg = "";

	while (i < args.length && args[i].startsWith("-")) {
	    arg = args[i++];
	}

	if (arg.equals("-verbose") || arg.equals("-v")) {
	    verbose = true;
	}
	else if (!arg.equals("")) {
	    System.err.println("usage: SpellCheck [-verbose] filename");
	    return false;
	}

	try {
	    inputFile = new File(args[i++]);
	} catch (Exception e) {
	    System.err.println("error opening file: " + e);
	    return false;
	}

	return true;
    }
}
