package com.ortecfinance.tasklist;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TaskListService {

    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private long lastId = 0;

    public Map<String, List<Task>> getTasks() {
        return tasks;
    }

    public void addProject(String name) {
        tasks.put(name, new ArrayList<>());
    }

    public void addTask(String project, String description) throws Exception {
        if (tasks.isEmpty() || tasks.get(project) == null) {
              throw new Exception("Could not find a project with the name " + project);
        }
        List<Task> projectTasks = tasks.get(project);
        projectTasks.add(new Task(nextId(), description, false, null));
    }

    public void check(Integer id) throws Exception {
        setDone(id, true);
    }

    public void uncheck(Integer id) throws Exception {
        setDone(id, false);
    }

    public void addDeadline(Integer id, String deadline) throws Exception {
        LocalDate date = LocalDate.parse(deadline, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    task.setDeadline(date);
                    return;
                }
            }
        }
        throw new Exception("Could not find a task with an ID of " + id);
    }

    public Map<LocalDate, Map<String, List<Task>>> groupByDeadline() {
        Map<LocalDate, Map<String, List<Task>>> groupedTasks =
                new TreeMap<>(Comparator.nullsLast(Comparator.naturalOrder()));

        tasks.forEach((project, projectTasks) -> {
            projectTasks.forEach(task -> {
                groupedTasks
                        .computeIfAbsent(task.getDeadline(), k -> new LinkedHashMap<>())
                        .computeIfAbsent(project, k -> new ArrayList<>())
                        .add(task);
            });
        });
        return groupedTasks;
    }

    private void setDone(Integer id, boolean done) throws Exception {
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    task.setDone(done);
                    return;
                }
            }
        }
        throw new Exception("Could not find a task with an ID of " + id);
    }

    private long nextId() {
        return ++lastId;
    }
}
