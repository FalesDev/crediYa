package co.com.pragma.model.report;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReportApprovedCountMessage {
    private UUID applicationId;
    private Double amount;
    private String state;
}
