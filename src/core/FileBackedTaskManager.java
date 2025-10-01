package core;
import model.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        if (file.exists() && file.length() > 0) {
            try {
                String content = Files.readString(file.toPath());
                String[] lines = content.split("\n");


                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (!line.isEmpty()) {
                        Task task = manager.fromString(line);

                        if (task instanceof Epic) {
                            manager.addNewEpic((Epic) task);
                        } else if (task instanceof Subtask) {
                            manager.addNewSubtask((Subtask) task);
                        } else {
                            manager.addNewTask(task);
                        }
                    }
                }
            } catch (IOException e) {
                throw new ManagerSaveException("Не удалось загрузить из файла", e);
            }
        }
        return manager;
    }


    private void save() {
        try {
            List<String> lines = new ArrayList<>();
            lines.add("id,type,name,status,description,epic,duration,startTime"); // ОБНОВЛЕННЫЙ заголовок

            for (Task task : getTasks()) {
                lines.add(toString(task));
            }

            for (Epic epic : getEpics()) {
                lines.add(toString(epic));
            }

            for (Subtask subtask : getSubtasks()) {
                lines.add(toString(subtask));
            }

            Files.write(file.toPath(), lines);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить в файл", e);
        }
    }

    private String toString(Task task) {
        TaskType type = getTaskType(task);
        String epicId = "";
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        String durationStr = "";
        if (task.getDuration() != null) {
            durationStr = String.valueOf(task.getDuration().toMinutes());
        }

        String startTimeStr = "";
        if (task.getStartTime() != null) {
            startTimeStr = task.getStartTime().toString();
        }

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId,
                durationStr,
                startTimeStr);
    }


    private TaskType getTaskType(Task task) {
        if (task instanceof Epic) {
            return TaskType.EPIC;
        } else if (task instanceof Subtask) {
            return TaskType.SUBTASK;
        } else {
            return TaskType.TASK;
        }
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        String epicIdStr = (parts.length > 5) ? parts[5] : "";
        Duration duration = null;
        if (parts.length > 6 && !parts[6].isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(parts[6]));
        }
        LocalDateTime startTime = null;
        if (parts.length > 7 && !parts[7].isEmpty()) {
            startTime = LocalDateTime.parse(parts[7]);
        }

        switch (type) {
            case TASK:
                Task task = new Task(id, name, description, status);
                if (duration != null) task.setDuration(duration);
                if (startTime != null) task.setStartTime(startTime);
                return task;
            case EPIC:
                Epic epic = new Epic(id, name, description);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                if (epicIdStr.isEmpty()) {
                    throw new IllegalArgumentException("Subtask должна иметь epicId");
                }
                int epicId = Integer.parseInt(epicIdStr);
                Subtask subtask = new Subtask(id, name, description, status, epicId);
                if (duration != null) subtask.setDuration(duration);
                if (startTime != null) subtask.setStartTime(startTime);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}
