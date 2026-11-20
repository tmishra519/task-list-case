package com.ortecfinance.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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

    private void add(String commandLine) throws Exception {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            taskListService.addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            taskListService.addTask(projectTask[0], projectTask[1]);
        }
    }

    private void check(String idString) throws Exception {
        taskListService.check(getId(idString));
    }

    private void uncheck(String idString) throws Exception {
        taskListService.uncheck(getId(idString));
    }

    private void show() {
        var tasks = taskListService.getTasks();
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                out.println(task.toString());
            }
            out.println();
        }
    }

    private void addDeadline(String commandLine) throws Exception {
        String[] argumentList = commandLine.split(" ", 2);
        Integer id = getId(argumentList[0]);
        taskListService.addDeadline(id, argumentList[1]);
    }

    private void showByDeadline() {
        Map<LocalDate, Map<String, List<Task>>> groupedTasks = taskListService.groupByDeadline();
        groupedTasks.forEach((deadline, projectTasks) -> {
                    String formattedDeadline = deadline == null ? "No Deadline" :
                            deadline.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    out.println(formattedDeadline);
                    projectTasks.forEach((project, tasks) -> {
                        out.println("\t" + project);
                        tasks.forEach(
                                (task) -> out.println("\t" + task.toString())
                        );
                    });
                }
        );
    }

    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <task ID> <dd-MM-yyyy>");
        out.println("  view-by-deadline");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }

    private int getId(String idString) throws Exception {
        try {
            return Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            throw new Exception("Could not find a task with an ID of " + idString);
        }
    }

}
