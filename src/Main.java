import java.util.Iterator;

public class Main {
    public static void main(String[] args) {
        SkipLinkedList<Integer, String> skipLinkedList = new SkipLinkedList<>((a, b) -> a - b);
        skipLinkedList.put(1, "one");
        skipLinkedList.put(2, "two");
        skipLinkedList.put(3, "three");
        skipLinkedList.put(4, "four");
        skipLinkedList.put(5, "five");

        System.out.println(skipLinkedList.get(3)); // Output: three
        System.out.println(skipLinkedList.get(6)); // Output: null
        System.out.println(skipLinkedList.get(2)); // Output: two
        skipLinkedList.remove(2);
        System.out.println(skipLinkedList.get(2)); // Output: null

        Iterator<SkipLinkedList.Entry<Integer, String>> iterator = skipLinkedList.iterator();
        while (iterator.hasNext()) {
            var res = iterator.next();
            System.out.println(res.getKey() + ": " + res.getValue());
        }

        System.out.println(skipLinkedList.getCurrentMaxLevel()); // Output: 3
    }
}
