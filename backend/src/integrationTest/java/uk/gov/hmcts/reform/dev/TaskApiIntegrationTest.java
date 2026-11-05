package uk.gov.hmcts.reform.dev;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs against H2
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskApiIntegrationTest {

    @Autowired
    private transient MockMvc mockMvc;

    @DisplayName("Should create, read, update the status of and delete a task")
    @Test
    void taskLifecycle() throws Exception {
        String created = mockMvc.perform(post("/tasks").contentType(APPLICATION_JSON).content("""
                                                                                                   {"title": "Review case", "description": "Check the bundle",
                                                                                                   "status": "TODO", "dueDate": "2026-12-01T10:00:00"}
                                                                                                  """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(created, "$.id");

        mockMvc.perform(get("/tasks/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Review case"))
            .andExpect(jsonPath("$.description").value("Check the bundle"))
            .andExpect(jsonPath("$.status").value("TODO"))
            .andExpect(jsonPath("$.dueDate").value("2026-12-01T10:00:00"));

        List<Integer> ids = JsonPath.read(getTasks(), "$[*].id");
        assertThat(ids).contains(id);

        mockMvc.perform(patch("/tasks/{id}/status", id).contentType(APPLICATION_JSON)
                            .content("{\"status\": \"COMPLETED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(get("/tasks/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(delete("/tasks/{id}", id))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", id))
            .andExpect(status().isNotFound());
    }

    @DisplayName("Should reject an invalid task and save nothing")
    @Test
    void invalidTaskIsNotSaved() throws Exception {
        int before = JsonPath.<List<?>>read(getTasks(), "$").size();

        mockMvc.perform(post("/tasks").contentType(APPLICATION_JSON).content("{\"title\": \"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.title").exists());

        assertThat(JsonPath.<List<?>>read(getTasks(), "$")).hasSize(before);
    }

    private String getTasks() throws Exception {
        return mockMvc.perform(get("/tasks"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }
}
