package model;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class Epic extends Task {

    private final List<Integer> subtaskIds = new ArrayList<>();

    private LocalDateTime endTime;

    public Epic(int id, String title, String description) {
        super(id, title, description, Status.NEW);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    @Override
    public Duration getDuration() {
        return duration; // Будет рассчитываться в менеджере
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime; // Будет рассчитываться в менеджере
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setCalculatedDuration(Duration duration) {
        this.duration = duration;
    }

    public void setCalculatedStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setCalculatedEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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
