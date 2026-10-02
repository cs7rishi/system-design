package Behavioural.ChainOfResponsibility;

public class AuthenticationHandler extends RequestHandler {
    @Override
    public void handle(Request request) {
        if(request.getToken() == null){
            throw new RuntimeException("Authentication failed");
        }

        System.out.println("Authentication Successful");

        handleNext(request);
    }
}
