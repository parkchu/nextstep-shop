package nextstep.product.domain;

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
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;

    private int stock;

    public Product(String name, int price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public void validateStockFor(int quantity) {
        if (stock < quantity) {
            throw new RuntimeException("재고 부족");
        }
    }

    public int subtotalFor(int quantity) {
        return price * quantity;
    }

    public void decreaseStock(int quantity) {
        stock -= quantity;
    }
}
