package mejai.mejaigg.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;

@Entity
@Getter
public class Game {

	@Id
	private String matchId;

	@OneToOne
	@MapsId //FK to PK
	@JoinColumn(name = "matchId")
	private Match match;

	private Long gameCreation;
	private Long gameDuration;
	private Long gameEndTimestamp;
	private Long gameId;
	private String gameMode;
	private String gameName;
	private Long gameStartTimestamp;
	private String gameType;
	private String gameVersion;
	private int mapId;
	private String platformId;
	private int queueId;
	private String tournamentCode;

	@OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
	private List<UserGameStat> gameStats = new ArrayList<>();

	public void addGameStat(UserGameStat userGameStat) {
		gameStats.add(userGameStat);
		userGameStat.setGame(this);
	}

	public void setMatchId(String matchId) {
		this.matchId = matchId;
	}
}
