import java.util.*;

public class RedoHistoryRecoveryInteractive {
    static class LogEntry {
        int lsn;
        String type;
        String txnId;
        String page;
        String data;
        Integer prevLSN;

        public LogEntry(int lsn, String type, String txnId, String page, String data, Integer prevLSN) {
            this.lsn = lsn;
            this.type = type;
            this.txnId = txnId;
            this.page = page;
            this.data = data;
            this.prevLSN = prevLSN;
        }

        public String toString() {
            return String.format("[%d] %s %s %s %s", lsn, type, txnId != null ? txnId : "", page != null ? page : "", data != null ? data : "");
        }
    }

    static int lsnCounter = 1;
    static List<LogEntry> log = new ArrayList<>();
    static Map<String, String> cache = new HashMap<>();
    static Map<String, String> database = new HashMap<>();
    static Map<String, Integer> pageLSN = new HashMap<>();
    static Map<String, Integer> txnLastLSN = new HashMap<>();
    static Set<String> committed = new HashSet<>();

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("🔄 Enter instructions (type EXIT to finish):");
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("EXIT")) break;
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toUpperCase();

            try {
                switch (cmd) {
                    case "BEGIN":
                        begin(parts[1]);
                        break;
                    case "WRITE":
                        write(parts[1], parts[2], parts[3]);
                        break;
                    case "COMMIT":
                        commit(parts[1]);
                        break;
                    case "FLUSH":
                        flush(parts[1]);
                        break;
                    case "CRASH":
                        crash();
                        break;
                    default:
                        System.out.println("⛔ Unknown command.");
                }
            } catch (Exception e) {
                System.out.println("⚠️ Error in command: " + e.getMessage());
            }
        }
    }

    static void begin(String txnId) {
        log.add(new LogEntry(lsnCounter++, "BEGIN", txnId, null, null, null));
        System.out.println("✅ Transaction " + txnId + " started.");
    }

    static void write(String txnId, String page, String data) {
        cache.put(page, data);
        Integer prev = txnLastLSN.get(txnId);
        LogEntry entry = new LogEntry(lsnCounter++, "WRITE", txnId, page, data, prev);
        log.add(entry);
        txnLastLSN.put(txnId, entry.lsn);
        System.out.println("✍️ WRITE recorded: " + entry);
    }

    static void commit(String txnId) {
        Integer prev = txnLastLSN.get(txnId);
        log.add(new LogEntry(lsnCounter++, "COMMIT", txnId, null, null, prev));
        committed.add(txnId);
        System.out.println("✅ Transaction " + txnId + " committed.");
    }

    static void flush(String page) {
        if (cache.containsKey(page)) {
            database.put(page, cache.get(page));
            pageLSN.put(page, getPageLSN(page));
            System.out.println("💾 Page " + page + " flushed to disk.");
        } else {
            System.out.println("⚠️ Page not found in cache.");
        }
    }

    static void crash() {
        System.out.println("\n🚨 SYSTEM CRASH");
        redoPass();
        undoPass();
        System.out.println("✅ Recovery complete. Current database: " + database + "\n");
    }

    static void redoPass() {
        System.out.println("\n🔁 Redo Pass:");
        for (LogEntry entry : log) {
            if (entry.type.equals("WRITE")) {
                int pageLsn = pageLSN.getOrDefault(entry.page, 0);
                if (pageLsn < entry.lsn) {
                    System.out.println("🔄 Redoing: " + entry);
                    cache.put(entry.page, entry.data);
                    database.put(entry.page, entry.data);
                    pageLSN.put(entry.page, entry.lsn);
                }
            }
        }
    }

    static void undoPass() {
        System.out.println("\n↩️ Undo Pass:");
        Set<String> losers = new HashSet<>();
        for (LogEntry entry : log) {
            if (entry.type.equals("WRITE") && !committed.contains(entry.txnId)) {
                losers.add(entry.txnId);
            }
        }

        for (String txnId : losers) {
            System.out.println("⛔ Undoing transaction " + txnId);
            for (int i = log.size() - 1; i >= 0; i--) {
                LogEntry entry = log.get(i);
                if (entry.txnId.equals(txnId) && entry.type.equals("WRITE")) {
                    System.out.println("🧹 Undo: removing " + entry.page);
                    database.remove(entry.page);
                    pageLSN.remove(entry.page);
                }
            }
        }
    }

    static int getPageLSN(String page) {
        for (int i = log.size() - 1; i >= 0; i--) {
            LogEntry entry = log.get(i);
            if (page.equals(entry.page)) {
                return entry.lsn;
            }
        }
        return 0;
    }
}
