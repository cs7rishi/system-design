package Behavioural.State;

public class Order {
    private OrderState state;

    public Order(){
        this.state = new CreatedState();
    }

    public void setState(OrderState state){
        this.state = state;
    }

    public void pay(){
        state.pay(this);
    }

    public void ship(){
        state.ship(this);
    }

    public void cancel(){
        state.cancel(this);
    }

}
