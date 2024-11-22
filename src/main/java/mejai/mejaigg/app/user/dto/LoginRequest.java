package mejai.mejaigg.app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import mejai.mejaigg.app.user.domain.SocialType;

@Data
@Getter
public class LoginRequest {
	@Schema(description = "소셜 ID", example = "1")
	private String socialId;

	@Schema(description = "소셜 타입"
		+ "카카오: kakao"
		+ "구글: google",
		example = "kakao")
	private SocialType socialType;
}