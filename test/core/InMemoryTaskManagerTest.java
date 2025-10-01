package core;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    @Test
    void addTaskWithTimeConflictThrowsException() {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task(0, "Task1", "Desc1", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60)); // 10:00-11:00

        Task task2 = new Task(0, "Task2", "Desc2", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));
        task2.setDuration(Duration.ofMinutes(60)); // 10:30-11:30 - пересекается!

        manager.addNewTask(task1);

        assertThrows(TaskTimeConflictException.class, () -> {
            manager.addNewTask(task2);
        });
    }

    @Test
    void epicTimeCalculatedFromSubtasks() {
        TaskManager manager = Managers.getDefault();

        Epic epic = new Epic(0, "Epic", "Epic desc");
        int epicId = manager.addNewEpic(epic);

        Subtask sub1 = new Subtask(0, "Sub1", "Desc1", Status.NEW, epicId);
        sub1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        sub1.setDuration(Duration.ofMinutes(60));

        Subtask sub2 = new Subtask(0, "Sub2", "Desc2", Status.NEW, epicId);
        sub2.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        sub2.setDuration(Duration.ofMinutes(30));

        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        Epic updatedEpic = manager.getEpic(epicId);

        // Время начала эпика = время начала самой ранней подзадачи
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), updatedEpic.getStartTime());

        // Время окончания эпика = время окончания самой поздней подзадачи
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 30), updatedEpic.getEndTime());

        // Продолжительность = сумма всех подзадач
        assertEquals(Duration.ofMinutes(90), updatedEpic.getDuration());
    }

    @Test
    void getPrioritizedTasksReturnsTasksSortedByStartTime() {
        TaskManager manager = Managers.getDefault();

        // Создаем задачи с разным временем начала
        Task task1 = new Task(0, "Task1", "Desc1", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));

        Task task2 = new Task(0, "Task2", "Desc2", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 8, 0));
        task2.setDuration(Duration.ofMinutes(30));

        Task task3 = new Task(0, "Task3", "Desc3", Status.NEW);
        // Без времени - не должна попасть в список

        manager.addNewTask(task1);
        manager.addNewTask(task2);
        manager.addNewTask(task3);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(2, prioritized.size());
        assertEquals("Task2", prioritized.get(0).getTitle()); // Раньше по времени
        assertEquals("Task1", prioritized.get(1).getTitle());
    }

}