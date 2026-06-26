import java.util.*;

public class SkipLinkedListTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String testName, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS: " + testName);
        } else {
            failed++;
            System.out.println("  FAIL: " + testName);
        }
    }

    public static void main(String[] args) {
        // Basic functionality tests
        testPutAndGet();
        testGetNonExistent();
        testContains();
        testSize();
        testUpdateExistingKey();
        testRemove();
        testRemoveNonExistent();
        testIteratorOrder();
        testIteratorPrev();
        testLargeDataset();
        testDuplicatePutOverwrite();
        testRemoveHeadAndTail();
        testEmptyListBehavior();
        testNullComparator();
        testStringKeys();

        // Additional tests
        testGetEntry();
        testEmpty();
        testClear();
        testSingleElement();
        testRemoveAllElements();
        testReverseOrderInsertion();
        testIteratorAfterRemove();
        testMixedOperations();
        testGetEntryNonExistent();
        testClearAndReuse();

        // Stress test
        stressTest100K();

        System.out.println("\n=============================");
        System.out.println("Total: " + (passed + failed) + " | Passed: " + passed + " | Failed: " + failed);
        System.out.println("=============================");
    }

    // ==================== Existing Tests ====================

    static void testPutAndGet() {
        System.out.println("\n[testPutAndGet]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "one");
        list.put(3, "three");
        list.put(2, "two");

        check("get existing key 1", "one".equals(list.get(1)));
        check("get existing key 2", "two".equals(list.get(2)));
        check("get existing key 3", "three".equals(list.get(3)));
    }

    static void testGetNonExistent() {
        System.out.println("\n[testGetNonExistent]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "one");

        check("get non-existent key returns null", list.get(99) == null);
        check("get negative key returns null", list.get(-1) == null);
    }

    static void testContains() {
        System.out.println("\n[testContains]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(5, "five");
        list.put(10, "ten");

        check("contains existing key 5", list.contains(5));
        check("contains existing key 10", list.contains(10));
        check("not contains missing key", !list.contains(7));
    }

    static void testSize() {
        System.out.println("\n[testSize]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        check("empty list size is 0", list.size() == 0);

        list.put(1, "a");
        list.put(2, "b");
        list.put(3, "c");
        check("size after 3 inserts", list.size() == 3);

        list.remove(2);
        check("size after 1 remove", list.size() == 2);
    }

    static void testUpdateExistingKey() {
        System.out.println("\n[testUpdateExistingKey]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "one");
        list.put(1, "ONE");

        check("updated value", "ONE".equals(list.get(1)));
        check("size unchanged after update", list.size() == 1);
    }

    static void testRemove() {
        System.out.println("\n[testRemove]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "one");
        list.put(2, "two");
        list.put(3, "three");

        list.remove(2);
        check("removed key not found", list.get(2) == null);
        check("other keys still exist", "one".equals(list.get(1)) && "three".equals(list.get(3)));
        check("size decremented", list.size() == 2);
        check("contains returns false after remove", !list.contains(2));
    }

    static void testRemoveNonExistent() {
        System.out.println("\n[testRemoveNonExistent]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "one");

        list.remove(99);
        check("size unchanged after removing non-existent", list.size() == 1);
        check("existing key still present", "one".equals(list.get(1)));
    }

    static void testIteratorOrder() {
        System.out.println("\n[testIteratorOrder]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(5, "e");
        list.put(1, "a");
        list.put(3, "c");
        list.put(2, "b");
        list.put(4, "d");

        List<Integer> keys = new ArrayList<>();
        for (var entry : list) {
            keys.add(entry.key());
        }
        check("iterator returns keys in sorted order", keys.equals(List.of(1, 2, 3, 4, 5)));
    }

    static void testIteratorPrev() {
        System.out.println("\n[testIteratorPrev]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "a");
        list.put(2, "b");
        list.put(3, "c");

        List<Integer> keys = new ArrayList<>();
        for (var entry : list) {
            keys.add(entry.key());
        }
        check("forward iteration order", keys.equals(List.of(1, 2, 3)));
        check("size is 3", list.size() == 3);
    }

    static void testLargeDataset() {
        System.out.println("\n[testLargeDataset]");
        SkipLinkedList<Integer, Integer> list = new SkipLinkedList<>((a, b) -> a - b);
        int n = 10000;

        for (int i = 0; i < n; i++) {
            list.put(i, i * 10);
        }
        check("size after 10000 inserts", list.size() == n);

        boolean allCorrect = true;
        for (int i = 0; i < n; i++) {
            if (list.get(i) == null || list.get(i) != i * 10) {
                allCorrect = false;
                break;
            }
        }
        check("all 10000 entries retrievable", allCorrect);

        for (int i = 0; i < n; i += 2) {
            list.remove(i);
        }
        check("size after removing 5000", list.size() == n / 2);

        boolean oddsCorrect = true;
        for (int i = 1; i < n; i += 2) {
            if (list.get(i) == null || list.get(i) != i * 10) {
                oddsCorrect = false;
                break;
            }
        }
        check("all odd entries still correct", oddsCorrect);

        boolean evensGone = true;
        for (int i = 0; i < n; i += 2) {
            if (list.get(i) != null) {
                evensGone = false;
                break;
            }
        }
        check("all even entries removed", evensGone);

        List<Integer> keys = new ArrayList<>();
        for (var entry : list) {
            keys.add(entry.key());
        }
        boolean sorted = true;
        for (int i = 1; i < keys.size(); i++) {
            if (keys.get(i) <= keys.get(i - 1)) {
                sorted = false;
                break;
            }
        }
        check("iterator returns remaining keys in sorted order", sorted);
    }

    static void testDuplicatePutOverwrite() {
        System.out.println("\n[testDuplicatePutOverwrite]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "first");
        list.put(1, "second");
        list.put(1, "third");

        check("value is last put", "third".equals(list.get(1)));
        check("size is 1", list.size() == 1);
    }

    static void testRemoveHeadAndTail() {
        System.out.println("\n[testRemoveHeadAndTail]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "a");
        list.put(2, "b");
        list.put(3, "c");

        list.remove(1);
        check("head removed, get(1) is null", list.get(1) == null);
        check("get(2) still works", "b".equals(list.get(2)));

        list.remove(3);
        check("tail removed, get(3) is null", list.get(3) == null);
        check("get(2) still works after tail removed", "b".equals(list.get(2)));
        check("size is 1", list.size() == 1);
    }

    static void testEmptyListBehavior() {
        System.out.println("\n[testEmptyListBehavior]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);

        check("get on empty list returns null", list.get(1) == null);
        check("contains on empty list returns false", !list.contains(1));
        check("size is 0", list.size() == 0);

        var it = list.iterator();
        check("empty list iterator has no next", !it.hasNext());
    }

    static void testNullComparator() {
        System.out.println("\n[testNullComparator]");
        boolean threw = false;
        try {
            new SkipLinkedList<>(null);
        } catch (RuntimeException e) {
            threw = true;
        }
        check("null comparator throws exception", threw);
    }

    static void testStringKeys() {
        System.out.println("\n[testStringKeys]");
        SkipLinkedList<String, Integer> list = new SkipLinkedList<>(String::compareTo);
        list.put("banana", 2);
        list.put("apple", 1);
        list.put("cherry", 3);

        check("string key get", list.get("apple") == 1);
        check("string key get", list.get("cherry") == 3);

        List<String> keys = new ArrayList<>();
        for (var entry : list) {
            keys.add(entry.key());
        }
        check("string keys in sorted order", keys.equals(List.of("apple", "banana", "cherry")));
    }

    // ==================== Additional Tests ====================

    static void testGetEntry() {
        System.out.println("\n[testGetEntry]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(10, "ten");
        list.put(20, "twenty");

        var entry1 = list.getEntry(10);
        check("getEntry returns non-null for existing key", entry1 != null);
        check("getEntry key is correct", entry1 != null && entry1.key() == 10);
        check("getEntry value is correct", entry1 != null && "ten".equals(entry1.value()));

        var entry2 = list.getEntry(99);
        check("getEntry returns null for non-existent key", entry2 == null);
    }

    static void testEmpty() {
        System.out.println("\n[testEmpty]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);

        check("new list is empty", list.empty());

        list.put(1, "one");
        check("not empty after put", !list.empty());

        list.remove(1);
        check("empty after removing all", list.empty());
    }

    static void testClear() {
        System.out.println("\n[testClear]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "one");
        list.put(2, "two");
        list.put(3, "three");

        check("size before clear", list.size() == 3);

        list.clear();

        check("size is 0 after clear", list.size() == 0);
        check("empty after clear", list.empty());
        check("get returns null after clear", list.get(1) == null);
        check("contains returns false after clear", !list.contains(1));

        // Verify list is reusable after clear
        list.put(10, "ten");
        check("can put after clear", "ten".equals(list.get(10)));
        check("size is 1 after re-put", list.size() == 1);
    }

    static void testSingleElement() {
        System.out.println("\n[testSingleElement]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(42, "forty-two");

        check("size is 1", list.size() == 1);
        check("get works", "forty-two".equals(list.get(42)));
        check("contains works", list.contains(42));

        List<Integer> keys = new ArrayList<>();
        for (var entry : list) {
            keys.add(entry.key());
        }
        check("iterator returns single element", keys.equals(List.of(42)));

        list.remove(42);
        check("size is 0 after remove", list.size() == 0);
        check("empty after remove", list.empty());
    }

    static void testRemoveAllElements() {
        System.out.println("\n[testRemoveAllElements]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "one");
        list.put(2, "two");
        list.put(3, "three");
        list.put(4, "four");
        list.put(5, "five");

        // Remove in random order
        list.remove(3);
        list.remove(1);
        list.remove(5);
        list.remove(2);
        list.remove(4);

        check("size is 0 after removing all", list.size() == 0);
        check("empty after removing all", list.empty());
        check("get(1) is null", list.get(1) == null);
        check("get(3) is null", list.get(3) == null);
        check("get(5) is null", list.get(5) == null);

        var it = list.iterator();
        check("iterator has no next", !it.hasNext());
    }

    static void testReverseOrderInsertion() {
        System.out.println("\n[testReverseOrderInsertion]");
        SkipLinkedList<Integer, Integer> list = new SkipLinkedList<>((a, b) -> a - b);

        // Insert in reverse order
        for (int i = 100; i >= 1; i--) {
            list.put(i, i);
        }

        check("size is 100", list.size() == 100);

        // Verify sorted order
        List<Integer> keys = new ArrayList<>();
        for (var entry : list) {
            keys.add(entry.key());
        }

        boolean sorted = true;
        for (int i = 1; i < keys.size(); i++) {
            if (keys.get(i) <= keys.get(i - 1)) {
                sorted = false;
                break;
            }
        }
        check("reverse-inserted keys in sorted order", sorted);
        check("first key is 1", keys.get(0) == 1);
        check("last key is 100", keys.get(99) == 100);
    }

    static void testIteratorAfterRemove() {
        System.out.println("\n[testIteratorAfterRemove]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "one");
        list.put(2, "two");
        list.put(3, "three");
        list.put(4, "four");
        list.put(5, "five");

        list.remove(2);
        list.remove(4);

        List<Integer> keys = new ArrayList<>();
        for (var entry : list) {
            keys.add(entry.key());
        }
        check("iterator skips removed elements", keys.equals(List.of(1, 3, 5)));
    }

    static void testMixedOperations() {
        System.out.println("\n[testMixedOperations]");
        SkipLinkedList<Integer, Integer> list = new SkipLinkedList<>((a, b) -> a - b);

        // Mix of put, get, remove, contains
        list.put(5, 50);
        list.put(3, 30);
        list.put(7, 70);
        list.put(1, 10);
        list.put(9, 90);

        check("get(5) = 50", list.get(5) == 50);
        check("contains(3)", list.contains(3));

        list.remove(3);
        check("get(3) is null after remove", list.get(3) == null);

        list.put(3, 300); // re-insert with different value
        check("get(3) = 300 after re-insert", list.get(3) == 300);

        list.put(5, 500); // update
        check("get(5) = 500 after update", list.get(5) == 500);

        check("size is 5", list.size() == 5);

        List<Integer> keys = new ArrayList<>();
        for (var entry : list) {
            keys.add(entry.key());
        }
        check("keys in sorted order after mixed ops", keys.equals(List.of(1, 3, 5, 7, 9)));
    }

    static void testGetEntryNonExistent() {
        System.out.println("\n[testGetEntryNonExistent]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);
        list.put(1, "one");

        var entry = list.getEntry(999);
        check("getEntry for non-existent key returns null", entry == null);
    }

    static void testClearAndReuse() {
        System.out.println("\n[testClearAndReuse]");
        SkipLinkedList<Integer, String> list = new SkipLinkedList<>((a, b) -> a - b);

        // First usage
        list.put(1, "one");
        list.put(2, "two");
        list.clear();

        // Second usage
        list.put(10, "ten");
        list.put(20, "twenty");
        list.put(30, "thirty");

        check("size is 3 after reuse", list.size() == 3);
        check("get(10) = ten", "ten".equals(list.get(10)));
        check("get(20) = twenty", "twenty".equals(list.get(20)));
        check("get(30) = thirty", "thirty".equals(list.get(30)));

        List<Integer> keys = new ArrayList<>();
        for (var entry : list) {
            keys.add(entry.key());
        }
        check("keys in sorted order after reuse", keys.equals(List.of(10, 20, 30)));
    }

    // ==================== Stress Test ====================

    static void stressTest100K() {
        System.out.println("\n[stressTest100K] - 100,000 entries");
        SkipLinkedList<Integer, Integer> list = new SkipLinkedList<>((a, b) -> a - b);
        int n = 100_000;

        // Phase 1: Insert 100K entries
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            list.put(i, i * 10);
        }
        long insertTime = System.currentTimeMillis() - startTime;
        System.out.println("  Insert " + n + " entries: " + insertTime + "ms");
        check("size after 100K inserts", list.size() == n);

        // Phase 2: Verify all entries
        startTime = System.currentTimeMillis();
        boolean allCorrect = true;
        for (int i = 0; i < n; i++) {
            Integer val = list.get(i);
            if (val == null || val != i * 10) {
                allCorrect = false;
                System.out.println("  ERROR at key " + i + ": expected " + (i * 10) + ", got " + val);
                break;
            }
        }
        long getTime = System.currentTimeMillis() - startTime;
        System.out.println("  Verify 100K entries: " + getTime + "ms");
        check("all 100K entries correct", allCorrect);

        // Phase 3: Update all entries
        startTime = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            list.put(i, i * 100);
        }
        long updateTime = System.currentTimeMillis() - startTime;
        System.out.println("  Update 100K entries: " + updateTime + "ms");
        check("size unchanged after updates", list.size() == n);

        // Phase 4: Verify updates
        startTime = System.currentTimeMillis();
        boolean updatesCorrect = true;
        for (int i = 0; i < n; i++) {
            Integer val = list.get(i);
            if (val == null || val != i * 100) {
                updatesCorrect = false;
                break;
            }
        }
        long verifyUpdateTime = System.currentTimeMillis() - startTime;
        System.out.println("  Verify updates: " + verifyUpdateTime + "ms");
        check("all 100K updates correct", updatesCorrect);

        // Phase 5: Remove half (even numbers)
        startTime = System.currentTimeMillis();
        for (int i = 0; i < n; i += 2) {
            list.remove(i);
        }
        long removeTime = System.currentTimeMillis() - startTime;
        System.out.println("  Remove 50K entries: " + removeTime + "ms");
        check("size after removing 50K", list.size() == n / 2);

        // Phase 6: Verify remaining
        startTime = System.currentTimeMillis();
        boolean remainingCorrect = true;
        for (int i = 1; i < n; i += 2) {
            Integer val = list.get(i);
            if (val == null || val != i * 100) {
                remainingCorrect = false;
                break;
            }
        }
        long verifyRemainingTime = System.currentTimeMillis() - startTime;
        System.out.println("  Verify 50K remaining: " + verifyRemainingTime + "ms");
        check("all 50K remaining correct", remainingCorrect);

        // Phase 7: Verify removed entries are gone
        boolean removedGone = true;
        for (int i = 0; i < n; i += 2) {
            if (list.get(i) != null) {
                removedGone = false;
                break;
            }
        }
        check("all 50K removed entries gone", removedGone);

        // Phase 8: Verify sorted order via iterator
        startTime = System.currentTimeMillis();
        List<Integer> keys = new ArrayList<>();
        int prevKey = -1;
        boolean sorted = true;
        int count = 0;
        for (var entry : list) {
            int key = entry.key();
            if (key <= prevKey) {
                sorted = false;
                break;
            }
            prevKey = key;
            count++;
        }
        long iterTime = System.currentTimeMillis() - startTime;
        System.out.println("  Iterator traversal: " + iterTime + "ms");
        check("iterator order correct after stress", sorted);
        check("iterator count is 50K", count == n / 2);

        // Phase 9: Contains check
        startTime = System.currentTimeMillis();
        boolean containsCorrect = true;
        for (int i = 1; i < n; i += 2) {
            if (!list.contains(i)) {
                containsCorrect = false;
                break;
            }
        }
        long containsTime = System.currentTimeMillis() - startTime;
        System.out.println("  Contains 50K checks: " + containsTime + "ms");
        check("contains works for all remaining", containsCorrect);

        // Phase 10: Clear and verify
        list.clear();
        check("size is 0 after clear", list.size() == 0);
        check("empty after clear", list.empty());

        // Summary
        long totalTime = insertTime + getTime + updateTime + verifyUpdateTime + removeTime + verifyRemainingTime + iterTime + containsTime;
        System.out.println("  Total stress test time: " + totalTime + "ms");
        System.out.println("  Max level reached: " + list.getCurrentMaxLevel());
    }
}
