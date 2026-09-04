package com.graduation.project;

import com.graduation.project.clinic.examination.service.ExaminationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import java.util.UUID;

@SpringBootTest
public class HistoryTest {

    @Autowired
    private ExaminationService examinationService;

    @Test
    public void testHistory() {
        try {
            System.out.println("TESTING HISTORY...");
            // doctor_uuid from seed script
            UUID doctorUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440099");
            var history = examinationService.getHistory(doctorUserId, PageRequest.of(0, 10));
            System.out.println("HISTORY FOUND: " + history.getTotalElements());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
