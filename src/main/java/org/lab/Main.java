package org.lab;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.lab.service.ProjectRepository;
import org.lab.service.ProjectService;
import org.lab.service.UserService;

public class Main {
    public static void main(String[] args) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            Future<ProjectRepository> dbInitTask = executor.submit(() -> {
                System.out.println("Инициализация базы данных...");

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("База данных инициализирована");
                return new ProjectRepository();
            });

            executor.submit(() -> {
                try {
                    System.out.println("Ожидание базы данных...");
                    ProjectRepository repo = dbInitTask.get();

                    System.out.println("База данных готова. Приложение запущено");

                    ProjectService service = new ProjectService(repo);
                    UserService userService = new UserService();

                    var userAdmin = userService.register("Admin");
                    var proj = service.createProject("Demo project", userAdmin);

                    System.out.println("Создан проект '" + proj.name() + "'");
                    System.out.println("Роль администратора: " + service.getRoleDescription(proj.id(), userAdmin.id()));

                } catch (ExecutionException | InterruptedException e) {
                    System.err.println("Ошибка в приложении: " + e.getMessage());
                }
            });

        }
    }
}
