import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/* SpellCheck.java
 * This program accepts as its sole argument a text file containing
 * common misspellings and their corrections. The program attempts
 * to correct each misspelled word, and prints the percent of accurate
 * corrections. The -verbose flag can be passed to print the
 * correction generated for each line.
 *
 * Before performing the correction process, the program prompts the
 * user for their flowers database login information. This is because
n * the program uses our group's 5-gram dataset to train the spell checker.
 */

public class SpellCheck {
    public static Connection DB;
    public static boolean verbose = false;
    public static File inputFile;
    public static HashMap<String, Integer> frequencies = new HashMap<String, Integer>();

    public static void main(String[] args) throws ClassNotFoundException {
	// Check command line args
	if (!parseFlags(args)) {
	    return;
	}

	System.out.println("verbose: " + verbose);

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

	// Train spellchecker from data, populating frequencies HashMap
	trainFromDatabase();

	// Process the given file
	processFile(inputFile);
    }

    public static void trainFromDatabase() {
	try {
	    String query1 = "SELECT sum(freq) as f, lower(word1) as w FROM ngrams GROUP BY word1";
	    String word;
	    int freq;
	    Statement s = DB.createStatement();
	    ResultSet results = s.executeQuery(query1);
	    // Loop through each tuple in database
	    while (results.next()) {
		word = results.getString("w");
		freq = results.getInt("f");
		if (!frequencies.containsKey(word))
		    frequencies.put(word, freq);
		else
		    frequencies.put(word, frequencies.get(word) + freq);
	    }

	    String query2 = "SELECT sum(freq) as f, lower(word2) as w FROM ngrams GROUP BY word2";
	    results = s.executeQuery(query2);
	    while (results.next()) {
		word = results.getString("w");
		freq = results.getInt("f");
		if (!frequencies.containsKey(word))
		    frequencies.put(word, freq);
		else
		    frequencies.put(word, frequencies.get(word) + freq);
	    }

	    String query3 = "SELECT sum(freq) as f, lower(word3) as w FROM ngrams GROUP BY word3";
	    results = s.executeQuery(query3);
	    while (results.next()) {
		word = results.getString("w");
		freq = results.getInt("f");
		if (!frequencies.containsKey(word))
		    frequencies.put(word, freq);
		else
		    frequencies.put(word, frequencies.get(word) + freq);
	    }

	    String query4 = "SELECT sum(freq) as f, lower(word4) as w FROM ngrams GROUP BY word4";
	    results = s.executeQuery(query4);
	    while (results.next()) {
		word = results.getString("w");
		freq = results.getInt("f");
		if (!frequencies.containsKey(word))
		    frequencies.put(word, freq);
		else
		    frequencies.put(word, frequencies.get(word) + freq);
	    }

	    String query5 = "SELECT sum(freq) as f, lower(word5) as w FROM ngrams GROUP BY word5";
	    results = s.executeQuery(query5);
	    while (results.next()) {
		word = results.getString("w");
		freq = results.getInt("f");
		if (!frequencies.containsKey(word))
		    frequencies.put(word, freq);
		else
		    frequencies.put(word, frequencies.get(word) + freq);
	    }

	    // TODO: debug
	    //System.out.println(frequencies.get("zoo"));
	} catch (SQLException e) {
	    System.err.println("database error: " + e);
	}
    }

    public static void processFile(File inputFile) {
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
