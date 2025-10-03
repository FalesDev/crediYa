package co.com.pragma.api.dto.request;

import java.util.List;
import java.util.UUID;

public record UsersFoundRequest (
        List<UUID> userIds
){
}
