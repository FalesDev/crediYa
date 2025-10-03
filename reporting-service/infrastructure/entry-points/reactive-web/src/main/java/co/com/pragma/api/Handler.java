package co.com.pragma.api;

import co.com.pragma.api.mapper.ReportMapper;
import co.com.pragma.usecase.getreport.GetReportApprovedByIdUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final GetReportApprovedByIdUseCase getReportApprovedByIdUseCase;
    private final ReportMapper reportMapper;

    public Mono<ServerResponse> getReportApprovedById(ServerRequest serverRequest) {
        return getReportApprovedByIdUseCase.getReportApprovedById()
                .map(reportMapper::toResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response)
                );
    }
}
