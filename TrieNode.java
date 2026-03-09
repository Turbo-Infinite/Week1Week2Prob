import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord;
    int frequency; // frequency of query
}

class AutocompleteSystem {
    private TrieNode root = new TrieNode();
    private HashMap<String, Integer> globalFrequency = new HashMap<>();

    // Insert query into Trie
    public void insert(String query, int freq) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEndOfWord = true;
        node.frequency += freq;
        globalFrequency.put(query, globalFrequency.getOrDefault(query, 0) + freq);
    }

    // Update frequency when new search happens
    public void updateFrequency(String query) {
        insert(query, 1);
    }

    // Get top 10 suggestions for prefix
    public List<String> search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) return Collections.emptyList();
            node = node.children.get(c);
        }

        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>((a, b) -> a.getValue() - b.getValue()); // min-heap

        dfs(node, new StringBuilder(prefix), pq);

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll().getKey());
        }
        Collections.reverse(result); // highest freq first
        return result;
    }

    // DFS to collect queries
    private void dfs(TrieNode node, StringBuilder prefix,
                     PriorityQueue<Map.Entry<String, Integer>> pq) {
        if (node.isEndOfWord) {
            String query = prefix.toString();
            int freq = globalFrequency.getOrDefault(query, 0);
            pq.offer(new AbstractMap.SimpleEntry<>(query, freq));
            if (pq.size() > 10) pq.poll(); // keep only top 10
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            dfs(entry.getValue(), prefix, pq);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    // Demo
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        system.insert("java tutorial", 1234567);
        system.insert("javascript", 987654);
        system.insert("java download", 456789);
        system.insert("java 21 features", 100);

        System.out.println("search(\"jav\") → " + system.search("jav"));

        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");
        System.out.println("Updated frequency for 'java 21 features': "
                + system.search("java 21"));
    }
}
