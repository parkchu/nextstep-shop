package nextstep.coupon.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    private static final int FIXED_AMOUNT = 1;
    private static final int FIXED_RATE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private int type;

    private int amount;

    private int rate;

    public Coupon(String code, int type, int amount, int rate) {
        this.code = code;
        this.type = type;
        this.amount = amount;
        this.rate = rate;
    }

    public int applyDiscountTo(int subtotal) {
        return switch (type) {
            case FIXED_AMOUNT -> subtotal - amount;
            case FIXED_RATE -> subtotal - subtotal * rate / 100;
            default -> subtotal;
        };
    }
}
