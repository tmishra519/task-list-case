package com.ortecfinance.tasklist;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service class with business logic for managing a list of tasks
 */
@Service
public class TaskListService {

    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private long lastId = 0;

    public Map<String, List<Task>> getTasks() {
        return tasks;
    }

    /**
     * Method to convert tasks to string for optimal display
     *
     * @return Map with key=project, value=list of task details as strings
     */
    public Map<String, List<String>> getTasksByProject() {
        Map<String, List<String>> response = new LinkedHashMap<>();
        tasks.forEach((project, projectTasks) ->
                response.put(project, projectTasks.stream().map(Task::toString).toList()));
        return response;
    }

    /**
     * Add a new project
     *
     * @param name - name of the project
     */
    public void addProject(String name) {
        tasks.put(name, new ArrayList<>());
    }

    /**
     * Add a new task
     *
     * @param project     - project for task
     * @param description - description of task
     * @throws InvalidIdException - thrown in case the project is not found
     */
    public void addTask(String project, String description) throws InvalidIdException {
        if (tasks.isEmpty() || tasks.get(project) == null) {
            throw new InvalidIdException("Could not find a project with the name " + project);
        }
        List<Task> projectTasks = tasks.get(project);
        projectTasks.add(new Task(nextId(), description, false, null));
    }

    /**
     * Mark a task as done
     *
     * @param id - id of task to be updated
     * @throws InvalidIdException - In case the task id is not valid
     */
    public void check(Integer id) throws InvalidIdException {
        setDone(id, true);
    }

    /**
     * Mark a task as not done
     *
     * @param id - id of task to be updated
     * @throws InvalidIdException - In case the task id is not valid
     */
    public void uncheck(Integer id) throws InvalidIdException {
        setDone(id, false);
    }

    /**
     * Update isDone on a task
     *
     * @param id   - task to be updated
     * @param done - true/false - is task completed?
     * @throws InvalidIdException - In case the task id is not valid
     */
    private void setDone(Integer id, boolean done) throws InvalidIdException {
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    task.setDone(done);
                    return;
                }
            }
        }
        throw new InvalidIdException("Could not find a task with an ID of " + id);
    }

    /**
     * Add deadline to a task
     *
     * @param id       - task to be updated
     * @param deadline - deadline for task in the dd-MM-yyyy format
     * @throws InvalidIdException     - In case the task id is not valid
     * @throws DateTimeParseException - In case the date is in an invalid format
     */
    public void addDeadline(Integer id, String deadline) throws InvalidIdException, DateTimeParseException {
        LocalDate date = LocalDate.parse(deadline, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    task.setDeadline(date);
                    return;
                }
            }
        }
        throw new InvalidIdException("Could not find a task with an ID of " + id);
    }

    /**
     * Create a two level map with deadline as key and project as secondary key
     *
     * @return - tasks grouped by deadline and project
     */
    public Map<LocalDate, Map<String, List<String>>> groupTasksByDeadlineAndProject() {
        Map<LocalDate, Map<String, List<String>>> groupedTasks = new TreeMap<>(Comparator.nullsLast(Comparator.naturalOrder()));

        tasks.forEach((project, projectTasks) -> {
            projectTasks.forEach(task -> {
                groupedTasks
                        .computeIfAbsent(task.getDeadline(), k -> new LinkedHashMap<>())
                        .computeIfAbsent(project, k -> new ArrayList<>())
                        .add(task.toStringWithoutCheckbox());
            });
        });
        return groupedTasks;
    }

    /**
     * Method to get tasks with a particular deadline
     *
     * @param deadline - deadline to use as key
     * @return - list of tasks with this deadline
     */
    public Map<String, List<String>> getTasksForDeadline(LocalDate deadline) {
        return groupTasksByDeadlineAndProject().get(deadline);
    }

    /**
     * Method to generate next id for a task
     *
     * @return - incremented id
     */
    private long nextId() {
        return ++lastId;
    }
}
