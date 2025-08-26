import static org.junit.jupiter.api.Assertions.*;

import Core.HistoryManager;
import Core.InMemoryHistoryManager;
import Core.Managers;
import Core.TaskManager;
import Model.Epic;
import Model.Status;
import Model.Subtask;
import Model.Task;
import org.junit.jupiter.api.Test;
import java.util.List;

class EpicTest {
    @Test
    void testTaskEqualsById() {
        Task t1 = new Task(1, "a", "d", Status.NEW);
        Task t2 = new Task(1, "b", "e", Status.DONE);
        assertEquals(t1, t2, "Tasks with same id should be equal");
    }
    @Test
    void testEpicCantBeItsOwnSubtask() {
        Epic epic = new Epic(1, "Model.Epic", "desc");
        epic.addSubtaskId(1);
        assertFalse(epic.getSubtaskIds().contains(epic.getId()), "Model.Epic can't be its own subtask");
    }
    @Test
    void testSubtaskCantBeItsOwnEpic() {
        Subtask subtask = new Subtask(2, "sub", "desc", Status.NEW, 1);
        assertNotEquals(subtask.getId(), subtask.getEpicId(), "Model.Subtask can't be its own epic");
    }
    @Test
    void testManagersGetDefault() {
        TaskManager tm = Managers.getDefault();
        assertNotNull(tm, "Core.Managers.getDefault() returned null");
    }
    @Test
    void testAddAndFindTaskById() {
        TaskManager tm = Managers.getDefault();
        Task task = new Task(0, "t", "d", Status.NEW);
        int id = tm.addNewTask(task);
        assertEquals(task, tm.getTask(id));
    }
    @Test
    void testNoIdConflicts() {
        TaskManager tm = Managers.getDefault();
        Task t1 = new Task(100, "a", "d", Status.NEW);
        int id1 = tm.addNewTask(t1);
        Task t2 = new Task(1, "Test name", "Test desc", Status.NEW);
        int id2 = tm.addNewTask(t2);
        assertNotEquals(id1, id2, "IDs must be unique even with manual id");
    }
    @Test
    void testTaskIsNotChangedInManager() {
        TaskManager tm = Managers.getDefault();
        Task t1 = new Task(1, "Test name", "Test desc", Status.NEW);
        int id = tm.addNewTask(t1);
        Task fromManager = tm.getTask(id);
        assertEquals(t1.getTitle(), fromManager.getTitle());
        assertEquals(t1.getDescription(), fromManager.getDescription());
        assertEquals(t1.getStatus(), fromManager.getStatus());
    }
    @Test
    void addTaskWithSameIdReplacesPreviousInHistory() {
        HistoryManager hm = Managers.getDefaultHistory();
        Task t1 = new Task(1, "a", "d", Status.NEW);
        hm.add(t1);

        Task t2 = new Task(1, "a", "d", Status.DONE);
        hm.add(t2);

        List<Task> history = hm.getHistory();
        assertEquals(1, history.size());
        assertEquals(Status.DONE, history.getFirst().getStatus());
    }
    @Test
    void addTaskTwice_shouldMoveToHistoryEndWithoutDuplication() {
        HistoryManager history = new InMemoryHistoryManager();
        Task t1 = new Task(1, "t1", "d", Status.NEW);
        Task t2 = new Task(2, "t2", "d", Status.NEW);
        history.add(t1);
        history.add(t2);
        history.add(t1);

        List<Task> hist = history.getHistory();

        assertEquals(2, hist.size());
        assertEquals(t2, hist.get(0));
        assertEquals(t1, hist.get(1));
    }
    @Test
    void historyCanBeLongerThanTen() {
        HistoryManager history = new InMemoryHistoryManager();
        for (int i = 1; i <= 20; i++) {
            history.add(new Task(i, "t"+i, "d", Status.NEW));
        }
        assertEquals(20, history.getHistory().size());
    }
    @Test
    void removeTaskFromHistory() {
        HistoryManager history = new InMemoryHistoryManager();
        Task t1 = new Task(1, "t1", "d", Status.NEW);
        Task t2 = new Task(2, "t2", "d", Status.NEW);
        history.add(t1);
        history.add(t2);

        history.remove(1);

        List<Task> hist = history.getHistory();
        assertEquals(1, hist.size());
        assertFalse(hist.contains(t1));
        assertTrue(hist.contains(t2));
    }
    @Test
    void removeLastTaskWithDuplicates() {
        HistoryManager history = new InMemoryHistoryManager();
        Task t1 = new Task(1, "t1", "d", Status.NEW);
        Task t2 = new Task(2, "t2", "d", Status.NEW);
        history.add(t1);
        history.add(t2);
        history.add(t1);

        history.remove(1);

        List<Task> hist = history.getHistory();
        assertEquals(1, hist.size());
        assertFalse(hist.contains(t1));
        assertTrue(hist.contains(t2));
    }
    @Test
    void historyOrderIsCorrect() {
        HistoryManager history = new InMemoryHistoryManager();
        Task t1 = new Task(1, "t1", "d", Status.NEW);
        Task t2 = new Task(2, "t2", "d", Status.NEW);
        Task t3 = new Task(3, "t3", "d", Status.NEW);
        history.add(t1);
        history.add(t2);
        history.add(t3);

        List<Task> hist = history.getHistory();
        assertEquals(List.of(t1, t2, t3), hist);

        history.add(t2);
        hist = history.getHistory();
        assertEquals(List.of(t1, t3, t2), hist);
    }
    @Test
    void deletingSubtaskRemovesIdFromEpic() {
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic(0, "Epic", "desc");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask = new Subtask(0, "Sub", "desc", Status.NEW, epicId);
        int subId = manager.addNewSubtask(subtask);

        manager.deleteSubtask(subId);

        Epic updatedEpic = manager.getEpic(epicId);
        assertFalse(updatedEpic.getSubtaskIds().contains(subId), "Epic must not contain deleted subtask id");
    }
    @Test
    void deletingEpicRemovesItsSubtasks() {
        TaskManager manager = Managers.getDefault();
        Epic epic = new Epic(0, "Epic", "desc");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask = new Subtask(0, "Sub", "desc", Status.NEW, epicId);
        int subId = manager.addNewSubtask(subtask);

        manager.deleteEpic(epicId);

        assertNull(manager.getSubtask(subId), "Subtask of deleted epic must also be deleted");
    }
    @Test
    void removeTaskAlsoRemovesItFromHistory() {
        TaskManager manager = Managers.getDefault();
        Task task = new Task(0, "Task", "desc", Status.NEW);
        int id = manager.addNewTask(task);

        manager.getTask(id); // добавляем в историю
        manager.deleteTask(id);

        assertFalse(manager.getHistory().contains(task), "Deleted task must be removed from history");
    }
    @Test
    void repeatedViewMovesTaskToEndOfHistoryWithoutDuplication() {
        HistoryManager history = Managers.getDefaultHistory();
        Task t1 = new Task(1, "t1", "d", Status.NEW);
        Task t2 = new Task(2, "t2", "d", Status.NEW);
        history.add(t1);
        history.add(t2);
        history.add(t1);

        List<Task> hist = history.getHistory();
        assertEquals(2, hist.size());
        assertEquals(t2, hist.get(0));
        assertEquals(t1, hist.get(1));
    }
    @Test
    void settersChangeFieldsAndManagerReflectsChanges() {
        TaskManager manager = Managers.getDefault();
        Task task = new Task(0, "Title", "Desc", Status.NEW);
        int id = manager.addNewTask(task);

        Task fromManager = manager.getTask(id);
        fromManager.setTitle("NewTitle");
        fromManager.setDescription("NewDesc");
        fromManager.setStatus(Status.DONE);

        Task again = manager.getTask(id);
        assertEquals("NewTitle", again.getTitle());
        assertEquals("NewDesc", again.getDescription());
        assertEquals(Status.DONE, again.getStatus());
    }
    @Test
    void epicCannotBeItsOwnSubtask() {
        Epic epic = new Epic(1, "Epic", "desc");
        epic.addSubtaskId(1);
        assertFalse(epic.getSubtaskIds().contains(epic.getId()), "Epic can't be its own subtask");
    }
    @Test
    void cannotCreateSubtaskWithItsOwnEpicId() {
        assertThrows(IllegalArgumentException.class, () -> new Subtask(1, "Sub", "Desc", Status.NEW, 1));
    }
}