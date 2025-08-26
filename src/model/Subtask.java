package model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(int id, String title, String description, Status status, int epicId) {
        super(id, title, description, status);
        if (id == epicId) {
            throw new IllegalArgumentException("Subtask cannot be its own epic");
        }
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return super.toString() + " [Model.Epic ID:" + epicId + "]";
    }
}
