import java.util.*;

/**
 * Text Compression Tool using Huffman Coding
 * PS No. 99
 *
 * Compile: javac TextCompressionTool.java
 * Run:     java TextCompressionTool
 */
public class TextCompressionTool {

    private static class Node {
        char ch;
        int frequency;
        Node left, right;

        Node(char ch, int frequency) {
            this.ch = ch;
            this.frequency = frequency;
        }

        Node(Node left, Node right) {
            this.ch = '\0';
            this.frequency = left.frequency + right.frequency;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf() {
            return left == null && right == null;
        }
    }

    private static final Scanner scanner = new Scanner(System.in);
    private static Node currentRoot;
    private static Map<Character, String> currentCodes = new LinkedHashMap<>();
    private static String lastOriginalText = "";

    public static void main(String[] args) {
        System.out.println("======================================");
        System.out.println("       TEXT COMPRESSION TOOL");
        System.out.println("          Huffman Coding");
        System.out.println("======================================");

        while (true) {
            System.out.println("\n1. Compress text");
            System.out.println("2. Decode using last generated tree");
            System.out.println("3. Show code table");
            System.out.println("4. Decode using a custom code table");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    compressText();
                    break;
                case "2":
                    decodeWithCurrentTree();
                    break;
                case "3":
                    showCodeTable();
                    break;
                case "4":
                    decodeWithCustomTable();
                    break;
                case "5":
                    System.out.println("Thank you for using Text Compression Tool!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 5.");
            }
        }
    }

    private static void compressText() {
        System.out.print("Enter text to compress: ");
        String text = scanner.nextLine();

        if (text.isEmpty()) {
            System.out.println("Input is empty. Nothing to compress.");
            return;
        }

        lastOriginalText = text;
        Map<Character, Integer> frequencies = countFrequencies(text);
        currentRoot = buildHuffmanTree(frequencies);
        currentCodes = new LinkedHashMap<>();
        generateCodes(currentRoot, "", currentCodes);

        StringBuilder encoded = new StringBuilder();
        for (char ch : text.toCharArray()) {
            encoded.append(currentCodes.get(ch));
        }

        int originalBits = text.length() * 8;
        int encodedBits = encoded.length();
        double saved = ((originalBits - encodedBits) * 100.0) / originalBits;

        System.out.println("\n--- Compression Result ---");
        printCodeTable(currentCodes);
        System.out.println("Encoded: " + encoded);
        System.out.println("Encoded length: " + encodedBits + " bits");
        System.out.println("Original: " + originalBits + " bits");
        System.out.printf(Locale.US, "Saved: %.2f%%%n", saved);
    }

