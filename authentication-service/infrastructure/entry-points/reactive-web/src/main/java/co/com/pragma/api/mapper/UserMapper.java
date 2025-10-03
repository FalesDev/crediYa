package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.dto.request.RegisterUserRequestDto;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idRole", ignore = true)
    User toEntity(RegisterUserRequestDto dto);

    UserDto toResponse(User user);
}
