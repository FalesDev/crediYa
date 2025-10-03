package co.com.pragma.api;

import co.com.pragma.api.dto.ReportDto;
import co.com.pragma.api.exception.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/reporting/api/v1/reports",
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "getReportApprovedById",
                    operation = @Operation(
                            operationId = "getReportApprovedById",
                            summary = "Get report approved by id",
                            tags = {"Report"},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Report retrieved successfully",
                                            content = @Content(
                                                    mediaType = "application/json",
                                                    schema = @Schema(implementation = ReportDto.class)
                                            )
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler,
                                                         GlobalExceptionHandler globalExceptionHandler) {
        return RouterFunctions.route()
                .GET("/reporting/api/v1/reports", handler::getReportApprovedById)
                .filter(globalExceptionHandler)
                .build();
    }
}
