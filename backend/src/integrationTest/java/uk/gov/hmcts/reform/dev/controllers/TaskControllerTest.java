package uk.gov.hmcts.reform.dev.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.dev.dto.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.services.TaskService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    private static final String VALID_TASK = """
        {"title": "Review case", "description": "Check the bundle",
         "status": "TODO", "dueDate": "2026-12-01T10:00:00"}
        """;

    @Autowired
    private transient MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @DisplayName("Should return all tasks with 200")
    @Test
    void getTasks() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(task(1L, TaskStatus.TODO), task(2L, TaskStatus.COMPLETED)));

        mockMvc.perform(get("/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].status").value("COMPLETED"));
    }

    @DisplayName("Should return a task by id and 200")
    @Test
    void getTask() throws Exception {
        when(taskService.getTask(1L)).thenReturn(task(1L, TaskStatus.TODO));

        mockMvc.perform(get("/tasks/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Task 1"))
            .andExpect(jsonPath("$.description").value("Description 1"))
            .andExpect(jsonPath("$.status").value("TODO"))
            .andExpect(jsonPath("$.dueDate").value("2026-12-01T10:00:00"));
    }

    @DisplayName("Should return 404 for an unknown task")
    @Test
    void getTaskNotFound() throws Exception {
        when(taskService.getTask(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/tasks/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Task 99 not found"));
    }

    @DisplayName("Should return 400 for a non-numeric id")
    @Test
    void getTaskInvalidId() throws Exception {
        mockMvc.perform(get("/tasks/abc"))
            .andExpect(status().isBadRequest());
    }

    @DisplayName("Should create a task and return 201")
    @Test
    void createTask() throws Exception {
        when(taskService.createTask(any())).thenReturn(task(1L, TaskStatus.TODO));

        mockMvc.perform(post("/tasks").contentType(APPLICATION_JSON).content(VALID_TASK))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("TODO"));
    }

    @DisplayName("Should return 400 with errors and not create a task when required fields are missing")
    @Test
    void createTaskMissingFields() throws Exception {
        mockMvc.perform(post("/tasks").contentType(APPLICATION_JSON).content("{\"title\": \"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Validation failed"))
            .andExpect(jsonPath("$.errors.title").value("must not be blank"))
            .andExpect(jsonPath("$.errors.status").value("must not be null"))
            .andExpect(jsonPath("$.errors.dueDate").value("must not be null"));

        verify(taskService, never()).createTask(any());
    }

    @DisplayName("Should return 400 when title exceeds max length")
    @Test
    void createTaskTitleTooLong() throws Exception {
        String body = """
            {"title": "%s", "status": "TODO", "dueDate": "2026-12-01T10:00:00"}
            """.formatted("a".repeat(256));

        mockMvc.perform(post("/tasks").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.title").exists());
    }

    @DisplayName("Should return 400 for an unknown status")
    @Test
    void createTaskInvalidStatus() throws Exception {
        String body = """
            {"title": "Review case", "status": "NOPE", "dueDate": "2026-12-01T10:00:00"}
            """;

        mockMvc.perform(post("/tasks").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @DisplayName("Should update a task's status and return 200")
    @Test
    void updateTaskStatus() throws Exception {
        when(taskService.updateStatus(1L, new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS)))
            .thenReturn(task(1L, TaskStatus.IN_PROGRESS));

        mockMvc.perform(patch("/tasks/1/status").contentType(APPLICATION_JSON).content("{\"status\": \"IN_PROGRESS\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @DisplayName("Should return 400 when status is missing from an update")
    @Test
    void updateTaskStatusMissingStatus() throws Exception {
        mockMvc.perform(patch("/tasks/1/status").contentType(APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.status").value("must not be null"));
    }

    @DisplayName("Should return 404 when updating an unknown task")
    @Test
    void updateTaskStatusNotFound() throws Exception {
        when(taskService.updateStatus(any(Long.class), any())).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(patch("/tasks/99/status").contentType(APPLICATION_JSON).content("{\"status\": \"TODO\"}"))
            .andExpect(status().isNotFound());
    }

    @DisplayName("Should delete a task with 204")
    @Test
    void deleteTask() throws Exception {
        mockMvc.perform(delete("/tasks/1"))
            .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @DisplayName("Should return 404 when deleting an unknown task")
    @Test
    void deleteTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException(99L)).when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/tasks/99"))
            .andExpect(status().isNotFound());
    }

    @DisplayName("Should return a generic 500 without leaking the exception message")
    @Test
    void unexpectedError() throws Exception {
        when(taskService.getAllTasks()).thenThrow(new RuntimeException("secret"));

        mockMvc.perform(get("/tasks"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
            .andExpect(content().string(not(containsString("secret"))));
    }

    private static Task task(Long id, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle("Task " + id);
        task.setDescription("Description " + id);
        task.setStatus(status);
        task.setDueDate(LocalDateTime.of(2026, 12, 1, 10, 0));
        return task;
    }
}
