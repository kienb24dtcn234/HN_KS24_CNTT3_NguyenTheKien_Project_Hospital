package re.hospital.model.dto.response;

import lombok.*;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JWTResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private String fullName;
    private String username;
    private Set<String> roles;
}
