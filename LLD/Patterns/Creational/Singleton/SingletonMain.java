package Creational.Singleton;

public class SingletonMain {
    public static void main(String[] args) {
        ConnectionPool connectionPool = ConnectionPool.getInstance();
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    }
}
