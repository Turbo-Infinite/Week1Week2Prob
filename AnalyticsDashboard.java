import java.util.*;
import java.util.concurrent.*;

class AnalyticsDashboard {
    private HashMap<String, Integer> pageViews = new HashMap<>();
    private HashMap<String, Set<String>> uniqueVisitors = new HashMap<>();
    private HashMap<String, Integer> trafficSources = new HashMap<>();

    // Process incoming event
    public synchronized void processEvent(String url, String userId, String source) {
        // Page views
        pageViews.put(url, pageViews.getOrDefault(url, 0) + 1);

        // Unique visitors
        uniqueVisitors.putIfAbsent(url, new HashSet<>());
        uniqueVisitors.get(url).add(userId);

        // Traffic sources
        trafficSources.put(source, trafficSources.getOrDefault(source, 0) + 1);
    }

    // Get top 10 pages
    private List<String> getTopPages() {
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
        pq.addAll(pageViews.entrySet());

        List<String> topPages = new ArrayList<>();
        int rank = 1;
        while (!pq.isEmpty() && rank <= 10) {
            Map.Entry<String, Integer> entry = pq.poll();
            String url = entry.getKey();
            int views = entry.getValue();
            int uniques = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
            topPages.add(rank + ". " + url + " - " + views + " views (" + uniques + " unique)");
            rank++;
        }
        return topPages;
    }

    // Get traffic source breakdown
    private Map<String, Double> getTrafficSourceStats() {
        int total = trafficSources.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> stats = new HashMap<>();
        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            stats.put(entry.getKey(), (entry.getValue() * 100.0) / total);
        }
        return stats;
    }

    // Print dashboard
    public void getDashboard() {
        System.out.println("Top Pages:");
        for (String page : getTopPages()) {
            System.out.println(page);
        }

        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, Double> entry : getTrafficSourceStats().entrySet()) {
            System.out.println(entry.getKey() + ": " + String.format("%.1f", entry.getValue()) + "%");
        }
    }

    // Demo with scheduled updates
    public static void main(String[] args) {
        AnalyticsDashboard dashboard = new AnalyticsDashboard();

        // Simulate events
        dashboard.processEvent("/article/breaking-news", "user_123", "google");
        dashboard.processEvent("/article/breaking-news", "user_456", "facebook");
        dashboard.processEvent("/sports/championship", "user_789", "direct");

        // Update dashboard every 5 seconds
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(dashboard::getDashboard, 0, 5, TimeUnit.SECONDS);
    }
}
