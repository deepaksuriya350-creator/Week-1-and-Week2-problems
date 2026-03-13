import java.util.*;

public class Plagiarism {

    private int n; // n-gram size

    // ngram -> set of document IDs
    private Map<String, Set<String>> ngramIndex = new HashMap<>();

    // documentId -> list of ngrams
    private Map<String, List<String>> documentNgrams = new HashMap<>();

    public Plagiarism(int n) {
        this.n = n;
    }

    // Add document to database
    public void addDocument(String docId, String text) {

        List<String> ngrams = generateNGrams(text);

        documentNgrams.put(docId, ngrams);

        for (String gram : ngrams) {

            ngramIndex
                    .computeIfAbsent(gram, k -> new HashSet<>())
                    .add(docId);
        }
    }

    // Generate n-grams
    private List<String> generateNGrams(String text) {

        List<String> result = new ArrayList<>();

        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i <= words.length - n; i++) {

            StringBuilder gram = new StringBuilder();

            for (int j = 0; j < n; j++) {
                gram.append(words[i + j]).append(" ");
            }

            result.add(gram.toString().trim());
        }

        return result;
    }

    // Analyze new document
    public void analyzeDocument(String docId, String text) {

        List<String> ngrams = generateNGrams(text);

        Map<String, Integer> matchCounts = new HashMap<>();

        for (String gram : ngrams) {

            if (ngramIndex.containsKey(gram)) {

                for (String existingDoc : ngramIndex.get(gram)) {

                    matchCounts.put(
                            existingDoc,
                            matchCounts.getOrDefault(existingDoc, 0) + 1
                    );
                }
            }
        }

        System.out.println("Extracted " + ngrams.size() + " n-grams");

        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {

            String existingDoc = entry.getKey();
            int matches = entry.getValue();

            double similarity = (matches * 100.0) / ngrams.size();

            System.out.println(
                    "Found " + matches + " matching n-grams with \"" + existingDoc + "\""
            );

            System.out.printf("Similarity: %.2f%%", similarity);

            if (similarity > 60) {
                System.out.println(" (PLAGIARISM DETECTED)");
            } else if (similarity > 10) {
                System.out.println(" (suspicious)");
            } else {
                System.out.println();
            }
        }
    }

    // Demo
    public static void main(String[] args) {

        Plagiarism detector = new Plagiarism(5);

        String essay1 =
                "Artificial intelligence is transforming the world of technology and science";

        String essay2 =
                "Artificial intelligence is transforming the world of business and technology";

        String essay3 =
                "Climate change affects agriculture and the global economy";

        detector.addDocument("essay_089.txt", essay1);
        detector.addDocument("essay_092.txt", essay2);
        detector.addDocument("essay_093.txt", essay3);

        String newEssay =
                "Artificial intelligence is transforming the world of technology and science rapidly";

        detector.analyzeDocument("essay_123.txt", newEssay);
    }
}