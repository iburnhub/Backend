package mejai.mejaigg.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import mejai.mejaigg.dto.UserProfileDto;
import mejai.mejaigg.repository.UserRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	@Transactional
	public UserProfileDto getUserProfile(String name) {
		//이전에 유저가 가입한 적 있는 경우
		// if (userRepository.findBySummonerName(name) != null) {
		// 	return userRepository.findBySummonerName(name);
		// } else {
		//유저가 가입한 적 없는 경우
		//라이엇 API에 요청
		//유저 정보 저장
		//유저 정보 반환
		return null;
		// }
	}
}