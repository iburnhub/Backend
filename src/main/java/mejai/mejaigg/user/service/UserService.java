package mejai.mejaigg.user.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejai.mejaigg.common.util.YearMonthToEpochUtil;
import mejai.mejaigg.match.entity.Match;
import mejai.mejaigg.match.repository.MatchRepository;
import mejai.mejaigg.matchDateStreak.entity.MatchDateStreak;
import mejai.mejaigg.matchDateStreak.repository.MatchDateStreakRepository;
import mejai.mejaigg.rank.dto.RankDto;
import mejai.mejaigg.rank.entity.Rank;
import mejai.mejaigg.rank.mapper.RankMapper;
import mejai.mejaigg.rank.repository.RankRepository;
import mejai.mejaigg.riot.dto.AccountDto;
import mejai.mejaigg.riot.dto.SummonerDto;
import mejai.mejaigg.riot.service.RiotService;
import mejai.mejaigg.searchHistory.entity.SearchHistory;
import mejai.mejaigg.searchHistory.repository.SearchHistoryRepository;
import mejai.mejaigg.user.dto.response.UserProfileDto;
import mejai.mejaigg.user.dto.response.UserStreakDto;
import mejai.mejaigg.user.entity.User;
import mejai.mejaigg.user.mapper.UserMapper;
import mejai.mejaigg.user.repository.UserRepository;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

	private final RiotService riotService;
	private final UserRepository userRepository;
	private final RankRepository rankRepository;
	private final SearchHistoryRepository searchHistoryRepository;
	private final MatchDateStreakRepository matchDateStreakRepository;
	private final MatchRepository matchRepository;

	@Value("${variables.resourceURL:https://ddragon.leagueoflegends.com/cdn/11.16.1/img/profileicon/}")
	private String resourceURL;

	//TODO : api 콜을 실패하는 경우 고려해야함
	//TODO : 비동기 레포지토리 방식 적용
	//TODO : 시즌 바꼈을때 추가하는 로직 필요하다.
	//처음으로 요청이 들어왔을 때 호출되는 서비스
	@Transactional(readOnly = false)
	public String setUserProfile(String name, String tag) { //처음 콜 할 때 세팅 되는 함수
		AccountDto accountDto = riotService.getAccountByNameAndTag(name, tag);
		SummonerDto summonerDto = riotService.getSummonerByPuuid(accountDto.getPuuid());

		User user = UserMapper.INSTANCE.toUserEntity(accountDto, summonerDto);

		Set<RankDto> rankDtos = riotService.getRankBySummonerId(summonerDto.getId());
		//TODO : Rank 자유랭크 일반랭크
		RankDto rankDto = rankDtos.stream().filter(rank -> rank.getQueueType().equals("RANKED_SOLO_5x5")).findFirst()
			.orElse(null);
		Rank rank = new Rank();

		if (rankDto != null) { //랭크가 없는 경우에는 배열이 비었다. (언랭 유저)
			rank = RankMapper.INSTANCE.toRankEntity(rankDto);
		} else {
			rank.setUnRanked();
		}
		rank.setUser(user);
		rankRepository.save(rank);
		user.setRank(rank);
		userRepository.save(user);
		return user.getPuuid();
	}

	public String getUserPuuidByApi(String name, String tag) {
		try {
			AccountDto account = riotService.getAccountByNameAndTag(name, tag);
			return account.getPuuid();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "summoner not found");
		}
	}

	@Transactional(readOnly = false)
	public String updateUserProfile(User user) {
		SummonerDto summoner = riotService.getSummonerByPuuid(user.getPuuid());
		Set<RankDto> rankDtos = riotService.getRankBySummonerId(summoner.getId());
		//TODO : Rank 자유랭크 일반랭크 저장 다 하기
		RankDto rankDto = rankDtos.stream().filter(rank -> rank.getQueueType().equals("RANKED_SOLO_5x5")).findFirst()
			.orElse(null);
		user.updateBySummonerDto(summoner);
		user.getRank().updateByRankDto(rankDto);
		return user.getPuuid();
	}

	@Transactional(readOnly = false)
	public Optional<List<UserStreakDto>> getUserMonthStreak(String puuid, int year, int month) {
		String dateYM = String.format("%d-%02d", year, month);
		Optional<User> userOptional = userRepository.findById(puuid);
		if (userOptional.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "summoner not found");
		}
		User user = userOptional.get();
		SearchHistory history = getHistory(user, dateYM);
		if (history.isDone() || isEmptyHistory(dateYM, puuid)) {
			return Optional.of(getUserStreakDtoList(history));
		}
		//mathData 저장
		saveStreakData(history, dateYM, puuid);
		userRepository.save(user);
		return Optional.of(getUserStreakDtoList(history));
	}

	private boolean isEmptyHistory(String dateYM, String puuid) {
		long startTime = YearMonthToEpochUtil.convertToEpochSeconds(dateYM);
		long endTime = YearMonthToEpochUtil.addMonthEpochSecond(dateYM, 1);
		if (dateYM.equals(YearMonthToEpochUtil.getNowYearMonth())) { // 현재 월에 해당하는 경우 현재 날짜까지만 가져옴
			endTime = YearMonthToEpochUtil.getNowEpochSecond();
		}
		String[] monthHistories = riotService.getMatchHistoryByPuuid(puuid, startTime, endTime, 0L,
			100); //100개의 매치를 가져옴
		return monthHistories == null || monthHistories.length == 0;
	}

	private void saveStreakData(SearchHistory history, String dateYM, String puuid) {
		int startDay = history.getLastSuccessDay();
		int endDay = YearMonthToEpochUtil.getDayWithYearMonth(dateYM);
		for (int i = startDay; i < endDay; i++) { // 하루씩 데이터를 가져옴
			if (matchDateStreakRepository.findByDateAndSearchHistory(
				new Date(YearMonthToEpochUtil.addDayEpochSecond(dateYM, i)),
				history.getHistoryId()).isPresent()) {
				continue;
			}
			long startTime = YearMonthToEpochUtil.addDayEpochSecond(dateYM, i);
			long endTime = YearMonthToEpochUtil.addDayEpochSecond(dateYM, i + 1);
			try {

				String[] matchHistory = riotService.getMatchHistoryByPuuid(puuid, startTime, endTime, 0L,
					100); //하루 지난 후 100개의 매치를 가져옴
				if (matchHistory == null || matchHistory.length == 0) {
					continue;
				}
				MatchDateStreak matchDateStreak = new MatchDateStreak();
				Date date = new Date(YearMonthToEpochUtil.addDayEpochSecond(dateYM, i) * 1000L);
				matchDateStreak.setDate(date);
				history.addMatchDateStreak(matchDateStreak);
				for (String matchId : matchHistory) {
					Optional<Match> optionalMatch = matchRepository.findById(matchId);
					Match match = new Match(matchId, false);
					if (optionalMatch.isPresent()) {
						match = optionalMatch.get();
					}
					matchDateStreak.addMatch(match);
				}
				matchDateStreakRepository.save(matchDateStreak);
			} catch (Exception e) {
				log.error("Http Error" + e.getMessage());
				searchHistoryRepository.updateLastSuccessDateByHistoryId(history.getHistoryId(), i);
				return;
			}
		}
		if (!dateYM.equals(YearMonthToEpochUtil.getNowYearMonth())) {
			searchHistoryRepository.updateIsDoneByHistoryId(history.getHistoryId(), true);
			searchHistoryRepository.updateLastSuccessDateByHistoryId(history.getHistoryId(),
				YearMonthToEpochUtil.getNowDay());
		} else {
			searchHistoryRepository.updateLastSuccessDateByHistoryId(history.getHistoryId(),
				31);
		}
	}

	private SearchHistory getHistory(User user, String dateYM) {
		Optional<SearchHistory> searchHistory = searchHistoryRepository.findByUserAndYearMonth(user, dateYM);
		SearchHistory history = new SearchHistory();
		if (searchHistory.isEmpty()) {
			history.setYearMonthAndUser(dateYM, user);
			searchHistoryRepository.save(history);
		} else {
			history = searchHistory.get();
		}
		return history;
	}

	// private void extracted(String[] monthIds, int days, SearchHistory history) {
	// 	if (monthIds.length < days) { // 갯수가 하루씩 부르는 것보다 더 적은 경우 하나씩 데이터를 가져옴
	// 		for (String matchId : monthIds) {
	// 			Match match = new Match(matchId, true);
	// 			Mono<MatchDto> matchData = apiService.getMatchDtoByMatchId(matchId);
	// 			MatchDto matchDto = matchData.block();
	//
	// 			InfoDto info = matchDto.getInfo();
	// 			Game gameEntity = GameMapper.INSTANCE.toGameEntity(info, matchId);
	// 			ParticipantDto[] participants = matchDto.getInfo().getParticipants();
	// 			for (ParticipantDto participant : participants) {
	// 				UserGameStat userGameStat = new UserGameStat();
	// 				userGameStat.setByParticipantDto(participant);
	// 				userGameStat.setGame(gameEntity);
	// 				gameEntity.addGameStat(userGameStat);
	// 			}
	// 			match.setGame(gameEntity);
	// 			gameRepository.save(gameEntity);
	//
	// 			String yearMonth = YearMonthToEpochUtil.convertToYearMonthDay(info.getGameCreation());
	// 			Date matchDate = new Date(info.getGameCreation());
	// 			Optional<MatchDateStreak> streakOptional = matchDateStreakRepository.findByDateAndSearchHistory(
	// 				matchDate, history.getHistoryId());
	// 			if (streakOptional.isEmpty()) {
	// 				MatchDateStreak matchDateStreak = new MatchDateStreak();
	// 				matchDateStreak.setDate(matchDate);
	// 				history.addMatchDateStreak(matchDateStreak);
	//
	// 				matchDateStreakRepository.save(matchDateStreak);
	// 				// gameRepository.save(gameEntity);
	// 				matchRepository.save(match);
	// 				// searchHistoryRepository.save(history);
	// 				// gameRepository.save(gameEntity);
	// 			} else {
	// 				MatchDateStreak matchDateStreak = streakOptional.get();
	// 				history.addMatchDateStreak(matchDateStreak);
	// 				match.setGame(gameEntity);
	// 				matchDateStreakRepository.save(matchDateStreak);
	// 				matchRepository.save(match);
	// 				gameRepository.save(gameEntity);
	// 				userGameStatRepository.saveAll(gameEntity.getGameStats());
	// 			}
	// 			match.setGame(gameEntity);
	// 			gameRepository.save(gameEntity);
	// 		}
	// 	}
	// }

	private List<UserStreakDto> getUserStreakDtoList(SearchHistory history) {
		Set<MatchDateStreak> matchDateStreaks = history.getSortedMatchDateStreaks();
		List<UserStreakDto> userStreakDtos = new ArrayList<>();
		for (MatchDateStreak matchDateStreak : matchDateStreaks) {
			UserStreakDto userStreakDto = new UserStreakDto();
			userStreakDto.setByMatchDateStreak(matchDateStreak, resourceURL);
			userStreakDtos.add(userStreakDto);
		}
		return userStreakDtos;
	}

	@Transactional
	public String getPuuidByNameTag(String name, String tag) {
		Optional<User> userOptional = userRepository.findBySummonerNameAndTagLineAllIgnoreCase(name, tag);
		if (userOptional.isEmpty()) {
			return getUserPuuidByApi(name, tag);
		} else {
			return userOptional.get().getPuuid();
		}
	}

	@Transactional
	public UserProfileDto getUserProfileByNameTag(String name, String tag) {
		Optional<User> userOptional = userRepository.findBySummonerNameAndTagLineAllIgnoreCase(name, tag);
		if (userOptional.isEmpty()) {
			try {
				String puuid = setUserProfile(name, tag);
				if (puuid == null) {
					throw new ResponseStatusException(HttpStatus.NOT_FOUND, "summoner not found");
				}
				userOptional = userRepository.findById(puuid);
			} catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "summoner not found");
			}
		} else {
			updateUserProfile(userOptional.get());
		}
		UserProfileDto userProfileDto = new UserProfileDto();
		if (userOptional.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "summoner not found");
		}
		User user = userOptional.get();
		userProfileDto.setByUser(user, resourceURL);
		return userProfileDto;
	}
}
