package org.lab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lab.model.*;
import org.lab.service.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectSystemTest {

    private UserService userService;
    private ProjectRepository projectRepository;
    private ProjectService projectService;
    private MilestoneService milestoneService;
    private TicketService ticketService;
    private BugReportService bugReportService;

    @BeforeEach
    void setUp() {
        projectRepository = new ProjectRepository();
        userService = new UserService();
        projectService = new ProjectService(projectRepository);
        milestoneService = new MilestoneService(projectRepository);
        ticketService = new TicketService(projectRepository);
        bugReportService = new BugReportService(projectRepository);
    }

    @Test
    void testProjectCreationAndTeam() {
        User manager = userService.register("Manager Viktor");
        User dev = userService.register("Dev Andrey");

        Project project = projectService.createProject("Demo project", manager);
        assertEquals(manager, project.manager());

        projectService.addDeveloper(project.id(), dev, manager);
        Project p = projectService.getProject(project.id());
        assertTrue(p.developers().stream().anyMatch(d -> d.id().equals(dev.id())));

        User other = userService.register("Spy");
        assertThrows(IllegalArgumentException.class, () -> projectService.addDeveloper(project.id(), other, dev));
    }

    @Test
    void testMilestoneLifecycle() {
        User manager = userService.register("Manager Viktor");
        Project project = projectService.createProject("Demo project 1", manager);
        LocalDate now = LocalDate.now();

        milestoneService.createMilestone(project.id(), "Demo milestone 1", now, now, manager);
        milestoneService.createMilestone(project.id(), "Demo milestone 2", now, now, manager);

        Project p = projectService.getProject(project.id());
        Long m1Id = p.milestones().get(0).id();
        Long m2Id = p.milestones().get(1).id();

        milestoneService.updateMilestoneStatus(project.id(), m1Id, MilestoneStatus.ACTIVE, manager);

        assertThrows(IllegalStateException.class,
                () -> milestoneService.updateMilestoneStatus(project.id(), m2Id, MilestoneStatus.ACTIVE, manager));

        User tl = userService.register("Team Lead Kirill");
        projectService.setTeamLead(project.id(), tl, manager);
        ticketService.createTicket(project.id(), m1Id, "Task", "Desc", tl);

        assertThrows(IllegalStateException.class,
                () -> milestoneService.updateMilestoneStatus(project.id(), m1Id, MilestoneStatus.CLOSED, manager));
    }

    @Test
    void testTicketWorkflow() {
        User manager = userService.register("Manager Viktor");
        User tl = userService.register("Team Lead Kirill");
        User dev = userService.register("Dev Andrey");

        Project project = projectService.createProject("Demo project 1", manager);
        projectService.addDeveloper(project.id(), dev, manager);
        projectService.setTeamLead(project.id(), tl, manager);

        LocalDate now = LocalDate.now();
        milestoneService.createMilestone(project.id(), "Demo milestone 1", now, now, manager);
        Long mId = projectService.getProject(project.id()).milestones().get(0).id();

        ticketService.createTicket(project.id(), mId, "Task 1", "Desc 1", tl);
        Ticket ticket = projectService.getProject(project.id()).milestones().get(0).tickets().get(0);

        ticketService.assignTicket(project.id(), mId, ticket.id(), dev, tl);

        ticketService.updateTicketStatus(project.id(), mId, ticket.id(), TicketStatus.IN_PROGRESS, dev);
        ticketService.updateTicketStatus(project.id(), mId, ticket.id(), TicketStatus.COMPLETED, dev);

        User tester = userService.register("Tester Vladimir");
        projectService.addTester(project.id(), tester, manager);
        assertThrows(IllegalArgumentException.class,
                () -> ticketService.updateTicketStatus(project.id(), mId, ticket.id(), TicketStatus.ACCEPTED, tester));
        ticketService.createTicket(project.id(), mId, "Team Lead Task", "Descr 2", manager);
        Ticket t2 = projectService.getProject(project.id()).milestones().get(0).tickets().get(1);
        ticketService.assignTicket(project.id(), mId, t2.id(), tl, manager);

        ticketService.updateTicketStatus(project.id(), mId, t2.id(), TicketStatus.COMPLETED, tl);
    }

    @Test
    void testBugReportWorkflow() {
        User manager = userService.register("Manager Viktor");
        User dev = userService.register("Dev Andrey");
        User tester = userService.register("Tester Vladimir");

        Project project = projectService.createProject("Demo project 1", manager);
        projectService.addDeveloper(project.id(), dev, manager);
        projectService.addTester(project.id(), tester, manager);

        bugReportService.createBugReport(project.id(), "Bug 1", "Desc 1", tester);

        bugReportService.createBugReport(project.id(), "Bug 2", "Desc 2", dev);

        Project p = projectService.getProject(project.id());
        assertEquals(2, p.bugReports().size());
        BugReport b1 = p.bugReports().get(0);

        bugReportService.updateBugStatus(project.id(), b1.id(), BugStatus.FIXED, dev);

        bugReportService.updateBugStatus(project.id(), b1.id(), BugStatus.TESTED, tester);

        List<BugReport> devBugs = bugReportService.getBugReportsForUser(dev);
        assertEquals(1, devBugs.size());
        assertEquals("Bug 2", devBugs.get(0).title());
    }

    @Test
    void testMultiRole() {
        User u = userService.register("User Larisa");
        Project p1 = projectService.createProject("Demo project 1", u);

        User u2 = userService.register("Spy");
        Project p2 = projectService.createProject("Demo project 2", u2);
        projectService.addDeveloper(p2.id(), u, u2);

        assertEquals(new Role.Manager(), projectService.getUserRole(p1.id(), u.id()));
        assertEquals(new Role.Developer(), projectService.getUserRole(p2.id(), u.id()));
    }
}
