package co.com.pragma.api.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ReportDto (
        String id,
        String status,
        Long count,
        Double totalAmount,
        Instant updatedAt
){
}
