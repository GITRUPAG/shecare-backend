package com.app.shecare.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SosResponse {
    private Long alertId;
    private String status;
    private int contactsNotified;
    private LocalDateTime triggeredAt;
}