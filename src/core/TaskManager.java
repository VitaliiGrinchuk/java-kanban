package core;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;
public interface TaskManager {

        List<Task> getTasks();

        List<Subtask> getSubtasks();

        List<Epic> getEpics();

        List<Subtask> getEpicSubtasks(int epicId);

        Task getTask(int id);

        Subtask getSubtask(int id);

        Epic getEpic(int id);

        int addNewTask(Task task);

        int addNewEpic(Epic epic);

        int addNewSubtask(Subtask subtask);

        void updateTask(Task task);

        void updateEpic(Epic epic);

        void updateSubtask(Subtask subtask);

        void deleteTask(int id);

        void deleteEpic(int id);

        void deleteSubtask(int id);

        void deleteTasks();

        void deleteEpics();

        void deleteSubtasks();

        List<Task> getHistory();
}
