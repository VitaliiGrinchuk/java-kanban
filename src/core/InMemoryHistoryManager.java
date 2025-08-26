package core;
import model.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    // Узел двусвязного списка
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }

    private final Map<Integer, Node> nodeMap = new HashMap<>(); // id -> Node
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) return;
        int id = task.getId();
        // Если задача есть — удалить старую из списка
        if (nodeMap.containsKey(id)) {
            removeNode(nodeMap.get(id));
        }
        // Вставить в конец списка
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.get(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    // Вспомогательные методы:

    private void linkLast(Task task) {
        Node oldTail = tail;
        Node newNode = new Node(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        nodeMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node == null) return;
        Node prev = node.prev;
        Node next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }
        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }
        nodeMap.remove(node.task.getId());
    }
}