    private static Map<Character, Integer> countFrequencies(String text) {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char ch : text.toCharArray()) {
            frequencies.put(ch, frequencies.getOrDefault(ch, 0) + 1);
        }
        return frequencies;
    }

    private static Node buildHuffmanTree(Map<Character, Integer> frequencies) {
        // Frequency is the primary priority. Character order makes ties repeatable.
        PriorityQueue<Node> queue = new PriorityQueue<>((a, b) -> {
            int byFrequency = Integer.compare(a.frequency, b.frequency);
            if (byFrequency != 0) return byFrequency;
            return Character.compare(minCharacter(a), minCharacter(b));
        });

        for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
            queue.offer(new Node(entry.getKey(), entry.getValue()));
        }

        while (queue.size() > 1) {
            Node left = queue.poll();   // smaller frequency gets bit 0
            Node right = queue.poll(); // other node gets bit 1
            queue.offer(new Node(left, right));
        }
        return queue.poll();
    }

    private static char minCharacter(Node node) {
        if (node.isLeaf()) return node.ch;
        return (char) Math.min(minCharacter(node.left), minCharacter(node.right));
    }

    private static void generateCodes(Node node, String path,
                                      Map<Character, String> codes) {
        if (node == null) return;

        if (node.isLeaf()) {
            // A single distinct character is assigned code 0.
            codes.put(node.ch, path.isEmpty() ? "0" : path);
            return;
        }

        generateCodes(node.left, path + "0", codes);
        generateCodes(node.right, path + "1", codes);
    }

    private static void decodeWithCurrentTree() {
        if (currentRoot == null) {
            System.out.println("Please compress some text first to generate a Huffman tree.");
            return;
        }

        System.out.print("Enter a bit string to decode: ");
        String bits = scanner.nextLine().trim();

        if (!isBinary(bits)) {
            System.out.println("Invalid bit string. Only 0 and 1 are allowed.");
            return;
        }
        if (bits.isEmpty()) {
            System.out.println("Decoded text: ");
            return;
        }

        StringBuilder decoded = new StringBuilder();

        // Special handling for a tree containing only one distinct character.
        if (currentRoot.isLeaf()) {
            for (int i = 0; i < bits.length(); i++) {
                if (bits.charAt(i) != '0') {
                    System.out.println("Invalid bit string for the current one-character code table.");
                    return;
                }
                decoded.append(currentRoot.ch);
            }
            System.out.println("Decoded text: " + decoded);
            return;
        }

        Node node = currentRoot;
        for (char bit : bits.toCharArray()) {
            node = (bit == '0') ? node.left : node.right;
            if (node == null) {
                System.out.println("Invalid encoded string for the current Huffman tree.");
                return;
            }
            if (node.isLeaf()) {
                decoded.append(node.ch);
                node = currentRoot;
            }
        }

        if (node != currentRoot) {
            System.out.println("Invalid bit string: it ends before a complete character code.");
        } else {
            System.out.println("Decoded text: " + decoded);
        }
    }

    private static void showCodeTable() {
        if (currentCodes.isEmpty()) {
            System.out.println("No code table available. Compress text first.");
            return;
        }
        System.out.println("\n--- Huffman Code Table ---");
        printCodeTable(currentCodes);
    }

    private static void printCodeTable(Map<Character, String> codes) {
        System.out.println("Character\tCode");
        for (Map.Entry<Character, String> entry : codes.entrySet()) {
            System.out.println(displayCharacter(entry.getKey()) + "\t\t" + entry.getValue());
        }
    }

    private static String displayCharacter(char ch) {
        if (ch == ' ') return "[space]";
        if (ch == '\n') return "[newline]";
        if (ch == '\t') return "[tab]";
        return String.valueOf(ch);
    }

    private static void decodeWithCustomTable() {
        System.out.print("How many character-code pairs? ");
        int count;
        try {
            count = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            return;
        }

        if (count <= 0) {
            System.out.println("The number of pairs must be positive.");
            return;
        }

        Map<String, Character> reverseCodes = new HashMap<>();
        Set<Character> seenCharacters = new HashSet<>();

        for (int i = 0; i < count; i++) {
            System.out.print("Character for pair " + (i + 1) + ": ");
            String charInput = scanner.nextLine();
            if (charInput.length() != 1) {
                System.out.println("Enter exactly one character.");
                return;
            }
            char ch = charInput.charAt(0);

            System.out.print("Code for '" + displayCharacter(ch) + "': ");
            String code = scanner.nextLine().trim();

            if (code.isEmpty() || !isBinary(code)) {
                System.out.println("Each code must contain only 0 and 1.");
                return;
            }
            if (!seenCharacters.add(ch) || reverseCodes.containsKey(code)) {
                System.out.println("Duplicate character or code found.");
                return;
            }
            reverseCodes.put(code, ch);
        }

        // A valid Huffman code must be prefix-free.
        List<String> allCodes = new ArrayList<>(reverseCodes.keySet());
        for (int i = 0; i < allCodes.size(); i++) {
            for (int j = 0; j < allCodes.size(); j++) {
                if (i != j && allCodes.get(j).startsWith(allCodes.get(i))) {
                    System.out.println("Invalid code table: one code is a prefix of another.");
                    return;
                }
            }
        }

        System.out.print("Enter the encoded bit string: ");
        String bits = scanner.nextLine().trim();
        if (!isBinary(bits)) {
            System.out.println("Invalid bit string. Only 0 and 1 are allowed.");
            return;
        }

        StringBuilder decoded = new StringBuilder();
        StringBuilder pending = new StringBuilder();

        for (char bit : bits.toCharArray()) {
            pending.append(bit);
            String code = pending.toString();

            if (reverseCodes.containsKey(code)) {
                decoded.append(reverseCodes.get(code));
                pending.setLength(0);
            } else {
                boolean couldMatch = false;
                for (String knownCode : reverseCodes.keySet()) {
                    if (knownCode.startsWith(code)) {
                        couldMatch = true;
                        break;
                    }
                }
                if (!couldMatch) {
                    System.out.println("Invalid encoded string: no matching code.");
                    return;
                }
            }
        }

        if (pending.length() != 0) {
            System.out.println("Invalid encoded string: incomplete code at the end.");
        } else {
            System.out.println("Decoded text: " + decoded);
        }
    }

    private static boolean isBinary(String bits) {
        for (char ch : bits.toCharArray()) {
            if (ch != '0' && ch != '1') return false;
        }
        return true;
    }
}
