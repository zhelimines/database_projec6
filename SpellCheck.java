import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;

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
    public static boolean interactive = false;
    public static File inputFile;
    public static HashMap<String, Integer> frequencies = new HashMap<String, Integer>();

    public static final int MAX_EDIT_DISTANCE = 1;

    public static void main(String[] args) throws ClassNotFoundException {
        // Check command line args
        if (!parseFlags(args)) {
            return;
        }

        System.out.println("verbose: " + verbose);
        System.out.println("interactive: " + interactive);

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

        if (interactive) {
            // Get word from user and attempt correction
            handleInteractive();
        }
        else {
            // Process the given file
            processFile(inputFile);
        }
    }

    /**
     * Reads every word from the ngrams table in the database,
     * and populates the frequencies HashMap with the words and
     * their frequencies.
     */
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
        } catch (SQLException e) {
            System.err.println("database error: " + e);
        }
    }

    /**
     * Reads every line of the given input file and attempts to
     * correct every word in the first column. Prints the number
     * of words successfully corrected.
     */
    public static void processFile(File inputFile) {
        int numCorrect = 0;
        int numTotal = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            String line;
            String corrected;
            String[] columns = new String[4];
            while ((line = br.readLine()) != null) {
                numTotal++;
                columns = line.split("\t");
                corrected = getCorrection(getTargets(columns[0], MAX_EDIT_DISTANCE), columns[0]);
                if (corrected.equals(columns[1])) {
                    if (verbose)
                        System.out.println(String.format("%-15s\t%1s\t%s", columns[0], "O", corrected));
                    numCorrect++;
                }
                else if (verbose)
                    System.out.println(String.format("%-15s\t%1s\t%-15s\t%s", columns[0], "X", corrected, columns[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(numCorrect + " correct out of " + numTotal);
    }

    /**
     * Returns the string which represents our best guess at a correction.
     */
    public static String getCorrection(HashSet<String> possibleCorrections, String original) {
        int max = -1;
        String bestCorrection = original;
        for (String s : possibleCorrections)
            if (frequencies.get(s) > max) {
                max = frequencies.get(s);
                bestCorrection = s;
            }

        return bestCorrection;
    }

    /**
     * Parses the command line options and sets verbose and interactive
     * to the appropriate values.
     */
    public static boolean parseFlags(String[] args) {
        int i = 0;
        ArrayList<String> argList = new ArrayList<String>();

        while (i < args.length && args[i].startsWith("-")) {
            argList.add(args[i++]);
        }

        for (String s : argList) {
            if (s.equals("-verbose") || s.equals("-v")) {
                verbose = true;
            }
            else if (s.equals("-interactive") || s.equals("-i")) {
                interactive = true;
            }
            else if (!s.equals("")) {
                System.err.println("usage: SpellCheck [-verbose] filename");
                return false;
            }
        }

        try {
            inputFile = new File(args[i++]);
        } catch (Exception e) {
            System.err.println("error opening file: " + e);
            return false;
        }

        return true;
    }

    /**
     * Returns the set of all possible transformations of distance
     * maxDistance from the given string. Only includes words that
     * are present in the frequencies HashMap.
     */
    public static HashSet<String> getTargets(String str, int maxDistance){
        String testStr;
        if(maxDistance < 1){
            return null;
        }
                
        str = str.trim();
        str = str.toLowerCase();

        HashSet<String> targetSet = new HashSet<>();

        // Generate targets
        for(int i = 0; i <= str.length(); i++){
            // Inserts
            char firstChar = 'a';
            for(int j = 0; j < 26; j++){
                if( i < str.length() ) {
                    testStr = str.substring(0,i) +  (char) (firstChar + j) + str.substring(i);
                    if (frequencies.containsKey(testStr))
                        targetSet.add(testStr);
                } else {
                    testStr = str.substring(0) + (char) (firstChar + j);
                    if (frequencies.containsKey(testStr))
                        targetSet.add(testStr);
                }
            }
                        
            // Only inserts use index == string length
            if(i == str.length())
                break;

            // Deletes
            if(i == 0 && str.length() > 1){
                testStr = str.substring(1);
                if (frequencies.containsKey(testStr))
                    targetSet.add(testStr);
            } else if(i == str.length() - 1){
                testStr = str.substring(0, str.length() - 1);
                if (frequencies.containsKey(testStr))
                    targetSet.add(testStr);
            } else {
                testStr = str.substring(0, i) + str.substring(i + 1);
                if (frequencies.containsKey(testStr))
                    targetSet.add(testStr);
            }

            // Replaces
            for(int j = 0; j < 26; j++){
                if(str.charAt(i) == (char) (firstChar + j))
                    continue;

                testStr = str.substring(0,i) + (char) (firstChar + j) + str.substring(i+1); 
                if (frequencies.containsKey(testStr))
                    targetSet.add(testStr);
            }


            // Transposes
            if( i < str.length() - 1 ){
                testStr =  str.substring(0,i) + str.charAt(i+1) + str.charAt(i) + str.substring(i+2);
                if (frequencies.containsKey(testStr)) 
                    targetSet.add(testStr);
            }
        }

        if(maxDistance == 1)
            return new HashSet<String>(targetSet);
                
        HashSet<String> returnSet = new HashSet<>(targetSet);
        for(String s : targetSet){
            returnSet.addAll( getTargets( s, maxDistance - 1));
        }

        return returnSet;
    }

    /**
     * Repeatedly prompts the user for a word to correct and
     * prints the corrected version of that word.
     */
    public static void handleInteractive() {
        while (true) {
            System.out.print("Enter the word or -1 to quit: ");
            String input = System.console().readLine().trim();
            if (input.equals("-1")) return;

            String corrected = getCorrection(getTargets(input, MAX_EDIT_DISTANCE), input);
            System.out.println("Result: " + corrected + "\n");
        }
    }
}
