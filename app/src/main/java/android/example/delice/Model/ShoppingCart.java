package android.example.delice.Model;

public class ShoppingCart {
    public String ingredient;
    public String amount;

    public ShoppingCart(String ingredient, String amount) {
        this.ingredient = ingredient;
        this.amount = amount;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "ShoppingCart{" +
                "ingredient='" + ingredient + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}