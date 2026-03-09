import java.util.*;

class VideoData {
    String videoId;
    String content; // simplified representation
    VideoData(String id, String content) {
        this.videoId = id;
        this.content = content;
    }
}

// LRU Cache using LinkedHashMap
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // access-order
        this.capacity = capacity;
    }
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

class MultiLevelCache {
    private LRUCache<String, VideoData> L1 = new LRUCache<>(10000);
    private LRUCache<String, VideoData> L2 = new LRUCache<>(100000);
    private Map<String, VideoData> L3 = new HashMap<>(); // database

    private int hitsL1 = 0, hitsL2 = 0, hitsL3 = 0, misses = 0;

    // Load video into DB (L3)
    public void addToDatabase(String videoId, String content) {
        L3.put(videoId, new VideoData(videoId, content));
    }

    // Get video
    public VideoData getVideo(String videoId) {
        if (L1.containsKey(videoId)) {
            hitsL1++;
            return L1.get(videoId);
        } else if (L2.containsKey(videoId)) {
            hitsL2++;
            VideoData data = L2.get(videoId);
            promoteToL1(videoId, data);
            return data;
        } else if (L3.containsKey(videoId)) {
            hitsL3++;
            VideoData data = L3.get(videoId);
            promoteToL2(videoId, data);
            return data;
        } else {
            misses++;
            return null;
        }
    }

    // Promote from L2 → L1
    private void promoteToL1(String videoId, VideoData data) {
        L1.put(videoId, data);
    }

    // Promote from L3 → L2
    private void promoteToL2(String videoId, VideoData data) {
        L2.put(videoId, data);
    }

    // Invalidate cache when content updates
    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        L3.remove(videoId);
    }

    // Stats
    public void getStatistics() {
        int total = hitsL1 + hitsL2 + hitsL3 + misses;
        System.out.println("L1: Hit Rate " + percent(hitsL1, total) + "%");
        System.out.println("L2: Hit Rate " + percent(hitsL2, total) + "%");
        System.out.println("L3: Hit Rate " + percent(hitsL3, total) + "%");
        System.out.println("Overall: Hit Rate " + percent(hitsL1+hitsL2+hitsL3, total) + "%");
    }

    private double percent(int part, int total) {
        return total == 0 ? 0 : (part * 100.0 / total);
    }

    // Demo
    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache();

        // Populate DB
        cache.addToDatabase("video_123", "Breaking News Clip");
        cache.addToDatabase("video_999", "Movie Trailer");

        // Access videos
        System.out.println("getVideo(video_123): " + cache.getVideo("video_123").content);
        System.out.println("getVideo(video_123): " + cache.getVideo("video_123").content);
        System.out.println("getVideo(video_999): " + cache.getVideo("video_999").content);

        cache.getStatistics();
    }
}
