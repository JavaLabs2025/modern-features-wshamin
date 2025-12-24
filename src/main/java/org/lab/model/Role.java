package org.lab.model;

public sealed interface Role permits Role.Manager, Role.TeamLead, Role.Developer, Role.Tester {
    record Manager() implements Role {
    }

    record TeamLead() implements Role {
    }

    record Developer() implements Role {
    }

    record Tester() implements Role {
    }
}
