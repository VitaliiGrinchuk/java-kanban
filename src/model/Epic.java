package model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(int id, String title, String description) {
        super(id, title, description, Status.NEW);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int id) {
        if (id == this.id) return;
        subtaskIds.add(id);
    }

    public void removeSubtaskId(Integer id) {
        subtaskIds.remove(id);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    @Override
    public String toString() {
        return super.toString() + " Подзадачи:" + subtaskIds;
    }
}
