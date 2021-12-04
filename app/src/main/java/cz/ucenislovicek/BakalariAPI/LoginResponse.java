// taken from https://github.com/vitSkalicky/lepsi-rozvrh/
package cz.ucenislovicek.BakalariAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    public String access_token;
    public int expires_in;
    public String refresh_token;
}
