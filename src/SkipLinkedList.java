import java.util.Comparator;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class SkipLinkedList<K, V> implements Iterable<SkipLinkedList.Entry<K, V>> {

    public static class Entry<K, V> {
        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }

    private static class SkipLinkedListNode<K, V> {
        public K key;
        public V value;
        public SkipLinkedListNode<K, V>[] levelRefList;
        public SkipLinkedListNode<K, V> prevNode;

        @SuppressWarnings("unchecked")
        public SkipLinkedListNode(K key, V value, int level) {
            this.key = key;
            this.value = value;
            this.levelRefList = (SkipLinkedListNode<K, V>[]) new SkipLinkedListNode[level];
        }
    }

    @Override
    public Iterator<SkipLinkedList.Entry<K, V>> iterator() {
        return new Iterator<SkipLinkedList.Entry<K, V>>() {

            private SkipLinkedListNode<K, V> current = headNode.levelRefList[0];

            @Override
            public boolean hasNext() {
                return current != tailNode;
            }

            @Override
            public SkipLinkedList.Entry<K, V> next() {
                Entry<K, V> entry = new Entry<>(current.key, current.value);
                current = current.levelRefList[0];
                return entry;
            }

            public boolean hasPrev() {
                return current != headNode;
            }

            public SkipLinkedList.Entry<K, V> prev() {
                Entry<K, V> entry = new Entry<>(current.key, current.value);
                current = current.prevNode;
                return entry;
            }
        };
    }

    private SkipLinkedListNode<K, V> headNode;
    private SkipLinkedListNode<K, V> tailNode;
    private int currentMaxLevel = 1;
    private int nodeCount = 0;
    private Comparator<K> cmp;

    private static int MAX_LEVEL_LIMIT = 32;

    public SkipLinkedList(Comparator<K> cmp) {
        if (cmp == null)
            throw new RuntimeException("The comparator is null.");
        this.cmp = cmp;
    }

    public void put(K key, V value) {
        if (headNode == null || tailNode == null)
            initHeadAndTailNode();
        insertAction(key, value);
    }

    public V get(K key) {
        var entry = searchAction(key);
        return entry == null ? null : entry.getValue();
    }

    public Entry<K, V> getEntry(K key) {
        return searchAction(key);
    }

    public void remove(K key) {
        removeAction(key);
    }

    public boolean contains(K key) {
        return searchAction(key) != null;
    }

    public int size() {
        return nodeCount;
    }

    public int getCurrentMaxLevel() {
        return currentMaxLevel;
    }

    private void initHeadAndTailNode() {
        headNode = new SkipLinkedListNode<K, V>(null, null, MAX_LEVEL_LIMIT);
        tailNode = new SkipLinkedListNode<K, V>(null, null, 1);
        for (int i = 0; i < MAX_LEVEL_LIMIT; ++i)
            headNode.levelRefList[i] = tailNode;
        tailNode.prevNode = headNode;
    }

    private void insertAction(K key, V value) {
        int newLevel = generateRandomLevel();
        int maxLevel = currentMaxLevel < newLevel ? newLevel : currentMaxLevel;
        SkipLinkedListNode<K, V> cursor = headNode;
        Stack<SkipLinkedListNode<K, V>> needUpdateStack = new Stack<>();
        for (int i = maxLevel - 1; i >= 0; --i) {
            SkipLinkedListNode<K, V> nextNode = cursor.levelRefList[i];
            while (nextNode != tailNode && cmp.compare(key, nextNode.key) > 0) {
                cursor = nextNode;
                nextNode = cursor.levelRefList[i];
            }
            if (nextNode != tailNode && cmp.compare(key, nextNode.key) == 0) {
                nextNode.value = value;
                return;
            }
            if (i <= newLevel - 1)
                needUpdateStack.push(cursor);
        }
        SkipLinkedListNode<K, V> newNode = new SkipLinkedListNode<K, V>(key, value, newLevel);
        int countVar = 0;
        while (!needUpdateStack.empty()) {
            SkipLinkedListNode<K, V> node = needUpdateStack.pop();
            SkipLinkedListNode<K, V> nextNode = node.levelRefList[countVar];
            newNode.levelRefList[countVar] = nextNode;
            node.levelRefList[countVar] = newNode;
            if (countVar == 0) {
                newNode.prevNode = node;
                nextNode.prevNode = newNode;
            }
            ++countVar;
        }
        currentMaxLevel = maxLevel;
        ++nodeCount;
    }

    private Entry<K, V> searchAction(K key) {
        SkipLinkedListNode<K, V> cursor = headNode;
        for (int i = currentMaxLevel - 1; i >= 0; --i) {
            SkipLinkedListNode<K, V> nextNode = cursor.levelRefList[i];
            while (nextNode != tailNode && cmp.compare(key, nextNode.key) > 0) {
                cursor = nextNode;
                nextNode = cursor.levelRefList[i];
            }
            if (nextNode != tailNode && cmp.compare(key, nextNode.key) == 0)
                return new Entry<>(key, nextNode.value);
        }
        return null;
    }

    private void removeAction(K key) {
        SkipLinkedListNode<K, V> cursor = headNode;
        Stack<SkipLinkedListNode<K, V>> needUpdateStack = new Stack<>();
        for (int i = currentMaxLevel - 1; i >= 0; --i) {
            SkipLinkedListNode<K, V> nextNode = cursor.levelRefList[i];
            while (nextNode != tailNode && cmp.compare(key, nextNode.key) > 0) {
                cursor = nextNode;
                nextNode = cursor.levelRefList[i];
            }
            if (nextNode != tailNode && cmp.compare(key, nextNode.key) == 0)
                needUpdateStack.push(cursor);
        }
        int countVar = 0;
        while (!needUpdateStack.empty()) {
            SkipLinkedListNode<K, V> node = needUpdateStack.pop();
            SkipLinkedListNode<K, V> nextNode = node.levelRefList[countVar];
            SkipLinkedListNode<K, V> nextNextNode = nextNode.levelRefList[countVar];
            node.levelRefList[countVar] = nextNextNode;
            if (countVar == 0 && nextNextNode != null)
                nextNextNode.prevNode = node;
            nextNode.levelRefList[countVar] = null;
            if (countVar == 0)
                nextNode.prevNode = null;
            ++countVar;
        }
        if (countVar == 0)
            return;
        --nodeCount;
        currentMaxLevel = getLastMaxLevel();
    }

    private static int generateRandomLevel() {
        int level = 1;
        while (ThreadLocalRandom.current().nextInt(4) == 0 && level < MAX_LEVEL_LIMIT)
            level += 1;
        return level;
    }

    private int getLastMaxLevel() {
        if (currentMaxLevel == 1)
            return 1;
        int lastMaxLevel = currentMaxLevel;
        while (lastMaxLevel > 1 && headNode.levelRefList[lastMaxLevel - 1] == tailNode)
            --lastMaxLevel;
        return lastMaxLevel;
    }
}