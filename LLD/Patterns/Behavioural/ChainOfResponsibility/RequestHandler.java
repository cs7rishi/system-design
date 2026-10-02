package Behavioural.ChainOfResponsibility;

public abstract class RequestHandler {
    protected RequestHandler nextHandler;

    public RequestHandler setNext(RequestHandler nextHandler){
        this.nextHandler = nextHandler;
        return nextHandler;
    }

    public abstract void handle(Request request);

    protected void handleNext(Request request){
        if(nextHandler != null){
            nextHandler.handle(request);
        }
    }
}
