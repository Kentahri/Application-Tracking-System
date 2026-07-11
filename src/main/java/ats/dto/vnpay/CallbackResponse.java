package ats.dto.vnpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CallbackResponse {

    @JsonProperty("RspCode")
    private final String responseCode;

    @JsonProperty("Message")
    private final String message;
}
