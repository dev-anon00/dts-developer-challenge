package uk.gov.hmcts.reform.dev.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.dto.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.services.TaskService;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RequiredArgsConstructor
@RestController
@RequestMapping("tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<TaskResponse>> getTasks() {
        return ok(taskService.getAllTasks().stream()
                      .map(TaskResponse::from)
                      .toList());
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<TaskResponse> getTask(@PathVariable long id) {
        return ok(TaskResponse.from(taskService.getTask(id)));
    }

    @PostMapping(value = "", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        return status(CREATED).body(TaskResponse.from(taskService.createTask(taskRequest)));
    }

    @PatchMapping(value = "/{id}/status", consumes = "application/json", produces = "application/json")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable long id,
                                                         @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ok(TaskResponse.from(taskService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable long id) {
        taskService.deleteTask(id);
        return noContent().build();
    }
}
