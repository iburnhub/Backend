package mejai.mejaigg.matchstreak.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mejai.mejaigg.global.exception.ErrorCode;

@Getter
@AllArgsConstructor
public enum StreakErrorCode implements ErrorCode {
	STREAK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 달의 스트릭을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}