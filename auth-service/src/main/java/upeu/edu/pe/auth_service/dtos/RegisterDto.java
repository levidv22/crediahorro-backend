package upeu.edu.pe.auth_service.dtos;

public class RegisterDto {

    private String username;
    private String password;
    private String whatsapp;
    private String email;
    private String role;

    public RegisterDto() {
    }

    public RegisterDto(String username, String password, String whatsapp, String email, String role) {
        this.username = username;
        this.password = password;
        this.whatsapp = whatsapp;
        this.email = email;
        this.role = role;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static class Builder {

        private String username;
        private String password;
        private String whatsapp;
        private String email;
        private String role;


        public RegisterDto.Builder username(String username) {
            this.username = username;
            return this;
        }

        public RegisterDto.Builder password(String password) {
            this.password = password;
            return this;
        }

        public RegisterDto.Builder whatsapp(String whatsapp) {
            this.whatsapp = whatsapp;
            return this;
        }

        public RegisterDto.Builder email(String email) {
            this.email = email;
            return this;
        }

        public RegisterDto.Builder role(String role) {
            this.role = role;
            return this;
        }


        public RegisterDto build() {
            return new RegisterDto(username, password, whatsapp, email, role);
        }
    }

    public static RegisterDto.Builder builder() {
        return new RegisterDto.Builder();
    }
}
