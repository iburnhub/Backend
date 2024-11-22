package mejai.mejaigg.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import mejai.mejaigg.global.interceptor.JwtInterceptor;

@RequiredArgsConstructor
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

	private final JwtInterceptor jwtInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(jwtInterceptor)
			.addPathPatterns("/app/**"); // 인터셉터를 적용할 경로 패턴
	}
}
