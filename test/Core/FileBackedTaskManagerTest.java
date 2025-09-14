package Core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import Core.FileBackedTaskManager;
import Core.ManagerSaveException;
import Model.Task;
import Model.Epic;
import Model.Subtask;
import Model.Status;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void saveAndLoadEmptyManager() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void saveAndLoadSeveralTasks() {
        Task task = new Task(0, "Task1", "Description1", Status.NEW);
        Epic epic = new Epic(0, "Epic1", "Epic description");

        int taskId = manager.addNewTask(task);
        int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask(0, "Subtask1", "Sub description", Status.DONE, epicId);
        int subtaskId = manager.addNewSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals(1, loadedManager.getEpics().size());
        assertEquals(1, loadedManager.getSubtasks().size());

        assertEquals("Task1", loadedManager.getTask(taskId).getTitle());
        assertEquals("Epic1", loadedManager.getEpic(epicId).getTitle());
        assertEquals("Subtask1", loadedManager.getSubtask(subtaskId).getTitle());
        assertEquals(Status.DONE, loadedManager.getSubtask(subtaskId).getStatus());
    }

    @Test
    void saveAfterTaskUpdate() {
        Task task = new Task(0, "Original", "Description", Status.NEW);
        int taskId = manager.addNewTask(task);

        Task updatedTask = new Task(taskId, "Updated", "New description", Status.DONE);
        manager.updateTask(updatedTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals("Updated", loadedManager.getTask(taskId).getTitle());
        assertEquals(Status.DONE, loadedManager.getTask(taskId).getStatus());
    }

    @Test
    void saveAfterTaskDeletion() {
        Task task = new Task(0, "Task", "Description", Status.NEW);
        int taskId = manager.addNewTask(task);

        manager.deleteTask(taskId);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
        assertNull(loadedManager.getTask(taskId));
    }
}