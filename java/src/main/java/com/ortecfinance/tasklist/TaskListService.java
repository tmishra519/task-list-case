package com.ortecfinance.tasklist;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskListService {

    public Map<String, List<Task>> getTasks() {
        return tasks;
    }

    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private long lastId = 0;


    public void addProject(String name) {
        tasks.put(name, new ArrayList<>());
    }

    public void addTask(String project, String description) {
        List<Task> projectTasks = tasks.get(project);
        if (projectTasks == null) {
//            out.printf("Could not find a project with the name \"%s\".", project);
//            out.println();
            return;
        }
        projectTasks.add(new Task(nextId(), description, false));
    }

    public void check(String idString) {
        setDone(idString, true);
    }

    public void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) {
        int id = Integer.parseInt(idString);
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    task.setDone(done);
                    return;
                }
            }
        }
//        out.printf("Could not find a task with an ID of %d.", id);
//        out.println();
    }

    private long nextId() {
        return ++lastId;
    }
}
