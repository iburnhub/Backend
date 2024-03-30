package mejai.mejaigg.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import lombok.Data;
import mejai.mejaigg.dto.riot.RankDto;

@Entity
@Data
public class Rank {
	@Id
	@MapsId
	@JoinColumn(name = "puuid")
	private String puuid;

	private String tier; //ex EMERALD
	private String rank; //ex IV :  String ->INT
	private Long leaguePoints;
	private String leagueId;
	private int wins;
	private int losses;
	private boolean hotStreak; //연승 여부
	private boolean veteran; //베테랑 여부
	private boolean freshBlood; //신규 여부
	private boolean inactive; //휴식 여부
	private String queueType; //RANKED_SOLO_5x5

	public void setUnRanked() {
		this.tier = "UNRANKED";
		this.rank = "I";
		this.leaguePoints = 0L;
		this.wins = 0;
		this.losses = 0;
		this.hotStreak = false;
		this.veteran = false;
		this.freshBlood = false;
		this.inactive = false;
	}

	public void updateByRankDto(RankDto rankDto) {
		this.tier = rankDto.getTier();
		this.rank = rankDto.getRank();
		this.leaguePoints = rankDto.getLeaguePoints();
		this.leagueId = rankDto.getLeagueId();
		this.wins = rankDto.getWins();
		this.losses = rankDto.getLosses();
		this.hotStreak = rankDto.isHotStreak();
		this.veteran = rankDto.isVeteran();
		this.freshBlood = rankDto.isFreshBlood();
		this.inactive = rankDto.isInactive();
		this.queueType = rankDto.getQueueType();
	}

}
