package uk.gov.hmcts.reform.dev.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dev.dto.TaskResponse;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;

import java.util.List;

@Service
public class TaskService {

    TaskRepository taskRepository;

    public List<TaskResponse> getAllTasks() {
        return List.of();
    }
}
