package mejai.mejaigg.app.user.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import mejai.mejaigg.app.jwt.JwtProvider;
import mejai.mejaigg.app.user.domain.AppUser;
import mejai.mejaigg.app.user.domain.SocialType;
import mejai.mejaigg.app.user.dto.LoginResponse;
import mejai.mejaigg.app.user.repository.AppUserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

	private final AppUserRepository appUserRepository;
	private final JwtProvider jwtProvider;

	public LoginResponse loginOrSignUp(String socialId, SocialType socialType) {
		AppUser appUser = appUserRepository.findAppUserBySocialIdAndSocialType(socialId, socialType)
			.orElseGet(() -> appUserRepository.save(new AppUser(socialId, socialType)));

		return new LoginResponse(jwtProvider.generateToken(appUser.getId()));
	}
}
