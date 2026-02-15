import java.util.*;

/**
 * SymbolTable Class
 * Manages symbol table for identifiers.
 * Stores: identifier name, type, first occurrence, frequency
 * CS4031 - Compiler Construction - Assignment 01
 */
public class SymbolTable {
    
    /**
     * Inner class to store symbol information
     */
    private static class SymbolInfo {
        String name;
        String type;        // For now, we'll use "IDENTIFIER"
        int firstLine;
        int firstColumn;
        int frequency;
        
        SymbolInfo(String name, int line, int column) {
            this.name = name;
            this.type = "IDENTIFIER";
            this.firstLine = line;
            this.firstColumn = column;
            this.frequency = 1;
        }
        
        void incrementFrequency() {
            frequency++;
        }
        
        @Override
        public String toString() {
            return String.format("%-20s %-15s Line: %-4d Col: %-4d Frequency: %d", 
                               name, type, firstLine, firstColumn, frequency);
        }
    }
    
    private Map<String, SymbolInfo> symbols;
    
    /**
     * Constructor
     */
    public SymbolTable() {
        symbols = new LinkedHashMap<>(); // Preserve insertion order
    }
    
    /**
     * Add or update an identifier in the symbol table
     * @param name Identifier name
     * @param line Line number of occurrence
     * @param column Column number of occurrence
     */
    public void addIdentifier(String name, int line, int column) {
        if (symbols.containsKey(name)) {
            // Identifier already exists, increment frequency
            symbols.get(name).incrementFrequency();
        } else {
            // New identifier
            symbols.put(name, new SymbolInfo(name, line, column));
        }
    }
    
    /**
     * Check if an identifier exists in the symbol table
     * @param name Identifier name
     * @return true if identifier exists
     */
    public boolean contains(String name) {
        return symbols.containsKey(name);
    }
    
    /**
     * Get symbol information
     * @param name Identifier name
     * @return SymbolInfo object or null if not found
     */
    public String getSymbolInfo(String name) {
        SymbolInfo info = symbols.get(name);
        return info != null ? info.toString() : null;
    }
    
    /**
     * Get total number of unique identifiers
     * @return Number of unique identifiers
     */
    public int getUniqueIdentifierCount() {
        return symbols.size();
    }
    
    /**
     * Get total number of identifier occurrences
     * @return Total frequency of all identifiers
     */
    public int getTotalIdentifierOccurrences() {
        int total = 0;
        for (SymbolInfo info : symbols.values()) {
            total += info.frequency;
        }
        return total;
    }
    
    /**
     * Print the symbol table
     */
    public void printSymbolTable() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SYMBOL TABLE");
        System.out.println("=".repeat(80));
        
        if (symbols.isEmpty()) {
            System.out.println("No identifiers found.");
        } else {
            System.out.println(String.format("%-20s %-15s %-10s %-10s %s", 
                                           "Name", "Type", "First Line", "First Col", "Frequency"));
            System.out.println("-".repeat(80));
            
            for (SymbolInfo info : symbols.values()) {
                System.out.println(info);
            }
            
            System.out.println("-".repeat(80));
            System.out.println("Total unique identifiers: " + getUniqueIdentifierCount());
            System.out.println("Total identifier occurrences: " + getTotalIdentifierOccurrences());
        }
        System.out.println("=".repeat(80));
    }
    
    /**
     * Clear the symbol table
     */
    public void clear() {
        symbols.clear();
    }
}
