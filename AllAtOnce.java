import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class AllAtOnce {
    // Global storage for assembly code produced during code generation.
    static List<String> assemblyCode = new ArrayList<>();
    // Temporary counter for generating temporary variable names in three-address code.
    static int tempCounter = 1;

    public static void main(String[] args) {
        // The V language program 
        String[] program = {
            "BEGIN",                                      // Line 1
            "INTEGER A, B, C, E, M, N, G, H, I, a, c",      // Line 2
            "INPUT A, B, C",                              // Line 3
            "LET B = A */ M",                             // Line 4 - error: illegal operator combination ("*/")
            "LET G = a + c",                              // Line 5 - correct; full processing required.
            "temp = <s%**h - j / w +d +*$&;",               // Line 6 - error: contains illegal symbols and semicolon.
            "M = A/B+C",                                  // Line 7 - correct; full processing required.
            "N = G/H-I+a*B/c",                            // Line 8 - correct; full processing required.
            "WRITE M",                                    // Line 9
            "WRITEE F;",                                  // Line 10 - error: misspelled keyword and semicolon at end.
            "END"                                         // Line 11
        };

        // Process each line independently.
        for (int i = 0; i < program.length; i++) {
            String line = program[i];
            int lineNumber = i + 1;
            System.out.println("Processing line " + lineNumber + ": " + line);

            // Perform error checking
            String errorMsg = checkForErrors(line);
            if (errorMsg != null) {
                System.out.println("Error detected: " + errorMsg);
            } else {
                // Only lines 5, 7, and 8 are fully compiled.
                if (lineNumber == 5 || lineNumber == 7 || lineNumber == 8) {
                    // Stage 1: Lexical Analysis
                    List<Token> tokens = lexicalAnalysis(line);
                    System.out.println("Lexical Analysis Tokens:");
                    for (Token t : tokens) {
                        System.out.println("  [" + t.type + " : " + t.value + "]");
                    }
                    
                    // Stage 2: Syntax Analysis
                    try {
                        syntaxAnalysis(tokens);
                        System.out.println("Syntax Analysis: No Syntax Errors.");
                    } catch (Exception e) {
                        System.out.println("Syntax Analysis Error: " + e.getMessage());
                        continue; // Proceed to next line.
                    }
                    
                    // Stage 3: Semantic Analysis
                    try {
                        semanticAnalysis(tokens);
                        System.out.println("Semantic Analysis: No Semantic Errors.");
                    } catch (Exception e) {
                        System.out.println("Semantic Analysis Error: " + e.getMessage());
                        continue;
                    }
                    
                    // Stage 4: Intermediate Code Generation (ICR)
                    List<String> icr = intermediateCode(tokens);
                    System.out.println("Intermediate Code (Three-Address Code):");
                    for (String s : icr) {
                        System.out.println("  " + s);
                    }
                    
                    // Clear assembly storage and reset temp counter for fresh code generation.
                    assemblyCode.clear();
                    // Stage 5: Code Generation
                    codeGeneration(icr);
                    System.out.println("Assembly Code:");
                    for (String s : assemblyCode) {
                        System.out.println("  " + s);
                    }
                    
                    // Stage 6: Code Optimization
                    codeOptimization();
                    System.out.println("Optimized Assembly Code:");
                    for (String s : assemblyCode) {
                        System.out.println("  " + s);
                    }
                    
                    // Stage 7: Target Machine Code Generation
                    System.out.println("Target Machine Code (Binary):");
                    targetMachineCode();
                } else {
                    System.out.println("No errors detected. (This line cannot be processed)");
                }
            }
            System.out.println(); // Blank line for clarity.
        }
    }

    //Checks a given line for errors based on the assignment rules.
    // Rules include:
    // - Misspelling (e.g. "WRITEE" instead of "WRITE") → Lexical error.
    // - Illegal symbols: %, $, &, <, > → Semantic error.
    // - Illegal operator combinations: +*, -/, */, *+ → Syntax error.
    // - If the line ends with a semicolon (after trimming) → Syntax error.
    // - Any digits (0-9) are not allowed → Syntax error.
  
    public static String checkForErrors(String line) {
        if (line.contains("WRITEE"))
            return "Lexical Error (Misspelled Keyword)";
        if (line.contains("%") || line.contains("$") || line.contains("&") ||
            line.contains("<") || line.contains(">"))
            return "Semantic Error (Invalid Symbols)";
        if (line.contains("+*") || line.contains("-/") || line.contains("*/") || line.contains("*+"))
            return "Syntax Error (Illegal Operator Combination)";
        if (line.trim().endsWith(";"))
            return "Syntax Error (Semicolon at end not allowed)";
        if (line.matches(".*[0-9].*"))
            return "Syntax Error (Numbers not allowed)";
        return null;
    }

    // ---------------- Stage 1: Lexical Analysis ----------------
    public static List<Token> lexicalAnalysis(String input) {
        System.out.println("Performing Lexical Analysis...");
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            // If a letter is encountered, accumulate as a word.
            if (Character.isLetter(c)) {
                int start = i;
                while (i < input.length() && Character.isLetter(input.charAt(i))) {
                    i++;
                }
                String word = input.substring(start, i);
                // Words longer than one letter that match our keywords are KEYWORD; all single-letter words are treated as IDENTIFIER.
                if (word.length() > 1 && isKeyword(word))
                    tokens.add(new Token("KEYWORD", word));
                else
                    tokens.add(new Token("IDENTIFIER", word));
            } else if (isOperator(c)) {
                tokens.add(new Token("OPERATOR", String.valueOf(c)));
                i++;
            } else if (c == '=') {
                tokens.add(new Token("SYMBOL", "="));
                i++;
            } else if (c == ',') {
                tokens.add(new Token("SEPARATOR", ","));
                i++;
            } else {
                tokens.add(new Token("UNKNOWN", String.valueOf(c)));
                i++;
            }
        }
        return tokens;
    }
    
    // Helper to recognize valid keywords.
    public static boolean isKeyword(String word) {
        return word.equals("BEGIN") || word.equals("INTEGER") || word.equals("LET") ||
               word.equals("INPUT") || word.equals("WRITE") || word.equals("END");
    }
    
    // Helper to recognize operator characters.
    public static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }
    
    // ---------------- Stage 2: Syntax Analysis ----------------
    // This parser expects an assignment statement of the form:
    //    [ LET ] identifier '=' expression
    // where expression = factor { operator factor } and factor = identifier.
    public static void syntaxAnalysis(List<Token> tokens) throws Exception {
        System.out.println("Performing Syntax Analysis...");
        int index = 0;
        // Optional "LET" keyword.
        if (tokens.get(index).type.equals("KEYWORD") && tokens.get(index).value.equals("LET"))
            index++;
        
        if (index >= tokens.size() || !tokens.get(index).type.equals("IDENTIFIER"))
            throw new Exception("Expected identifier in assignment");
        index++; // Consume identifier.
        
        if (index >= tokens.size() || !tokens.get(index).type.equals("SYMBOL") || !tokens.get(index).value.equals("="))
            throw new Exception("Expected '=' in assignment");
        index++; // Consume '='.
        
        // Parse expression: must be at least one factor.
        index = parseExpression(tokens, index);
        if (index < tokens.size())
            throw new Exception("Unexpected tokens after valid assignment expression");
    }
    
    // Recursively parse an expression of the form: factor { operator factor }
    private static int parseExpression(List<Token> tokens, int index) throws Exception {
        index = parseFactor(tokens, index);
        while (index < tokens.size() && tokens.get(index).type.equals("OPERATOR")) {
            index++; // Consume operator.
            index = parseFactor(tokens, index);
        }
        return index;
    }
    
    // Parse a factor; here simply an identifier.
    private static int parseFactor(List<Token> tokens, int index) throws Exception {
        if (index >= tokens.size() || !tokens.get(index).type.equals("IDENTIFIER"))
            throw new Exception("Expected identifier in expression");
        return index + 1;
    }
    
    // ---------------- Stage 3: Semantic Analysis ----------------
    // Ensures that identifiers contain only letters and that no unknown tokens are present.
    public static void semanticAnalysis(List<Token> tokens) throws Exception {
        System.out.println("Performing Semantic Analysis...");
        for (Token token : tokens) {
            if (token.type.equals("IDENTIFIER")) {
                if (!token.value.matches("[a-zA-Z]+"))
                    throw new Exception("Invalid identifier: " + token.value);
            }
            if(token.type.equals("UNKNOWN"))
                throw new Exception("Unknown token encountered: " + token.value);
        }
    }
    
    // ---------------- Stage 4: Intermediate Code Representation (ICR) ----------------
    // Generates three-address code (TAC) by converting the right-hand side expression
    // to postfix notation and then generating instructions.
    public static List<String> intermediateCode(List<Token> tokens) {
        System.out.println("Generating Intermediate Code Representation...");
        List<String> tac = new ArrayList<>();
        String lhs = "";
        int index = 0;
        // Skip the optional LET keyword.
        if (tokens.get(index).type.equals("KEYWORD") && tokens.get(index).value.equals("LET"))
            index++;
        lhs = tokens.get(index).value;
        index++; // Consume LHS identifier.
        index++; // Consume '=' symbol.
        
        // Remaining tokens form the expression.
        List<Token> exprTokens = new ArrayList<>();
        while (index < tokens.size()) {
            exprTokens.add(tokens.get(index));
            index++;
        }
        // Convert infix expression to postfix notation.
        List<Token> postfix = infixToPostfix(exprTokens);
        Stack<String> stack = new Stack<>();
        for (Token token : postfix) {
            if (token.type.equals("IDENTIFIER")) {
                stack.push(token.value);
            } else if (token.type.equals("OPERATOR")) {
                String op2 = stack.pop();
                String op1 = stack.pop();
                String temp = "t" + tempCounter++;
                tac.add(temp + " = " + op1 + " " + token.value + " " + op2);
                stack.push(temp);
            }
        }
        // Final assignment to LHS.
        if (!stack.isEmpty()) {
            String result = stack.pop();
            tac.add(lhs + " = " + result);
        }
        return tac;
    }
    
    // Helper: Convert an infix token list to postfix using the Shunting Yard algorithm.
    private static List<Token> infixToPostfix(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Stack<Token> opStack = new Stack<>();
        for (Token token : tokens) {
            if (token.type.equals("IDENTIFIER")) {
                output.add(token);
            } else if (token.type.equals("OPERATOR")) {
                while (!opStack.isEmpty() && opStack.peek().type.equals("OPERATOR") &&
                        precedence(opStack.peek().value) >= precedence(token.value))
                    output.add(opStack.pop());
                opStack.push(token);
            }
        }
        while (!opStack.isEmpty())
            output.add(opStack.pop());
        return output;
    }
    
    // Helper: Define operator precedence.
    private static int precedence(String op) {
        if (op.equals("*") || op.equals("/")) return 2;
        if (op.equals("+") || op.equals("-")) return 1;
        return 0;
    }
    
    // ---------------- Stage 5: Code Generation (CG) ----------------
    public static void codeGeneration(List<String> icr) {
        System.out.println("Generating Code (Assembly)...");
        for (String instr : icr) {
            if (instr.contains(" = ")) {
                String[] parts = instr.split(" = ");
                String target = parts[0].trim();
                String expr = parts[1].trim();
                if (expr.contains(" ")) {
                    String[] tokens = expr.split(" ");
                    if (tokens.length == 3) {
                        String op1 = tokens[0].trim();
                        String operator = tokens[1].trim();
                        String op2 = tokens[2].trim();
                        assemblyCode.add("LDA " + op1);
                        assemblyCode.add("OPER " + operator);
                        assemblyCode.add("LDA " + op2);
                        assemblyCode.add("STR " + target);
                    } else {
                        assemblyCode.add("STR " + target + " from " + expr);
                    }
                } else {
                    assemblyCode.add("MOV " + expr + " TO " + target);
                }
            }
        }
    }
    
    // ---------------- Stage 6: Code Optimization (CO) ----------------
    // A simple optimization pass that removes consecutive duplicate assembly instructions.
    public static void codeOptimization() {
        System.out.println("Optimizing Code...");
        List<String> optimized = new ArrayList<>();
        String prev = "";
        for (String ins : assemblyCode) {
            if (!ins.equals(prev))
                optimized.add(ins);
            prev = ins;
        }
        assemblyCode = optimized;
    }
    
    // ---------------- Stage 7: Target Machine Code (TMC) ----------------
    // Converts each assembly instruction into pseudo-binary code (8-bit per character).
    public static void targetMachineCode() {
        System.out.println("Generating Target Machine Code...");
        for (String asm : assemblyCode) {
            String binary = stringToBinary(asm);
            System.out.println("  " + binary);
        }
    }
    
    // Helper: Convert a string into its binary representation.
    public static String stringToBinary(String s) {
    StringBuilder binaryResult = new StringBuilder();
    String[] words = s.split(" "); // Split into words
    
    for (String word : words) {
        if (!word.isEmpty()) { // Skip empty words
            char firstChar = word.charAt(0);
            // Convert first character to 8-bit binary
            String binary = String.format("%8s", Integer.toBinaryString(firstChar)).replace(' ', '0');
            binaryResult.append(binary).append(" "); // Add space separator
        }
    }
    
    return binaryResult.toString().trim(); // Remove trailing space
}
}

// ---------------- Token Class ----------------
class Token {
    String type;
    String value;
    
    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }
}
