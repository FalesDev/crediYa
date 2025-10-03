package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.dto.request.RegisterUserRequestDto;
import co.com.pragma.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    @DisplayName("Should map RegisterUserRequestDto to User entity correctly")
    void testToEntity() {
        RegisterUserRequestDto dto = new RegisterUserRequestDto(
                "Fabricio",
                "Rodriguez",
                "fabricio@example.com",
                "77777777",
                "909090909",
                2500.50,
                "password123"
        );

        User user = mapper.toEntity(dto);

        assertNotNull(user);
        assertEquals(dto.firstName(), user.getFirstName());
        assertEquals(dto.lastName(), user.getLastName());
        assertEquals(dto.email(), user.getEmail());
        assertEquals(dto.idDocument(), user.getIdDocument());
        assertEquals(dto.phoneNumber(), user.getPhoneNumber());
        assertEquals(dto.baseSalary(), user.getBaseSalary());
        assertEquals(dto.password(), user.getPassword());

        assertNull(user.getId());
        assertNull(user.getIdRole());
    }

    @Test
    @DisplayName("Should map User entity to UserDto correctly")
    void testToResponse() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("Fabricio")
                .lastName("Rodriguez")
                .email("fabricio@example.com")
                .idDocument("77777777")
                .phoneNumber("909090909")
                .idRole(UUID.randomUUID())
                .baseSalary(2500.50)
                .password("password123")
                .build();

        UserDto dto = mapper.toResponse(user);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.id());
        assertEquals(user.getFirstName(), dto.firstName());
        assertEquals(user.getLastName(), dto.lastName());
        assertEquals(user.getEmail(), dto.email());
        assertEquals(user.getIdDocument(), dto.idDocument());
        assertEquals(user.getPhoneNumber(), dto.phoneNumber());
        assertEquals(user.getIdRole(), dto.idRole());
        assertEquals(user.getBaseSalary(), dto.baseSalary());
        assertEquals(user.getPassword(), dto.password());
    }

    @Test
    @DisplayName("Should return null when mapping null values")
    void testNullHandling() {
        assertNull(mapper.toEntity(null));
        assertNull(mapper.toResponse(null));
    }
}
