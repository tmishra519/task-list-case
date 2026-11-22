package com.ortecfinance.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for web interface with ReST APIs for managing a list of tasks
 */
@RestController
@RequestMapping("/tasks")
public class TaskWebController {

    private final TaskListService taskListService;

    @Autowired
    public TaskWebController(TaskListService taskListService) {
        this.taskListService = taskListService;
    }

    @GetMapping(value = "/projects",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<String>>> getTasksGroupedByProject() {
        return ResponseEntity.ok(
                taskListService.getFormattedTasksByProject());
    }

    @GetMapping(value = "/projects/view-by-deadline",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Map<String, List<String>>>> getTasksGroupedByDeadlineAndProject() {
        Map<String, Map<String, List<String>>> groupedTasks = taskListService.getFormattedTasksByDeadlineAndProject();
        return ResponseEntity.ok(groupedTasks);
    }

    @PostMapping("/projects")
    public ResponseEntity<String> addProject(@RequestBody TaskDTO taskDTO) {
        try {
            taskListService.addProject(taskDTO.name());
            return ResponseEntity.status(HttpStatus.CREATED).body("Created project with name " + taskDTO.name());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to create project with name " + taskDTO.name());
        }
    }

    @PostMapping("/projects/{project_id}/tasks")
    public ResponseEntity<String> addTask(@PathVariable String project_id, @RequestBody TaskDTO taskDTO) {
        try {
            taskListService.addTask(project_id, taskDTO.name());
            return ResponseEntity.status(HttpStatus.CREATED).body("Created task with description " + taskDTO.name());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to create task with description " + taskDTO.name());
        }
    }

    @PutMapping("/projects/{project_id}/tasks/{task_id}")
    public ResponseEntity<String> addDeadline(@PathVariable String project_id, @PathVariable Integer task_id,
                                              @RequestParam String deadline) {
        try {
            taskListService.addDeadline(task_id, deadline);
            return ResponseEntity.status(HttpStatus.OK).body("Added deadline to task " + task_id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to update task with id " + task_id);
        }
    }
}
