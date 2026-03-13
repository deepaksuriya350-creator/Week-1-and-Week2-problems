import java.util.*;

public class Autocomplete{

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
        Set<String> queries = new HashSet<>(); // store queries passing through node
    }

    private TrieNode root;
    private Map<String, Integer> frequencyMap;
    private int topK;

    public Autocomplete(int topK) {
        root = new TrieNode();
        frequencyMap = new HashMap<>();
        this.topK = topK;
    }

    // Add or update query frequency
    public void addQuery(String query) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
            node.queries.add(query);
        }
        node.isEnd = true;
    }

    // Get top K suggestions for prefix
    public List<String> getSuggestions(String prefix) {

        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return new ArrayList<>();
        }

        PriorityQueue<String> pq = new PriorityQueue<>((a, b) -> {
            int freqCompare = Integer.compare(frequencyMap.get(a), frequencyMap.get(b));
            if (freqCompare == 0) return b.compareTo(a); // lexicographical tie-breaker
            return freqCompare;
        });

        for (String query : node.queries) {
            pq.offer(query);
            if (pq.size() > topK) pq.poll();
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) result.add(pq.poll());

        Collections.reverse(result);
        return result;
    }

    // Suggest closest matches for typos
    public List<String> suggestCorrections(String input) {
        List<String> candidates = new ArrayList<>();
        for (String query : frequencyMap.keySet()) {
            if (levenshteinDistance(query, input) <= 1) { // edit distance <=1
                candidates.add(query);
            }
        }
        candidates.sort((a, b) -> frequencyMap.get(b) - frequencyMap.get(a));
        return candidates.size() > topK ? candidates.subList(0, topK) : candidates;
    }

    // Levenshtein distance
    private int levenshteinDistance(String a, String b) {
        int n = a.length(), m = b.length();
        int[][] dp = new int[n+1][m+1];
        for (int i=0;i<=n;i++) dp[i][0]=i;
        for (int j=0;j<=m;j++) dp[0][j]=j;
        for (int i=1;i<=n;i++) {
            for (int j=1;j<=m;j++) {
                if (a.charAt(i-1)==b.charAt(j-1)) dp[i][j]=dp[i-1][j-1];
                else dp[i][j]=1+Math.min(dp[i-1][j-1], Math.min(dp[i-1][j], dp[i][j-1]));
            }
        }
        return dp[n][m];
    }

    // Demo
    public static void main(String[] args) {
        Autocomplete system = new Autocomplete(10);


        system.addQuery("java tutorial");
        system.addQuery("javascript");
        system.addQuery("java download");
        system.addQuery("java tutorial");
        system.addQuery("java 21 features");

        System.out.println("Suggestions for 'jav':");
        for (String s : system.getSuggestions("jav")) {
            System.out.println(s + " (" + system.frequencyMap.get(s) + " searches)");
        }

        System.out.println("\nTypo corrections for 'jvaa':");
        for (String s : system.suggestCorrections("jvaa")) {
            System.out.println(s + " (" + system.frequencyMap.get(s) + " searches)");
        }
    }
}