package nextstep.order.application;

import nextstep.fake.FakeCouponRepository;
import nextstep.fake.FakeMemberRepository;
import nextstep.fake.FakeProductRepository;
import nextstep.member.domain.Grade;
import nextstep.member.domain.Member;
import nextstep.product.domain.Product;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.List;

import static nextstep.fixture.MemberBuilder.aMember;
import static nextstep.fixture.ProductBuilder.aProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

/**
 * 레거시 OrderService의 현재 동작을 고정하는 테스트(특성 테스트).
 * 아래 예제를 복사해 Given의 값만 바꿔 가며 케이스를 추가한다.
 */
class OrderServiceTest {

    private final FakeMemberRepository memberRepository = new FakeMemberRepository();
    private final FakeProductRepository productRepository = new FakeProductRepository();
    private final FakeCouponRepository couponRepository = new FakeCouponRepository();
    private final OrderService orderService = new OrderService(memberRepository, productRepository, couponRepository);

    @Test
    void 일요일이_아니면_골드_회원_할인만_적용된다() {
        // given
        Member member = memberRepository.save(aMember().grade(Grade.GOLD).build());
        Product product = productRepository.save(aProduct().name("코트").price(60_000).build());
        LocalDate monday = LocalDate.of(2026, 2, 2);

        // when
        int orderMoney = 지정한_날짜에_주문한다(monday, member, product);

        // then: 60,000 × 0.95 = 57,000. 50,000원 이상이라 배송비 없음
        assertThat(orderMoney).isEqualTo(57_000);
    }

    @Test
    void 일요일에는_골드_회원_할인에_1000원_할인이_추가된다() {
        // given
        Member member = memberRepository.save(aMember().grade(Grade.GOLD).build());
        Product product = productRepository.save(aProduct().name("코트").price(60_000).build());
        LocalDate sunday = LocalDate.of(2026, 2, 1);

        // when
        int orderMoney = 지정한_날짜에_주문한다(sunday, member, product);

        // then: 60,000 × 0.95 - 1,000 = 56,000
        assertThat(orderMoney).isEqualTo(56_000);
    }

    private int 지정한_날짜에_주문한다(LocalDate date, Member member, Product product) {
        try (MockedStatic<LocalDate> mockedDate = mockStatic(LocalDate.class)) {
            mockedDate.when(LocalDate::now).thenReturn(date);
            return orderService.order(member.getId(), List.of(product.getId()), List.of(1), null);
        }
    }
}
