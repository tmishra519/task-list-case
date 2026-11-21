package com.ortecfinance.tasklist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TaskListService}
 */
public class TaskListServiceTest {

    @Spy
    private TaskListService taskListService;

    @BeforeEach
    void setup() {
        taskListService = new TaskListService();
    }

    @Test
    void shouldAddProject() {
        taskListService.addProject("Project1");
        assertTrue(taskListService.getTasks().containsKey("Project1"));
    }

    @Test
    void shouldAddTask() {
        taskListService.addProject("Project1");
        assertDoesNotThrow(() ->
                taskListService.addTask("Project1", "Task1"));
        List<Task> tasks = taskListService.getTasks().get("Project1");
        assertTrue(tasks.stream().anyMatch(t -> t.getDescription().equals("Task1")));

    }

    @Test
    void shouldThrowExceptionForInvalidProjectOnAddingTask() {
        taskListService.addProject("Project1");
        assertThrows(Exception.class, () -> taskListService.addTask("Project100", "Task1"));
    }

    @Test
    void shouldCheckTask() {
        taskListService.addProject("Project1");
        assertDoesNotThrow(() -> taskListService.addTask("Project1", "Task1"));
        assertDoesNotThrow(() -> taskListService.check(1));
        assertTrue(taskListService.getTasks().get("Project1").getFirst().isDone());
    }

    @Test
    void shouldUncheckTask() {
        taskListService.addProject("Project1");
        assertDoesNotThrow(() -> taskListService.addTask("Project1", "Task1"));
        assertDoesNotThrow(() -> taskListService.check(1));
        assertDoesNotThrow(() -> taskListService.uncheck(1));
        assertFalse(taskListService.getTasks().get("Project1").getFirst().isDone());
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFoundOnCheck() {
        taskListService.addProject("Project1");
        assertDoesNotThrow(() -> taskListService.addTask("Project1", "Task1"));
        assertThrows(Exception.class, () -> taskListService.check(2));
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFoundOnUncheck() {
        taskListService.addProject("Project1");
        assertDoesNotThrow(() -> taskListService.addTask("Project1", "Task1"));
        assertThrows(Exception.class, () -> taskListService.uncheck(2));
    }

    @Test
    void shouldAddDeadline() {
        taskListService.addProject("Project1");
        assertDoesNotThrow(() -> taskListService.addTask("Project1", "Task1"));
        assertDoesNotThrow(() -> taskListService.addDeadline(1, "12-12-2025"));
        assertEquals("12-12-2025", taskListService.getTasks().get("Project1").getFirst().getDeadline()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFoundOnAddingDeadline() {
        taskListService.addProject("Project1");
        assertDoesNotThrow(() -> taskListService.addTask("Project1", "Task1"));
        assertThrows(Exception.class, () -> taskListService.addDeadline(2, "12-12-2025"));
    }

    @Test
    void shouldThrowExceptionWhenInvalidDateOnAddingDeadline() {
        taskListService.addProject("Project1");
        assertDoesNotThrow(() -> taskListService.addTask("Project1", "Task1"));
        assertThrows(Exception.class, () -> taskListService.addDeadline(1, "78-12-2025"));
    }

    @Test
    void shouldGroupByDeadlineAndProject() throws Exception {
        initTestData();
        var tasksGroupedByDeadlineAndProject = taskListService.groupTasksByDeadlineAndProject();

        assertTrue(tasksGroupedByDeadlineAndProject.containsKey(
                LocalDate.parse("10-12-2026", DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        assertTrue(tasksGroupedByDeadlineAndProject.containsKey(
                LocalDate.parse("15-12-2026", DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        assertTrue(tasksGroupedByDeadlineAndProject.containsKey(
                LocalDate.parse("20-12-2026", DateTimeFormatter.ofPattern("dd-MM-yyyy"))));

        var mapEntry = tasksGroupedByDeadlineAndProject.get(
                LocalDate.parse("10-12-2026", DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        assertTrue(mapEntry.containsKey("ProjectA"));
        assertTrue(mapEntry.containsKey("ProjectB"));
        assertEquals(1, mapEntry.get("ProjectA").size());
        assertEquals(1, mapEntry.get("ProjectB").size());

        assertEquals(1, tasksGroupedByDeadlineAndProject.get(null).get("ProjectA").size());
        assertEquals(1, tasksGroupedByDeadlineAndProject.get(null).get("ProjectC").size());
    }

    @Test
    void shouldGetTasksForDeadline() throws Exception {
        initTestData();
        assertNotNull(taskListService.getTasksForDeadline(
                LocalDate.parse("10-12-2026", DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        assertNull(taskListService.getTasksForDeadline(
                LocalDate.parse("10-12-2028", DateTimeFormatter.ofPattern("dd-MM-yyyy"))));

    }

    private void initTestData() throws Exception {
        taskListService.addProject("ProjectA");
        taskListService.addProject("ProjectB");
        taskListService.addProject("ProjectC");

        taskListService.addTask("ProjectA", "Implement login");
        taskListService.addTask("ProjectA", "Create dashboard");
        taskListService.addTask("ProjectA", "Add unit tests");

        taskListService.addTask("ProjectB", "Implement REST API");
        taskListService.addTask("ProjectB", "Add database integration");

        taskListService.addTask("ProjectC", "Write documentation");

        // Same deadline across two projects
        taskListService.addDeadline(1, "10-12-2026");
        taskListService.addDeadline(4, "10-12-2026");

        taskListService.addDeadline(2, "15-12-2026");
        taskListService.addDeadline(5, "20-12-2026");
    }


}
