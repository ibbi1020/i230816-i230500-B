import java.io.*;
import java.util.*;

/**
 * CS4031 Compiler Construction - Assignment 01
 * Task 2.3: Comparison of Manual and JFlex Scanners
 */
public class Comparison {

    // ADAPT THIS PATH IF NEEDED (e.g. "../tests/" if running from src)
    private static final String TEST_DIR = "tests/";
    
    private static final String[] TEST_FILES = {
        "test1.lang", 
        "test2.lang", 
        "test3.lang", 
        "test4.lang", 
        "test5.lang"
    };

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("              SCANNER COMPARISON & BENCHMARK                     ");
        System.out.println("=================================================================\n");

        for (String filename : TEST_FILES) {
            File file = new File(TEST_DIR + filename);
            
            // Handle case where tests might be in parent folder or current folder
            if (!file.exists()) {
                 file = new File("../" + TEST_DIR + filename);
                 if (!file.exists()) {
                    System.out.println("[WARNING] Test file not found: " + filename);
                    continue;
                 }
            }

            System.out.println("PROCESSING: " + filename);
            System.out.println("-----------------------------------------------------------------");
            
            try {
                // 1. Read File Content into String (Required for ManualScanner)
                String fileContent = readFileToString(file);

                // 2. Run Manual Scanner
                long startTimeManual = System.nanoTime();
                List<Token> manualTokens = runManualScanner(fileContent);
                long endTimeManual = System.nanoTime();
                double durationManual = (endTimeManual - startTimeManual) / 1_000_000.0; 

                // 3. Run JFlex Scanner
                long startTimeJFlex = System.nanoTime();
                List<Token> jflexTokens = runJFlexScanner(file);
                long endTimeJFlex = System.nanoTime();
                double durationJFlex = (endTimeJFlex - startTimeJFlex) / 1_000_000.0;

                // 4. Compare Outputs
                boolean match = compareTokens(manualTokens, jflexTokens);

                // 5. Print Statistics
                System.out.printf("%-20s | %-20s | %-15s\n", "Scanner Type", "Token Count", "Time (ms)");
                System.out.println("-----------------------------------------------------------------");
                System.out.printf("%-20s | %-20d | %-15.4f\n", "Manual Scanner", manualTokens.size(), durationManual);
                System.out.printf("%-20s | %-20d | %-15.4f\n", "JFlex Scanner", jflexTokens.size(), durationJFlex);
                System.out.println("-----------------------------------------------------------------");
                
                if (match) {
                    System.out.println("[SUCCESS] Outputs are IDENTICAL.");
                } else {
                    System.err.println("[FAILURE] Outputs DIFFER. See details above.");
                }
                System.out.println("\n");

            } catch (Exception e) {
                System.err.println("Error processing " + filename + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads file content to string for ManualScanner
     */
    private static String readFileToString(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * Runs your existing ManualScanner
     */
    private static List<Token> runManualScanner(String input) {
        ManualScanner scanner = new ManualScanner(input);
        List<Token> rawTokens = scanner.scan();
        
        // Filter out EOF token for comparison (since JFlex typically returns null at end)
        List<Token> cleanTokens = new ArrayList<>();
        for (Token t : rawTokens) {
            if (t.getType() != TokenType.EOF) {
                cleanTokens.add(t);
            }
        }
        return cleanTokens;
    }

    /**
     * Runs the JFlex generated Yylex scanner
     */
    private static List<Token> runJFlexScanner(File file) throws IOException {
        List<Token> tokens = new ArrayList<>();
        
        // Initialize Yylex with FileReader
        Reader reader = new FileReader(file);
        Yylex scanner = new Yylex(reader);
        
        Token t;
        // JFlex returns null on EOF
        while ((t = scanner.yylex()) != null) {
            tokens.add(t);
        }
        
        reader.close();
        return tokens;
    }

    /**
     * Compares two lists of tokens
     */
    private static boolean compareTokens(List<Token> manual, List<Token> jflex) {
        boolean match = true;
        int size = Math.max(manual.size(), jflex.size());
        
        System.out.printf("%-5s | %-35s | %-35s\n", "#", "Manual Output", "JFlex Output");
        System.out.println("---------------------------------------------------------------------------------");

        for (int i = 0; i < size; i++) {
            String mStr = (i < manual.size()) ? manual.get(i).toString() : "---";
            String jStr = (i < jflex.size()) ? jflex.get(i).toString() : "---";
            
            // Compare string representations
            boolean rowMatch = mStr.equals(jStr);
            if (!rowMatch) match = false;
            
            String status = rowMatch ? " " : "*"; 
            //System.out.printf("%-5s | %-35s | %-35s %s\n", (i+1), formatTruncate(mStr), formatTruncate(jStr), status);
            
            if (!match && i > 15 && (size - i) > 5) {
                System.out.println("... (output truncated) ...");
                break;
            }
        }
        return match;
    }
    
    private static String formatTruncate(String s) {
        if (s.length() > 35) return s.substring(0, 32) + "...";
        return s;
    }
}