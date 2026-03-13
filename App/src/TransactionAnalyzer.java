import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class Transaction {
    int id;
    double amount;
    String merchant;
    String account;
    LocalDateTime time;

    Transaction(int id, double amount, String merchant, String account, String timeStr) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        int hour = Integer.parseInt(timeStr.split(":")[0]);
        int minute = Integer.parseInt(timeStr.split(":")[1]);
        this.time = LocalDateTime.of(2026, 3, 13, hour, minute);
    }

    @Override
    public String toString() {
        return "id:" + id + " amt:" + amount + " merchant:" + merchant + " acc:" + account + " time:" + time.toLocalTime();
    }
}

public class TransactionAnalyzer {

    List<Transaction> transactions;

    public TransactionAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Classic Two-Sum
    public List<int[]> findTwoSum(double target) {
        Map<Double, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();
        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum with time window (minutes)
    public List<int[]> findTwoSumTimeWindow(double target, int windowMinutes) {
        Map<Double, List<Transaction>> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                for (Transaction candidate : map.get(complement)) {
                    long diff = Math.abs(java.time.Duration.between(t.time, candidate.time).toMinutes());
                    if (diff <= windowMinutes) {
                        result.add(new int[]{candidate.id, t.id});
                    }
                }
            }
            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // K-Sum
    public List<List<Integer>> findKSum(int k, double target) {
        List<List<Integer>> result = new ArrayList<>();
        transactions.sort(Comparator.comparingDouble(t -> t.amount));
        kSumHelper(0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void kSumHelper(int start, int k, double target, List<Integer> path, List<List<Integer>> res) {
        if (k == 2) {
            int left = start, right = transactions.size() - 1;
            while (left < right) {
                double sum = transactions.get(left).amount + transactions.get(right).amount;
                if (Math.abs(sum - target) < 1e-6) {
                    List<Integer> temp = new ArrayList<>(path);
                    temp.add(transactions.get(left).id);
                    temp.add(transactions.get(right).id);
                    res.add(temp);
                    left++;
                    right--;
                } else if (sum < target) left++;
                else right--;
            }
        } else {
            for (int i = start; i < transactions.size() - k + 1; i++) {
                path.add(transactions.get(i).id);
                kSumHelper(i + 1, k - 1, target - transactions.get(i).amount, path, res);
                path.remove(path.size() - 1);
            }
        }
    }

    // Detect duplicates: same amount & merchant, different accounts
    public Map<String, List<String>> detectDuplicates() {
        Map<String, Set<String>> temp = new HashMap<>();
        Map<String, List<String>> dupMap = new HashMap<>();

        for (Transaction t : transactions) {
            String key = t.amount + "|" + t.merchant;
            temp.computeIfAbsent(key, k -> new HashSet<>()).add(t.account);
        }

        for (Map.Entry<String, Set<String>> entry : temp.entrySet()) {
            if (entry.getValue().size() > 1) {
                dupMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
        return dupMap;
    }

    // Demo
    public static void main(String[] args) {
        List<Transaction> txns = Arrays.asList(
                new Transaction(1, 500, "Store A", "acc1", "10:00"),
                new Transaction(2, 300, "Store B", "acc2", "10:15"),
                new Transaction(3, 200, "Store C", "acc3", "10:30"),
                new Transaction(4, 500, "Store A", "acc2", "11:00")
        );

        TransactionAnalyzer analyzer = new TransactionAnalyzer(txns);

        System.out.println("Classic Two-Sum 500:");
        for (int[] pair : analyzer.findTwoSum(500)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nTwo-Sum within 60 mins:");
        for (int[] pair : analyzer.findTwoSumTimeWindow(500, 60)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nK-Sum k=3, target=1000:");
        for (List<Integer> combo : analyzer.findKSum(3, 1000)) {
            System.out.println(combo);
        }

        System.out.println("\nDuplicate detection:");
        for (Map.Entry<String, List<String>> entry : analyzer.detectDuplicates().entrySet()) {
            System.out.println(entry.getKey() + " -> accounts: " + entry.getValue());
        }
    }
}