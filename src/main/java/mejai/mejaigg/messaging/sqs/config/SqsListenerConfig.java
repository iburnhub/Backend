package mejai.mejaigg.messaging.sqs.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsListenerConfig {
	@Bean
	public SqsMessageListenerContainerFactory<Object> sqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
		return SqsMessageListenerContainerFactory.builder()
			.sqsAsyncClient(sqsAsyncClient)
			.configure(options -> options
				.maxConcurrentMessages(10)  // 최대 동시 메시지 처리 수
				.pollTimeout(Duration.ofSeconds(10)))  // 폴링 타임아웃 설정
			.build();
	}
}
