package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.response.AuthResponse;
import co.com.pragma.model.token.Token;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TokenMapper {

    AuthResponse toResponse(Token token);
}
