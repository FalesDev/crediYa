package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.ReportDto;
import co.com.pragma.model.report.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReportMapperTest {

    private ReportMapper mapper;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(ReportMapper.class);
    }

    @Test
    @DisplayName("Should map Report entity to ReportDto correctly")
    void testToResponse() {
        Report report = Report.builder()
                .id("test-report")
                .status("APPROVED")
                .count(7L)
                .totalAmount(5000.0)
                .updatedAt(Instant.now())
                .build();

        ReportDto dto = mapper.toResponse(report);

        assertNotNull(dto);
        assertEquals(report.getId(), dto.id());
        assertEquals(report.getStatus(), dto.status());
        assertEquals(report.getCount(), dto.count());
        assertEquals(report.getTotalAmount(), dto.totalAmount());
        assertEquals(report.getUpdatedAt(), dto.updatedAt());
    }

    @Test
    @DisplayName("Should return null when mapping null values")
    void testNullHandling() {
        assertNull(mapper.toResponse(null));
    }
}
