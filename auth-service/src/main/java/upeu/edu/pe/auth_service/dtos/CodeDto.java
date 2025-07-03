package upeu.edu.pe.auth_service.dtos;

public class CodeDto {
    private String username;
    private String accessCode;

    public CodeDto() {
    }

    public CodeDto(String username, String accessCode) {
        this.username = username;
        this.accessCode = accessCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public static class Builder {

        private String username;
        private String accessCode;


        public CodeDto.Builder username(String username) {
            this.username = username;
            return this;
        }

        public CodeDto.Builder accessCode(String accessCode) {
            this.accessCode = accessCode;
            return this;
        }

        public CodeDto build() {
            return new CodeDto(username, accessCode);
        }
    }

    public static CodeDto.Builder builder() {
        return new CodeDto.Builder();
    }
}
