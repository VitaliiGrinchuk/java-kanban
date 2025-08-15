import static org.junit.jupiter.api.Assertions.*;

import Core.HistoryManager;
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
    void testHistoryManagerStoresPreviousVersion() {
        HistoryManager hm = Managers.getDefaultHistory();
        Task t1 = new Task(1, "a", "d", Status.NEW);
        hm.add(t1);

        Task t2 = new Task(1, "a", "d", Status.DONE);
        hm.add(t2);

        List<Task> history = hm.getHistory();
        assertEquals(Status.NEW, history.get(0).getStatus());
        assertEquals(Status.DONE, history.get(1).getStatus());
    }
}