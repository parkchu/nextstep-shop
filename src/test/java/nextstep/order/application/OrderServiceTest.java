package nextstep.order.application;

import nextstep.coupon.domain.Coupon;
import nextstep.fake.FakeCouponRepository;
import nextstep.fake.FakeMemberRepository;
import nextstep.fake.FakeProductRepository;
import nextstep.member.domain.Grade;
import nextstep.member.domain.Member;
import nextstep.order.infra.EmailSender;
import nextstep.order.infra.PaymentClient;
import nextstep.product.domain.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.List;

import static nextstep.fixture.CouponBuilder.aCoupon;
import static nextstep.fixture.MemberBuilder.aMember;
import static nextstep.fixture.ProductBuilder.aProduct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void 일요일이_아니면_골드_회원의_주문을_5퍼센트_할인된_금액으로_완료한다() {
        // given
        LocalDate monday = LocalDate.of(2026, 11, 30);
        Member member = memberRepository.save(aMember().grade(Grade.GOLD).build());
        Product product = productRepository.save(aProduct().price(60_000).stock(10).build());

        try (MockedStatic<PaymentClient> payments = mockStatic(PaymentClient.class);
             MockedStatic<EmailSender> emails = mockStatic(EmailSender.class)) {
            // when
            int orderMoney = orderOneOn(monday, member, product, null);

            // then
            assertThat(orderMoney).isEqualTo(57_000);
            payments.verify(() -> PaymentClient.pay(member.getCardNo(), 57_000));
            payments.verifyNoMoreInteractions();
            assertThat(productRepository.findById(product.getId()).getStock()).isEqualTo(9);
            emails.verify(() -> EmailSender.send(member.getEmail(), "주문 완료: 57000"));
            emails.verifyNoMoreInteractions();
        }
    }

    @Test
    void 재고가_부족한_상품이_있으면_결제나_재고_차감_없이_주문이_실패한다() {
        // given
        Member member = memberRepository.save(aMember().build());
        Product firstProduct = productRepository.save(aProduct().stock(10).build());
        Product secondProduct = productRepository.save(aProduct().stock(1).build());

        try (MockedStatic<PaymentClient> payments = mockStatic(PaymentClient.class);
             MockedStatic<EmailSender> emails = mockStatic(EmailSender.class)) {
            // when & then
            assertThatThrownBy(() -> orderService.order(
                    member.getId(), List.of(firstProduct.getId(), secondProduct.getId()), List.of(1, 2), null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("재고 부족");

            assertThat(productRepository.findById(firstProduct.getId()).getStock()).isEqualTo(10);
            assertThat(productRepository.findById(secondProduct.getId()).getStock()).isEqualTo(1);
            payments.verifyNoInteractions();
            emails.verifyNoInteractions();
        }
    }

    @Test
    void 일요일이면_골드_회원은_5퍼센트_할인_후_1000원을_추가로_할인받는다() {
        // given
        LocalDate sunday = LocalDate.of(2026, 11, 29);
        Member member = memberRepository.save(aMember().grade(Grade.GOLD).build());
        Product product = productRepository.save(aProduct().price(60_000).build());

        try (MockedStatic<PaymentClient> ignoredPayments = mockStatic(PaymentClient.class);
             MockedStatic<EmailSender> ignoredEmails = mockStatic(EmailSender.class)) {
            // when
            int orderMoney = orderOneOn(sunday, member, product, null);

            // then
            assertThat(orderMoney).isEqualTo(56_000);
        }
    }

    @ParameterizedTest(name = "정액 쿠폰 {0}원 적용 시 결제액은 {1}원이다")
    @CsvSource({"8000, 52000", "7000, 50000"})
    void 정액_쿠폰은_등급_할인_후_적용하고_할인된_금액으로_배송비를_판단한다(int discountAmount, int expectedTotal) {
        // given
        LocalDate monday = LocalDate.of(2026, 11, 30);
        Member member = memberRepository.save(aMember().grade(Grade.GOLD).build());
        Product product = productRepository.save(aProduct().price(60_000).build());
        Coupon coupon = couponRepository.save(aCoupon().fixedAmount(discountAmount).build());

        try (MockedStatic<PaymentClient> ignoredPayments = mockStatic(PaymentClient.class);
             MockedStatic<EmailSender> ignoredEmails = mockStatic(EmailSender.class)) {
            // when
            int orderMoney = orderOneOn(monday, member, product, coupon.getCode());

            // then
            assertThat(orderMoney).isEqualTo(expectedTotal);
        }
    }

    @Test
    void 정률_쿠폰은_할인액의_원_미만을_버린다() {
        // given
        LocalDate monday = LocalDate.of(2026, 11, 30);
        Member member = memberRepository.save(aMember().grade(Grade.NORMAL).build());
        Product product = productRepository.save(aProduct().price(60_003).build());
        Coupon coupon = couponRepository.save(aCoupon().rate(10).build());

        try (MockedStatic<PaymentClient> ignoredPayments = mockStatic(PaymentClient.class);
             MockedStatic<EmailSender> ignoredEmails = mockStatic(EmailSender.class)) {
            // when
            int orderMoney = orderOneOn(monday, member, product, coupon.getCode());

            // then
            assertThat(orderMoney).isEqualTo(54_003);
        }
    }

    private int orderOneOn(LocalDate orderDate, Member member, Product product, String couponCode) {
        try (MockedStatic<LocalDate> dates = mockStatic(LocalDate.class)) {
            dates.when(LocalDate::now).thenReturn(orderDate);
            return orderService.order(member.getId(), List.of(product.getId()), List.of(1), couponCode);
        }
    }
}
