package nextstep.fixture;

import nextstep.coupon.domain.Coupon;

/**
 * 테스트용 쿠폰 빌더. 쿠폰 종류의 숫자 코드는 빌더 안에 숨긴다.
 * 쿠폰 종류의 표현이 바뀌어도 이 클래스만 고치면 된다.
 * <pre>
 * Coupon fixed = aCoupon().fixedAmount(6_000).build();   // 정액
 * Coupon rate  = aCoupon().rate(10).build();             // 정률 10%
 * </pre>
 */
public class CouponBuilder {

    private static final int FIXED_AMOUNT = 1;
    private static final int FIXED_RATE = 2;
    private static final int FREE_SHIPPING = 3;

    private String code = "COUPON-001";
    private int type = FIXED_AMOUNT;
    private int amount = 1_000;
    private int rate = 0;

    public static CouponBuilder aCoupon() {
        return new CouponBuilder();
    }

    public CouponBuilder code(String code) {
        this.code = code;
        return this;
    }

    public CouponBuilder fixedAmount(int amount) {
        this.type = FIXED_AMOUNT;
        this.amount = amount;
        this.rate = 0;
        return this;
    }

    public CouponBuilder rate(int rate) {
        this.type = FIXED_RATE;
        this.rate = rate;
        this.amount = 0;
        return this;
    }

    public CouponBuilder freeShipping() {
        this.type = FREE_SHIPPING;
        this.amount = 0;
        this.rate = 0;
        return this;
    }

    public Coupon build() {
        return new Coupon(code, type, amount, rate);
    }
}
