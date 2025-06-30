package upeu.edu.pe.auth_service.dtos;

public class RegisterDto {

    private String username;
    private String password;
    private String whatsapp;

    public RegisterDto() {
    }

    public RegisterDto(String username, String password, String whatsapp) {
        this.username = username;
        this.password = password;
        this.whatsapp = whatsapp;
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

        private String username;
        private String password;
        private String whatsapp;


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


        public RegisterDto build() {
            return new RegisterDto(username, password, whatsapp);
        }
    }

    public static RegisterDto.Builder builder() {
        return new RegisterDto.Builder();
    }
}
