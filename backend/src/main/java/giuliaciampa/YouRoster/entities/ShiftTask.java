package giuliaciampa.YouRoster.entities;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "shift_tasks")
public class ShiftTask {

    //ATTRIBUTI

    @Id
    @GeneratedValue
    @Column(name = "shift_task_id")
    private UUID id;


    @JoinColumn(name = "shiftAssignment_id", nullable = false)
    @ManyToOne
    private ShiftAssignment shiftAssignment;

    private LocalTime time;

    @ManyToOne
    @JoinColumn(name = "shift_task_title")
    private ShiftTaskTitle taskTitle;

    //COSTRUTTORE
    public ShiftTask(ShiftAssignment shiftAssignment, LocalTime time, ShiftTaskTitle taskTitle) {
        this.shiftAssignment = shiftAssignment;
        this.time = time;
        this.taskTitle = taskTitle;
    }

    //COSTRUTTORE VUOTO

    public ShiftTask() {
    }

    //GETTER E SETTER

    public UUID getId() {
        return id;
    }

    public ShiftAssignment getShiftAssignment() {
        return shiftAssignment;
    }

    public void setShiftAssignment(ShiftAssignment shiftAssignment) {
        this.shiftAssignment = shiftAssignment;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public ShiftTaskTitle getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(ShiftTaskTitle taskTitle) {
        this.taskTitle = taskTitle;
    }

    //TOSTRING
    @Override
    public String toString() {
        return "ShiftTask{" +
                "id=" + getId() +
                ", shiftAssignment=" + getShiftAssignment() +
                ", time=" + getTime() +
                ", taskTitle=" + getTaskTitle() +
                '}';
    }
}
