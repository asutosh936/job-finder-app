package com.asutosh.jobtracker.controller;

import com.asutosh.jobtracker.model.Job;
import com.asutosh.jobtracker.model.JobStatus;
import com.asutosh.jobtracker.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    private Map<JobStatus, List<Job>> emptyBoard() {
        Map<JobStatus, List<Job>> board = new LinkedHashMap<>();
        for (JobStatus status : JobStatus.values()) {
            board.put(status, List.of());
        }
        return board;
    }

    @Test
    void boardRendersAllFiveColumnsWhenEmpty() throws Exception {
        when(jobService.getJobsGroupedByStatus()).thenReturn(emptyBoard());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("board"))
                .andExpect(content().string(containsString("Wishlist")))
                .andExpect(content().string(containsString("Applied")))
                .andExpect(content().string(containsString("Interviewing")))
                .andExpect(content().string(containsString("Offer")))
                .andExpect(content().string(containsString("Rejected")))
                .andExpect(content().string(containsString("No jobs yet.")));
    }

    @Test
    void boardRendersJobCardWhenPresent() throws Exception {
        Map<JobStatus, List<Job>> board = emptyBoard();
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Backend Engineer");
        job.setCompany("Acme Corp");
        job.setLocation("Remote");
        job.setStatus(JobStatus.WISHLIST);
        board.put(JobStatus.WISHLIST, List.of(job));
        when(jobService.getJobsGroupedByStatus()).thenReturn(board);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Backend Engineer")))
                .andExpect(content().string(containsString("Acme Corp")))
                .andExpect(content().string(containsString("Remote")));
    }
}
