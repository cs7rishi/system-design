package Creational.Builder;

public class User {

    private String name;
    private String email;
    private int age;
    private String phone;
    private String address;
    private boolean admin;

    private User(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
        this.phone = builder.phone;
        this.address = builder.address;
        this.admin = builder.admin;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", admin=" + admin +
                '}';
    }

    public static class Builder{
        private String name;
        private String email;
        private int age;
        private String phone;
        private String address;
        private boolean admin;

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder age(int age){
            this.age = age;
            return this;
        }   

        public Builder phone(String phone){
            this.phone = phone;
            return this;
        }

        public Builder address(String address){
            this.address = address;
            return this;
        }
        public Builder email(String email){
            this.email = email;
            return this;
        }
        public Builder admin(boolean admin){
            this.admin = admin;
            return this;
        }

        public User build(){
            return new User(this);
        }
    }

}