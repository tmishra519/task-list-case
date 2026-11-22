package com.ortecfinance.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for command line interface for managing tasks list
 */
@Component
public final class TaskConsoleController implements Runnable {
    private static final String QUIT = "quit";

    private final BufferedReader in;
    private final PrintWriter out;
    private final TaskListService taskListService;

    @Autowired
    public TaskConsoleController(TaskListService taskListService) {
        this.in = new BufferedReader(new InputStreamReader(System.in));
        this.out = new PrintWriter(System.out);
        this.taskListService = taskListService;
    }

    public TaskConsoleController(BufferedReader in, PrintWriter out, TaskListService taskListService) {
        this.in = in;
        this.out = out;
        this.taskListService = taskListService;
    }

    public void run() {
        out.println("Welcome to TaskList! Type 'help' for available commands.");
        while (true) {
            out.print("> ");
            out.flush();
            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            execute(command);
        }
    }

    private void execute(String commandLine) {
        try {
            String[] commandRest = commandLine.split(" ", 2);
            String command = commandRest[0];
            switch (command) {
                case "show":
                    show();
                    break;
                case "add":
                    add(commandRest[1]);
                    break;
                case "check":
                    check(commandRest[1]);
                    break;
                case "uncheck":
                    uncheck(commandRest[1]);
                    break;
                case "deadline":
                    addDeadline(commandRest[1]);
                    break;
                case "view-by-deadline":
                    showByDeadline();
                    break;
                case "today":
                    getTasksWithDeadlineToday();
                    break;
                case "help":
                    help();
                    break;
                default:
                    error(command);
                    break;
            }
        } catch (Exception e) {
            out.println(e.getMessage());
        }
    }

    /**
     * Parse arguments and call intended target
     * @param commandLine - user input
     * @throws InvalidIdException - exception thrown when task id is invalid
     */
    private void add(String commandLine) throws InvalidIdException {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            taskListService.addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            taskListService.addTask(projectTask[0], projectTask[1]);
        }
    }

    /**
     * Mark task as done
     * @param idString - id of task
     * @throws InvalidIdException - In case task id is invalid
     * @throws NumberFormatException - In case id can't be parsed to a number
     */
    private void check(String idString) throws InvalidIdException, NumberFormatException {
        taskListService.check(getId(idString));
    }

    /**
     * Mark task as not done
     *
     * @param idString - id of task
     * @throws InvalidIdException - In case task id is invalid
     * @throws NumberFormatException - In case id can't be parsed to a number
     */
    private void uncheck(String idString) throws InvalidIdException, NumberFormatException  {
        taskListService.uncheck(getId(idString));
    }

    /**
     * Display all tasks grouped by deadline
     */
    private void show() {
        var tasks = taskListService.getFormattedTasksByProject();
        for (Map.Entry<String, List<String>> project : tasks.entrySet()) {
            out.println(project.getKey());
            project.getValue().forEach(out::println);
            out.println();
        }
    }

    /**
     * @param commandLine - argument with task id and deadline
     * @throws NumberFormatException - In case the date format is invalid.
     * @throws InvalidIdException    - In case the task is not valid.
     */
    private void addDeadline(String commandLine) throws NumberFormatException, InvalidIdException {
        String[] argumentList = commandLine.split(" ", 2);
        Integer id = getId(argumentList[0]);
        taskListService.addDeadline(id, argumentList[1]);
    }

    /**
     * Method to display tasks grouped by deadline then project
     */
    private void showByDeadline() {
        Map<String, Map<String, List<String>>> groupedTasks = taskListService.getFormattedTasksByDeadlineAndProject();
        groupedTasks.forEach((deadline, projectTasks) -> {
                    out.println(deadline);
                    projectTasks.forEach((project, tasks) -> {
                        out.println("    " + project);
                        tasks.forEach(t -> out.println("        " + t));
                    });
                }
        );
    }

    /**
     * Display all tasks that have a deadline set to today
     */
    private void getTasksWithDeadlineToday() {
        var tasksWithDeadlineToday = taskListService.getTasksForDeadline(LocalDate.now());

        if (tasksWithDeadlineToday == null) {
            out.println("No tasks with deadline today");
        } else {
            tasksWithDeadlineToday.forEach((project, tasks) -> {
                        out.println(project);
                        tasks.forEach(t -> out.println("        " + t));
                        out.println();
                    }
            );
        }
    }

    /**
     * List all available commands
     */
    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <task ID> <dd-MM-yyyy>");
        out.println("  view-by-deadline");
        out.println("  today");
        out.println();
    }

    /**
     * Error when command not found
     *
     * @param command - user input command
     */
    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }

    /**
     * Get numerical id from String
     *
     * @param idString - id as string
     * @return - id as number
     * @throws NumberFormatException - If not able to parse the id
     */
    private int getId(String idString) throws NumberFormatException {
        try {
            return Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Could not parse the id " + idString);
        }
    }

}
