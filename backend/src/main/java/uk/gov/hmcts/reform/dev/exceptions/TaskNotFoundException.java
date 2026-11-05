package uk.gov.hmcts.reform.dev.exceptions;

import java.io.Serial;

public class TaskNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TaskNotFoundException(long id) {
        super("Task " + id + " not found");
    }
}
