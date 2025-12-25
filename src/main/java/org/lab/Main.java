package org.lab;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.lab.service.MilestoneService;
import org.lab.service.ProjectRepository;
import org.lab.service.ProjectService;
import org.lab.service.TicketService;
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
                    var repo = dbInitTask.get();

                    System.out.println("База данных готова. Приложение запущено");

                    var projectService = new ProjectService(repo);
                    var userService = new UserService();
                    var testUser = userService.register("Manager Viktor");
                    var testProj = projectService.createProject("Viktor's project", testUser);
                    var testProjId = testProj.id();
                    System.out.println("Создан проект '" + testProj.name() + "'");
                    System.out.println("Роль администратора: " + projectService.getRoleDescription(testProj.id(),
                            testUser.id()));

                    var milestoneService = new MilestoneService(repo);
                    milestoneService.createMilestone(testProjId,
                            "First milestone",
                            LocalDate.now(),
                            LocalDate.ofYearDay(2026, 2),
                            testUser);
                    var firstMilestone = projectService.getProject(testProjId).milestones().getFirst();
                    var firstMilestoneId = firstMilestone.id();
                    System.out.println("Создан milestone " + firstMilestone.name());

                    var ticketService = new TicketService(repo);
                    ticketService.createTicket(testProjId,
                            firstMilestoneId,
                            "First ticket",
                            "Ticket desc",
                            testUser);
                    var firstTicket = projectService.getProject(testProjId).milestones().getFirst().tickets().getFirst();
                    System.out.println("Создан ticket " + firstTicket.title());
                } catch (ExecutionException | InterruptedException e) {
                    System.err.println("Ошибка в приложении: " + e.getMessage());
                }
            });

        }
    }
}
