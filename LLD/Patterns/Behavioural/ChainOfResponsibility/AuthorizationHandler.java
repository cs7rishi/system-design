package Behavioural.ChainOfResponsibility;

public class AuthorizationHandler extends RequestHandler {

    @Override
    public void handle(Request request) {

        if (!"ADMIN".equals(request.getRole())) {
            throw new RuntimeException("Authorization failed");
        }

        System.out.println("Authorization successful");

        handleNext(request);
    }
}
