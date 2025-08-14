import java.util.Scanner;

public class Main {
    private static final TaskManager manager = Managers.getDefault();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Поехали!");
        while (true) {
            printMenu();
            int input = Integer.parseInt(scanner.nextLine());
            switch (input) {
                case 1:
                    System.out.println(manager.getTasks());
                    break;
                case 2:
                    System.out.println(manager.getEpics());
                    break;
                case 3:
                    System.out.println(manager.getSubtasks());
                    break;
                case 4:
                    getTaskById();
                    break;
                case 5:
                    addTask();
                    break;
                case 6:
                    updateTask();
                    break;
                case 7:
                    deleteTaskById();
                    break;
                case 8:
                    deleteAllTasks();
                    break;
                case 9:
                    getEpicSubtasks();
                    break;
                case 0:
                    System.out.println("Выход");
                    return;
                default:
                    System.out.println("Неверная команда");
            }
        }
    }

    private static void printMenu() {
        System.out.println("1 - Все задачи");
        System.out.println("2 - Все эпики");
        System.out.println("3 - Все подзадачи");
        System.out.println("4 - Получить задачу по ID");
        System.out.println("5 - Создать задачу");
        System.out.println("6 - Обновить задачу");
        System.out.println("7 - Удалить задачу по ID");
        System.out.println("8 - Удалить все задачи");
        System.out.println("9 - Подзадачи эпика");
        System.out.println("0 - Выход");
    }

    private static void getTaskById() {
        System.out.println("Введите ID задачи:");
        int id = Integer.parseInt(scanner.nextLine());
        Task task = manager.getTask(id);
        if (task == null) task = manager.getEpic(id);
        if (task == null) task = manager.getSubtask(id);
        System.out.println(task != null ? task : "Задача не найдена");
    }

    private static void addTask() {
        System.out.println("Тип (1-Task, 2-Epic, 3-Subtask):");
        int type = Integer.parseInt(scanner.nextLine());
        System.out.println("Название:");
        String title = scanner.nextLine();
        System.out.println("Описание:");
        String desc = scanner.nextLine();
        Status status = Status.NEW;
        if (type != 2) {
            System.out.println("Статус (NEW, INPROGRESS, DONE):");
            status = Status.valueOf(scanner.nextLine());
        }
        switch (type) {
            case 1:
                Task task = new Task(0, title, desc, status);
                System.out.println("Создана задача id=" + manager.addNewTask(task));
                break;
            case 2:
                Epic epic = new Epic(0, title, desc);
                System.out.println("Создан эпик id=" + manager.addNewEpic(epic));
                break;
            case 3:
                System.out.println("Введите ID эпика:");
                int epicId = Integer.parseInt(scanner.nextLine());
                if (manager.getEpic(epicId) == null) {
                    System.out.println("Эпик не найден");
                    return;
                }
                Subtask subtask = new Subtask(0, title, desc, status, epicId);
                int subtaskId = manager.addNewSubtask(subtask);
                System.out.println(subtaskId != -1 ? "Создана подзадача id=" + subtaskId :"Ошибка создания подзадачи");
                break;
            default:
                System.out.println("Ошибка типа задачи");
        }
    }

    private static void updateTask() {
        System.out.println("Введите ID задачи для обновления:");
        int id = Integer.parseInt(scanner.nextLine());
        Task task = manager.getTask(id);
        Epic epic = manager.getEpic(id);
        Subtask subtask = manager.getSubtask(id);
        if (task == null && epic == null && subtask == null) {
            System.out.println("Задача не найдена");
            return;
        }
        System.out.println("Новое название:");
        String title = scanner.nextLine();
        System.out.println("Новое описание:");
        String desc = scanner.nextLine();
        Status status = Status.NEW;
        if (epic == null) {
            System.out.println("Новый статус (NEW, INPROGRESS, DONE):");
            status = Status.valueOf(scanner.nextLine());
        }
        if (task != null) {
            manager.updateTask(new Task(id, title, desc, status));
            System.out.println("Задача обновлена");
        } else if (epic != null) {
            epic.title = title;
            epic.description = desc;
            manager.updateEpic(epic);
            System.out.println("Эпик обновлён");
        } else {
            subtask.title = title;
            subtask.description = desc;
            subtask.setStatus(status);
            manager.updateSubtask(subtask);
            System.out.println("Подзадача обновлена");
        }
    }

    private static void deleteTaskById() {
        System.out.println("Введите ID задачи:");
        int id = Integer.parseInt(scanner.nextLine());
        manager.deleteTask(id);
        manager.deleteEpic(id);
        manager.deleteSubtask(id);
        System.out.println("Задача удалена, если существовала");
    }

    private static void deleteAllTasks() {
        manager.deleteTasks();
        manager.deleteEpics();
        manager.deleteSubtasks();
        System.out.println("Все задачи удалены");
    }

    private static void getEpicSubtasks() {
        System.out.println("Введите ID эпика:");
        int epicId = Integer.parseInt(scanner.nextLine());
        System.out.println(manager.getEpicSubtasks(epicId));
    }
}