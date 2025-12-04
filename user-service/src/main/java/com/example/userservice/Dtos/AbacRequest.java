package com.example.userservice.Dtos;

import lombok.Data;

import java.util.Map;
@Data
public class AbacRequest {
    private String service, function, userId;
    private Map<String, Object> requestAttributes;

//    public String getService() { return service; }
//    public void setService(String service) { this.service = service; }
//    public String getFunction() { return function; }
//    public void setFunction(String function) { this.function = function; }
//    public String getUserId() { return userId; }
//    public void setUserId(String userId) { this.userId = userId; }
//    public Map<String, Object> getRequestAttributes() { return requestAttributes; }
//    public void setRequestAttributes(Map<String, Object> requestAttributes) { this.requestAttributes = requestAttributes; }
}
