package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	/**
	 * Order의 Member와 OrderItem, Delivery는 지연로딩이다.
	 * 따라서 Order를 조회하게 되면 실제 엔티티 대신 프록시 객체를 Injection한다.
	 * jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성하는지 모르기 때문에 예외를 발생시킨다.
	 * Hibernate5Module을 스프링 빈으로 등록하면 해결된다.
	 * @return
	 */
	@Bean
	Hibernate5Module hibernate5Module() {
		Hibernate5Module hibernate5Module = new Hibernate5Module();
		/**
		 * 강제 지연로딩이 가능해진다. (연관 엔터티가 노출되고 성능에 영향을 준다.)
		 * 설정하지 않으면 LazyLoading이 동작하지 않아 연관관계 엔티티값을 null로 가져온다.
		 */
//		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return hibernate5Module;
	}


}
