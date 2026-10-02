package Creational.Singleton;

public class ConnectionPool {
    private static volatile ConnectionPool instance;

    private ConnectionPool(){

    }

    public static ConnectionPool getInstance(){
        if(instance == null){
            synchronized (ConnectionPool.class){
                if(instance == null){
                    instance = new ConnectionPool();
                }
            }
        }
        return instance;
    }
}
