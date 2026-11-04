package uk.gov.hmcts.reform.dev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;

import java.time.LocalDateTime;

public record TaskRequest(
    @NotBlank @Size(max = 255) String title,
    @Size(max = 3000) String description,
    @NotNull TaskStatus status,
    @NotNull LocalDateTime dueDate
) {

    public Task toTask() {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);
        return task;
    }
}
