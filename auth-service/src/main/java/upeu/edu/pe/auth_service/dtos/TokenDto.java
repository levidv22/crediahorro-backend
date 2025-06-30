package upeu.edu.pe.auth_service.dtos;

public class TokenDto {
    private String accessToken;

    public TokenDto() {
    }

    public TokenDto(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public static class Builder {

        private String accessToken;


        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        public TokenDto build() {
            return new TokenDto(accessToken);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}