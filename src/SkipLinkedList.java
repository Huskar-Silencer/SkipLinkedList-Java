import java.util.Comparator;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class SkipLinkedList<K, V> {

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

    public static class SkipLinkedListIterator<K, V> {
        private SkipLinkedListNode<K, V> currentNode;
        private SkipLinkedListNode<K, V> headNode;
        private SkipLinkedListNode<K, V> tailNode;

        public SkipLinkedListIterator(SkipLinkedListNode<K, V> headNode, SkipLinkedListNode<K, V> tailNode) {
            this.headNode = headNode;
            this.tailNode = tailNode;
            this.currentNode = headNode;
        }

        public void next() {
            if (!hasNext())
                return;
            currentNode = currentNode.levelRefList[0];
        }

        public void prev() {
            if (!hasPrev())
                return;
            currentNode = currentNode.prevNode;
        }

        public boolean hasNext() {
            return currentNode.levelRefList[0] != tailNode;
        }

        public boolean hasPrev() {
            return currentNode != headNode;
        }

        public K getKey() {
            return currentNode.key;
        }

        public V getValue() {
            return currentNode.value;
        }
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

    public V getValue(K key) {
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

    public SkipLinkedListIterator<K, V> iterator() {
        return new SkipLinkedListIterator<>(headNode, tailNode);
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
            while (nextNode != tailNode && cmp.compare(key, nextNode.key) < 0) {
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
            newNode.levelRefList[countVar] = node.levelRefList[countVar];
            node.levelRefList[countVar] = newNode;
            if (countVar == 0) {
                newNode.prevNode = node;
                newNode.levelRefList[0].prevNode = newNode;
            }
            ++countVar;
        }
        currentMaxLevel = maxLevel;
        ++nodeCount;
    }

    private Entry<K, V> searchAction(K key) {
        SkipLinkedListNode<K, V> cursor = headNode;
        for (int i = currentMaxLevel; i >= 1; --i) {
            SkipLinkedListNode<K, V> nextNode = cursor.levelRefList[i - 1];
            while (nextNode != tailNode && cmp.compare(key, nextNode.key) < 0) {
                cursor = nextNode;
                nextNode = cursor.levelRefList[i - 1];
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
            while (nextNode != tailNode && cmp.compare(key, nextNode.key) < 0) {
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
        currentMaxLevel = getLastLevel();
    }

    private static int generateRandomLevel() {
        int level = 1;
        while (ThreadLocalRandom.current().nextInt(16) < 4 && level < MAX_LEVEL_LIMIT)
            level += 1;
        return level;
    }

    private int getLastLevel() {
        if (currentMaxLevel == 1)
            return 1;
        int lastLevel = currentMaxLevel;
        while (headNode.levelRefList[lastLevel - 1] == tailNode)
            --lastLevel;
        return lastLevel;
    }
}