import java.util.*;

public class TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int currentId = 1;
    private final Scanner scanner = new Scanner(System.in);

    private int generateId() {
        return currentId++;
    }

    public void start() {
        while (true) {
            printMenu();
            int command = Integer.parseInt(scanner.nextLine());
            switch (command) {
                case 1: printAllTasks(); break;
                case 2: removeAllTasks(); break;
                case 3: getTaskById(); break;
                case 4: createTask(); break;
                case 5: updateTask(); break;
                case 6: removeTaskById(); break;
                case 7: getSubtasksByEpic(); break;
                case 8: return;
                default: System.out.println("Неверная команда.");
            }
        }
    }

    private void printMenu() {
        System.out.println("Выберите действие:");
        System.out.println("1 - Получить список всех задач");
        System.out.println("2 - Удалить все задачи");
        System.out.println("3 - Получить задачу по ID");
        System.out.println("4 - Создать задачу/эпик/подзадачу");
        System.out.println("5 - Обновить задачу по ID");
        System.out.println("6 - Удалить задачу по ID");
        System.out.println("7 - Получить подзадачи по ID эпика");
        System.out.println("8 - Выход");
    }

    private void printAllTasks() {
        System.out.println("Задачи: " + tasks.values());
        System.out.println("Эпики: " + epics.values());
        System.out.println("Подзадачи: " + subtasks.values());
    }

    private void removeAllTasks() {
        tasks.clear();
        epics.clear();
        subtasks.clear();
        System.out.println("Все задачи удалены.");
    }

    private void getTaskById() {
        System.out.println("Введите ID задачи:");
        int id = Integer.parseInt(scanner.nextLine());

        if (tasks.containsKey(id)) {
            System.out.println(tasks.get(id));
        } else if (epics.containsKey(id)) {
            System.out.println(epics.get(id));
        } else if (subtasks.containsKey(id)) {
            System.out.println(subtasks.get(id));
        } else {
            System.out.println("Задача не найдена.");
        }
    }

    private void createTask() {
        System.out.println("Тип задачи (1-обычная, 2-эпик, 3-подзадача):");
        int type = Integer.parseInt(scanner.nextLine());

        System.out.println("Введите название:");
        String title = scanner.nextLine();
        System.out.println("Введите описание:");
        String desc = scanner.nextLine();

        Status status = Status.NEW;
        if (type != 2) {
            System.out.println("Введите статус (NEW, INPROGRESS, DONE):");
            status = Status.valueOf(scanner.nextLine());
        }

        switch (type) {
            case 1:
                Task task = new Task(generateId(), title, desc, status);
                tasks.put(task.getId(), task);
                System.out.println("Задача создана: " + task);
                break;
            case 2:
                Epic epic = new Epic(generateId(), title, desc);
                epics.put(epic.getId(), epic);
                System.out.println("Эпик создан: " + epic);
                break;
            case 3:
                System.out.println("Введите ID эпика:");
                int epicId = Integer.parseInt(scanner.nextLine());
                if (!epics.containsKey(epicId)) {
                    System.out.println("Эпик не найден.");
                    return;
                }
                Subtask subtask = new Subtask(generateId(), title, desc, status, epicId);
                subtasks.put(subtask.getId(), subtask);
                epics.get(epicId).addSubtaskId(subtask.getId());
                updateEpicStatus(epicId);
                System.out.println("Подзадача создана: " + subtask);
                break;
            default:
                System.out.println("Неверный тип задачи.");
        }
    }

    private void updateTask() {
        System.out.println("Введите ID задачи для обновления:");
        int id = Integer.parseInt(scanner.nextLine());
        if (tasks.containsKey(id)) {
            System.out.println("Введите новое название и описание:");
            String title = scanner.nextLine();
            String desc = scanner.nextLine();
            System.out.println("Введите новый статус (NEW, IN_PROGRESS, DONE):");
            Status status = Status.valueOf(scanner.nextLine());
            Task task = new Task(id, title, desc, status);
            tasks.put(id, task);
            System.out.println("Задача обновлена." + task);
        } else if (epics.containsKey(id)) {
            System.out.println("Введите новое название и описание эпика:");
            String title = scanner.nextLine();
            String desc = scanner.nextLine();
            Epic epic = epics.get(id);
            epic.title = title;
            epic.description = desc;
            System.out.println("Эпик обновлён." + epic);
        } else if (subtasks.containsKey(id)) {
            System.out.println("Введите новое название и описание подзадачи:");
            String title = scanner.nextLine();
            String desc = scanner.nextLine();
            System.out.println("Введите новый статус (NEW, IN_PROGRESS, DONE):");
            Status status = Status.valueOf(scanner.nextLine());
            Subtask subtask = subtasks.get(id);
            subtask.title = title;
            subtask.description = desc;
            subtask.setStatus(status);
            updateEpicStatus(subtask.getEpicId());
            System.out.println("Подзадача обновлена." + subtask);
        } else {
            System.out.println("Задача не найдена.");
        }
    }

    private void removeTaskById() {
        System.out.println("Введите ID задачи для удаления:");
        int id = Integer.parseInt(scanner.nextLine());
        if (tasks.remove(id) != null || subtasks.remove(id) != null) {
            System.out.println("Удалено.");
        } else if (epics.containsKey(id)) {
            Epic epic = epics.remove(id);
            for (int subId : epic.getSubtaskIds()) subtasks.remove(subId);
            System.out.println("Эпик и его подзадачи удалены.");
        } else {
            System.out.println("Задача не найдена.");
        }
    }

    private void getSubtasksByEpic() {
        System.out.println("Введите ID эпика:");
        int epicId = Integer.parseInt(scanner.nextLine());
        if (!epics.containsKey(epicId)) {
            System.out.println("Эпик не найден.");
            return;
        }
        for (int id : epics.get(epicId).getSubtaskIds()) {
            System.out.println(subtasks.get(id));
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        boolean allDone = true, allNew = true;
        for (int id : epic.getSubtaskIds()) {
            Subtask s = subtasks.get(id);
            if (s.getStatus() != Status.DONE) allDone = false;
            if (s.getStatus() != Status.NEW) allNew = false;
        }
        epic.setStatus(allDone ? Status.DONE : (allNew ? Status.NEW : Status.INPROGRESS));
    }
}
