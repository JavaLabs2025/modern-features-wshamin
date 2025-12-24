package org.lab.service;

import org.lab.model.Project;
import org.lab.model.User;

public class AuthorizationUtils {
    public static boolean isManager(Project p, User u) {
        return p.manager().id().equals(u.id());
    }

    public static boolean isTeamLead(Project p, User u) {
        return p.teamLead().isPresent() && p.teamLead().get().id().equals(u.id());
    }

    public static boolean isDeveloper(Project p, User u) {
        return p.developers().stream().anyMatch(d -> d.id().equals(u.id()));
    }

    public static boolean isTester(Project p, User u) {
        return p.testers().stream().anyMatch(t -> t.id().equals(u.id()));
    }
}
