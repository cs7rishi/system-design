package Behavioural.ChainOfResponsibility;

public class ValidationHandler extends RequestHandler {
    @Override
    public void handle(Request request) {
        if(request.getBody() == null || request.getBody().isEmpty()){
            throw new RuntimeException("Validation Failed");
        }

        System.out.println("Validation Successful");
        handleNext(request);
    }
}
