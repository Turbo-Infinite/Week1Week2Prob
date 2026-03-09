import java.util.*;

public class UsernameChecker {
    private HashMap<String, Integer> userMap = new HashMap<>();
    private HashMap<String, Integer> attemptFrequency = new HashMap<>();

    // Add a username (simulate registration)
    public void register(String username, int userId) {
        userMap.put(username, userId);
    }

    // Check availability
    public boolean checkAvailability(String username) {
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !userMap.containsKey(username);
    }

    // Suggest alternatives
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add(username + "1");
        suggestions.add(username + "2");
        suggestions.add(username.replace("_", "."));
        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        return attemptFrequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    // Demo
    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();
        checker.register("john_doe", 101);
        checker.register("admin", 999);

        System.out.println(checker.checkAvailability("john_doe")); // false
        System.out.println(checker.checkAvailability("jane_smith")); // true
        System.out.println(checker.suggestAlternatives("john_doe")); // [john_doe1, john_doe2, john.doe]
        System.out.println("Most attempted: " + checker.getMostAttempted());
    }
}
