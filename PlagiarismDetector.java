import java.util.*;

class PlagiarismDetector {
    private HashMap<String, Set<String>> nGramIndex = new HashMap<>();
    private int n; // size of n-gram

    public PlagiarismDetector(int n) {
        this.n = n;
    }

    // Break document into n-grams
    private List<String> extractNGrams(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        List<String> nGrams = new ArrayList<>();
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                sb.append(words[i + j]).append(" ");
            }
            nGrams.add(sb.toString().trim());
        }
        return nGrams;
    }

    // Index a document
    public void indexDocument(String docId, String text) {
        List<String> nGrams = extractNGrams(text);
        for (String gram : nGrams) {
            nGramIndex.putIfAbsent(gram, new HashSet<>());
            nGramIndex.get(gram).add(docId);
        }
    }

    // Analyze similarity of a new document
    public Map<String, Double> analyzeDocument(String docId, String text) {
        List<String> nGrams = extractNGrams(text);
        Map<String, Integer> matchCount = new HashMap<>();

        for (String gram : nGrams) {
            if (nGramIndex.containsKey(gram)) {
                for (String otherDoc : nGramIndex.get(gram)) {
                    if (!otherDoc.equals(docId)) {
                        matchCount.put(otherDoc, matchCount.getOrDefault(otherDoc, 0) + 1);
                    }
                }
            }
        }

        // Calculate similarity percentage
        Map<String, Double> similarity = new HashMap<>();
        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            double percent = (entry.getValue() * 100.0) / nGrams.size();
            similarity.put(entry.getKey(), percent);
        }
        return similarity;
    }

    // Demo
    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector(5);

        // Index previous essays
        detector.indexDocument("essay_089", "This is a sample essay with some unique content.");
        detector.indexDocument("essay_092", "This essay contains a lot of repeated words and similar phrases.");

        // Analyze new essay
        Map<String, Double> results = detector.analyzeDocument("essay_123",
                "This essay contains a lot of repeated words and similar phrases.");

        for (Map.Entry<String, Double> entry : results.entrySet()) {
            System.out.println("Similarity with " + entry.getKey() + ": " + entry.getValue() + "%");
        }
    }
}
