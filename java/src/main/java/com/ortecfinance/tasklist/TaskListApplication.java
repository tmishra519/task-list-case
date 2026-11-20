package com.ortecfinance.tasklist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TaskListApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext context =
                SpringApplication.run(TaskListApplication.class, args);

        if (args.length == 0) {
            System.out.println("Starting console Application");
            TaskConsoleController controller = context.getBean(TaskConsoleController.class);
            controller.run();
        }
        else {
            System.out.println("localhost:8080/tasks");
        }
    }

}
