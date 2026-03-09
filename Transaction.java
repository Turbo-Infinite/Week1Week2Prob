import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long timestamp; // in ms

    Transaction(int id, int amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }
}

class FraudDetector {
    private List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // Classic Two-Sum
    public List<int[]> findTwoSum(int target) {
        HashMap<Integer, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum with time window (1 hour)
    public List<int[]> findTwoSumWithTime(int target) {
        List<int[]> result = new ArrayList<>();
        HashMap<Integer, Transaction> map = new HashMap<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                Transaction other = map.get(complement);
                if (Math.abs(t.timestamp - other.timestamp) <= 3600_000) {
                    result.add(new int[]{other.id, t.id});
                }
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // K-Sum (recursive)
    public List<List<Integer>> findKSum(int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(transactions, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(List<Transaction> txs, int k, int target, int start,
                           List<Integer> current, List<List<Integer>> result) {
        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }
        if (k == 0 || target < 0) return;

        for (int i = start; i < txs.size(); i++) {
            current.add(txs.get(i).id);
            backtrack(txs, k - 1, target - txs.get(i).amount, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // Duplicate detection
    public Map<String, Map<Integer, Set<String>>> detectDuplicates() {
        Map<String, Map<Integer, Set<String>>> duplicates = new HashMap<>();

        for (Transaction t : transactions) {
            duplicates.putIfAbsent(t.merchant, new HashMap<>());
            Map<Integer, Set<String>> byAmount = duplicates.get(t.merchant);
            byAmount.putIfAbsent(t.amount, new HashSet<>());
            byAmount.get(t.amount).add(t.account);
        }

        // Filter only duplicates (same amount, same merchant, multiple accounts)
        Map<String, Map<Integer, Set<String>>> suspicious = new HashMap<>();
        for (String merchant : duplicates.keySet()) {
            for (Map.Entry<Integer, Set<String>> entry : duplicates.get(merchant).entrySet()) {
                if (entry.getValue().size() > 1) {
                    suspicious.putIfAbsent(merchant, new HashMap<>());
                    suspicious.get(merchant).put(entry.getKey(), entry.getValue());
                }
            }
        }
        return suspicious;
    }

    // Demo
    public static void main(String[] args) {
        FraudDetector fd = new FraudDetector();

        fd.addTransaction(new Transaction(1, 500, "Store A", "acc1", System.currentTimeMillis()));
        fd.addTransaction(new Transaction(2, 300, "Store B", "acc2", System.currentTimeMillis() + 900000));
        fd.addTransaction(new Transaction(3, 200, "Store C", "acc3", System.currentTimeMillis() + 1800000));
        fd.addTransaction(new Transaction(4, 500, "Store A", "acc2", System.currentTimeMillis()));

        System.out.println("Classic Two-Sum (target=500): " + fd.findTwoSum(500));
        System.out.println("Two-Sum with Time Window (target=500): " + fd.findTwoSumWithTime(500));
        System.out.println("K-Sum (k=3, target=1000): " + fd.findKSum(3, 1000));
        System.out.println("Duplicate Detection: " + fd.detectDuplicates());
    }
}
