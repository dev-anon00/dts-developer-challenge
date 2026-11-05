package uk.gov.hmcts.reform.dev.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.dto.TaskRequest;
import uk.gov.hmcts.reform.dev.dto.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final LocalDateTime DUE_DATE = LocalDateTime.of(2026, 12, 1, 10, 0);

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @DisplayName("Should return all tasks from the repository")
    @Test
    void getAllTasks() {
        List<Task> tasks = List.of(task(1L, TaskStatus.TODO), task(2L, TaskStatus.COMPLETED));
        when(taskRepository.findAll()).thenReturn(tasks);

        assertThat(taskService.getAllTasks()).isEqualTo(tasks);
    }

    @DisplayName("Should return the task if it exists")
    @Test
    void getTask() {
        Task task = task(1L, TaskStatus.TODO);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThat(taskService.getTask(1L)).isSameAs(task);
    }

    @DisplayName("Should throw TaskNotFoundException if task does not exist")
    @Test
    void getTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(1L))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessage("Task 1 not found");
    }

    @DisplayName("Should save a task built from the request and return the task")
    @Test
    void createTask() {
        TaskRequest request = new TaskRequest("Review case", "Check the bundle", TaskStatus.TODO, DUE_DATE);
        Task saved = task(1L, TaskStatus.TODO);
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        when(taskRepository.save(captor.capture())).thenReturn(saved);

        assertThat(taskService.createTask(request)).isSameAs(saved);

        Task toSave = captor.getValue();
        assertThat(toSave.getId()).isNull();
        assertThat(toSave.getTitle()).isEqualTo("Review case");
        assertThat(toSave.getDescription()).isEqualTo("Check the bundle");
        assertThat(toSave.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(toSave.getDueDate()).isEqualTo(DUE_DATE);
    }

    @DisplayName("Should change the status of an existing task and save it")
    @Test
    void updateStatus() {
        Task task = task(1L, TaskStatus.TODO);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task updated = taskService.updateStatus(1L, new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS));

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(task);
    }

    @DisplayName("Should throw TaskNotFoundException and save nothing when updating a missing task")
    @Test
    void updateStatusNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateStatus(1L, new UpdateTaskStatusRequest(TaskStatus.COMPLETED)))
            .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).save(any());
    }

    @DisplayName("Should delete existing task")
    @Test
    void deleteTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }

    @DisplayName("Should throw TaskNotFoundException and delete nothing when if task does not exist")
    @Test
    void deleteTaskNotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask(1L))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessage("Task 1 not found");
        verify(taskRepository, never()).deleteById(anyLong());
    }

    private static Task task(Long id, TaskStatus status) {
        Task task = new Task();
        task.setId(id);
        task.setTitle("Task " + id);
        task.setStatus(status);
        task.setDueDate(DUE_DATE);
        return task;
    }
}
