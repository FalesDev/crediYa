package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.ReportDto;
import co.com.pragma.model.report.Report;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    ReportDto toResponse(Report report);
}
