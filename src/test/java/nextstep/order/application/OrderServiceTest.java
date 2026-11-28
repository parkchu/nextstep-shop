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
    void 일요일이_아니면_골드_회원은_5퍼센트_할인된_금액을_결제한다() {
        // given
        LocalDate monday = LocalDate.of(2026, 11, 30);
        Member member = memberRepository.save(aMember().grade(Grade.GOLD).build());
        Product product = productRepository.save(aProduct().price(60_000).build());

        // when
        int orderMoney = orderOneWithoutCouponOn(monday, member, product);

        // then
        assertThat(orderMoney).isEqualTo(57_000);
    }

    @Test
    void 일요일이면_골드_회원은_5퍼센트_할인_후_1000원을_추가로_할인받는다() {
        // given
        LocalDate sunday = LocalDate.of(2026, 11, 29);
        Member member = memberRepository.save(aMember().grade(Grade.GOLD).build());
        Product product = productRepository.save(aProduct().price(60_000).build());

        // when
        int orderMoney = orderOneWithoutCouponOn(sunday, member, product);

        // then
        assertThat(orderMoney).isEqualTo(56_000);
    }

    private int orderOneWithoutCouponOn(LocalDate orderDate, Member member, Product product) {
        try (MockedStatic<LocalDate> dates = mockStatic(LocalDate.class)) {
            dates.when(LocalDate::now).thenReturn(orderDate);
            return orderService.order(member.getId(), List.of(product.getId()), List.of(1), null);
        }
    }
}
