package model;

import java.io.Serializable;
import java.util.Objects;

public class Credit implements Serializable {

    private static final long serialVersionUID = -2565570290688784024L;

    private Integer id;
    private String name;
    private Integer money;
    private Integer month;
    private Integer decimal;
    private Integer summa;
    private Integer payment;

    public Credit(Integer id, String name, Integer money, Integer month, Integer decimal, Integer summa, Integer payment) {
        this.id = id;
        this.name = name;
        this.money = money;
        this.month = month;
        this.decimal = decimal;
        this.summa = summa;
        this.payment = payment;
    }

    public Credit(){

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDecimal() {
        return decimal;
    }

    public void setDecimal(Integer decimal) {
        this.decimal = decimal;
    }

    public Integer getSumma() {
        return summa;
    }

    public void setSumma(Integer summa) {
        this.summa = summa;
    }

    public Integer getPayment() {
        return payment;
    }

    public void setPayment(Integer payment) {
        this.payment = payment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Credit)) return false;
        Credit credit = (Credit) o;
        return id.equals(credit.id) &&
                name.equals(credit.name) &&
                money.equals(credit.money) &&
                month.equals(credit.month) &&
                decimal.equals(credit.decimal) &&
                summa.equals(credit.summa) &&
                payment.equals(credit.payment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, money, month, decimal, summa, payment);
    }

    @Override
    public String toString() {
        return "Credit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", money=" + money +
                ", month=" + month +
                ", decimal=" + decimal +
                ", summa=" + summa +
                ", payment=" + payment +
                '}';
    }
}
