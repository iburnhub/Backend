package mejai.mejaigg.app.user.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mejai.mejaigg.app.user.dto.LoginRequest;
import mejai.mejaigg.app.user.dto.LoginResponse;
import mejai.mejaigg.app.user.service.UserService;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"*"})
@Tag(name = "User", description = "앱 회원관련 기능")
@RequestMapping("/app/user")
public class UserController {
	private final UserService userService;

	@PostMapping("/login")
	@Operation(summary = "앱 로그인", description = "회원이라면 로그인하고, 아니라면 회원가입 합니다. \n카카오: kakao\n구글: google")
	public LoginResponse login(@RequestBody LoginRequest loginRequest) {
		return userService.loginOrSignUp(loginRequest.getSocialId(), loginRequest.getSocialType());
	}
}