package nextstep.order.application;

import lombok.RequiredArgsConstructor;
import nextstep.coupon.domain.Coupon;
import nextstep.coupon.infra.CouponRepository;
import nextstep.member.domain.Member;
import nextstep.member.infra.MemberRepository;
import nextstep.order.infra.EmailSender;
import nextstep.order.infra.PaymentClient;
import nextstep.product.domain.Product;
import nextstep.product.infra.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public int order(Long memberId, List<Long> productIds, List<Integer> quantities, String couponCode) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        int total = 0;
        for (int i = 0; i < productIds.size(); i++) {
            Product product = productRepository.findById(productIds.get(i));
            int quantity = quantities.get(i);
            product.validateStockFor(quantity);
            total += product.subtotalFor(quantity);
        }
        if (member.getGrade().name().equals("VIP")) total = (int) (total * 0.9);
        else if (member.getGrade().name().equals("GOLD")) total = (int) (total * 0.95);

        Coupon coupon = couponRepository.findByCode(couponCode);
        if (coupon != null) {
            total = coupon.applyDiscountTo(total);
        }
        if (requiresShippingFee(total, member, coupon)) total += 3000;
        if (LocalDate.now().getDayOfWeek() == DayOfWeek.SUNDAY) total -= 1000;

        PaymentClient.pay(member.getCardNo(), total);   // static 외부 호출
        for (int i = 0; i < productIds.size(); i++) {   // 재고 차감
            Product product = productRepository.findById(productIds.get(i));
            product.decreaseStock(quantities.get(i));
        }
        EmailSender.send(member.getEmail(), "주문 완료: " + total);
        return total;
    }

    private boolean requiresShippingFee(int total, Member member, Coupon coupon) {
        if (total >= 50000)
            return false;
        if (member.getGrade().name().equals("VIP"))
            return false;
        if (coupon != null && coupon.getType() == 3)
            return false;
        return true;
    }
}
