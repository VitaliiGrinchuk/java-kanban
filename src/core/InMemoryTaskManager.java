package core;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int currentId = 1;

    private final HistoryManager historyManager;

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(
            Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())
    ).thenComparing(Task::getId));

    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    private int generateId() {
        return currentId++;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public int addNewTask(Task task) {
        if (hasTimeConflict(task)) {
            throw new TaskTimeConflictException("Задача пересекается по времени с существующей");
        }

        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return -1;

        if (hasTimeConflict(subtask)) {
            throw new TaskTimeConflictException("Подзадача пересекается по времени с существующей");
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        epic.addSubtaskId(id);
        updateEpicStatus(epic.getId());
        updateEpicTime(epic.getId());
        return id;
    }

    @Override
    public void updateTask(Task task) {
        if (hasTimeConflict(task)) {
            throw new TaskTimeConflictException("Обновленная задача пересекается по времени с существующей");
        }

        Task oldTask = tasks.get(task.getId());
        if (oldTask != null) {
            prioritizedTasks.remove(oldTask);
        }
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic != null) {
            prioritizedTasks.remove(oldEpic);

            epic.clearSubtasks();
            for (int subtaskId : oldEpic.getSubtaskIds()) {
                epic.addSubtaskId(subtaskId);
            }
            epic.setStatus(oldEpic.getStatus());
            epics.put(epic.getId(), epic);

            if (epic.getStartTime() != null) {
                prioritizedTasks.add(epic);
            }
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (hasTimeConflict(subtask)) {
            throw new TaskTimeConflictException("Обновленная подзадача пересекается по времени с существующей");
        }

        Subtask oldSubtask = subtasks.get(subtask.getId());
        if (oldSubtask != null) {
            prioritizedTasks.remove(oldSubtask);
        }

        subtasks.put(subtask.getId(), subtask);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        updateEpicStatus(subtask.getEpicId());
        updateEpicTime(subtask.getEpicId());
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            prioritizedTasks.remove(epic);

            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.remove(subtaskId);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }
                historyManager.remove(subtaskId);
            }
        }
        historyManager.remove(id);
    }


    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);

            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
                updateEpicTime(epic.getId());
            }
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteTasks() {
        for (Task task : tasks.values()) {
            prioritizedTasks.remove(task);
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }


    @Override
    public void deleteSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }

        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic.getId());
            updateEpicTime(epic.getId());
        }
    }


    @Override
    public void deleteEpics() {
        for (Epic epic : epics.values()) {
            prioritizedTasks.remove(epic);
            historyManager.remove(epic.getId());
        }

        for (Subtask subtask : subtasks.values()) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(subtask.getId());
        }

        epics.clear();
        subtasks.clear();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;
        for (int id : epic.getSubtaskIds()) {
            Status status = subtasks.get(id).getStatus();
            if (status != Status.DONE) allDone = false;
            if (status != Status.NEW) allNew = false;
        }

        if (allDone) epic.setStatus(Status.DONE);
        else if (allNew) epic.setStatus(Status.NEW);
        else epic.setStatus(Status.INPROGRESS);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicTime(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setCalculatedDuration(null);
            epic.setCalculatedStartTime(null);
            epic.setCalculatedEndTime(null);
            return;
        }

        // Находим самое раннее время начала
        LocalDateTime earliestStart = epicSubtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Находим самое позднее время окончания
        LocalDateTime latestEnd = epicSubtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Суммируем продолжительность всех подзадач
        Duration totalDuration = epicSubtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        epic.setCalculatedStartTime(earliestStart);
        epic.setCalculatedEndTime(latestEnd);
        epic.setCalculatedDuration(totalDuration.isZero() ? null : totalDuration);
    }

    private boolean isTasksOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getEndTime() == null ||
                task2.getStartTime() == null || task2.getEndTime() == null) {
            return false; // Если время не задано, пересечения нет
        }

        // Математический метод наложения отрезков:
        // Отрезки НЕ пересекаются, если один заканчивается до начала другого
        return !(task1.getEndTime().isBefore(task2.getStartTime()) ||
                task2.getEndTime().isBefore(task1.getStartTime()));
    }

    // Проверка пересечения задачи с любой другой в менеджере
    private boolean hasTimeConflict(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) {
            return false; // Если время не задано, конфликта нет
        }

        // Используем Stream API для проверки
        return prioritizedTasks.stream()
                .filter(task -> task.getId() != newTask.getId()) // Исключаем саму задачу
                .anyMatch(task -> isTasksOverlap(newTask, task));
    }

}



