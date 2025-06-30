package upeu.edu.pe.auth_service.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "user_info")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String whatsapp;

    public UserEntity() {
    }

    public UserEntity(Long id, String username, String password, String whatsapp) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.whatsapp = whatsapp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public static class Builder {
        private Long id;
        private String username;
        private String password;
        private String whatsapp;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder whatsapp(String whatsapp) {
            this.whatsapp = whatsapp;
            return this;
        }


        public UserEntity build() {
            return new UserEntity(id, username, password, whatsapp);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}