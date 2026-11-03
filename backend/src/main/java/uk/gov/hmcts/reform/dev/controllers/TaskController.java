package uk.gov.hmcts.reform.dev.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.services.TaskService;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("tasks")
public class TaskController {

    TaskService taskService;

    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<TaskResponse>> getTasks() {
        return ok(List.of());
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<TaskResponse> getTask(@PathVariable long id) {
        return ok(new TaskResponse());
    }
}
