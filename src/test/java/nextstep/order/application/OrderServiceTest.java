package nextstep.order.application;

import nextstep.fake.FakeCouponRepository;
import nextstep.fake.FakeMemberRepository;
import nextstep.fake.FakeProductRepository;
import nextstep.member.domain.Grade;
import nextstep.member.domain.Member;
import nextstep.product.domain.Product;
import org.junit.jupiter.api.Test;

import java.util.List;

import static nextstep.fixture.MemberBuilder.aMember;
import static nextstep.fixture.ProductBuilder.aProduct;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 레거시 OrderService의 현재 동작을 고정하는 테스트(특성 테스트).
 * 아래 예제를 복사해 Given의 값만 바꿔 가며 케이스를 추가한다.
 */
class OrderServiceTest {

    private final FakeMemberRepository memberRepository = new FakeMemberRepository();
    private final FakeProductRepository productRepository = new FakeProductRepository();
    private final FakeCouponRepository couponRepository = new FakeCouponRepository();
    private final OrderService orderService = new OrderService(memberRepository, productRepository, couponRepository);

    // 주의: 이 테스트는 일요일에 실행하면 실패한다. 왜일까? (OrderService.order()를 읽어 보자)
    @Test
    void 골드_회원은_상품_금액에서_5퍼센트_할인된_금액을_결제한다() {
        // given
        Member member = memberRepository.save(aMember().grade(Grade.GOLD).build());
        Product product = productRepository.save(aProduct().name("코트").price(60_000).build());

        // when
        int orderMoney = orderService.order(member.getId(), List.of(product.getId()), List.of(1), null);

        // then
        // 60,000 × 0.95 = 57,000. 50,000원 이상이라 배송비 없음
        assertThat(orderMoney).isEqualTo(57_000);
        // 결제·메일 발송은 static 호출이라 테스트에서 확인할 수 없다. 지금은 반환값만 검증한다.
    }
}
