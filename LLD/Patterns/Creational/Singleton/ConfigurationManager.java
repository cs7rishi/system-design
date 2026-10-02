package Creational.Singleton;

public class ConfigurationManager {

    private ConfigurationManager(){}

    public static ConfigurationManager getInstance(){
        return Holder.INSTANCE;
    }

    public String getConfigValue(String key){
        return "Value for " + key;
    }

    private static class Holder{
        private static final ConfigurationManager INSTANCE = new ConfigurationManager();
    }
}
