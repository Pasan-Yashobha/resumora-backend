package com.yash.resume_builder.service;

import com.yash.resume_builder.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yash.resume_builder.util.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplatesService {

    private final AuthService authService;

    public Map<String, Object> getTemplates(Object principal) {

        // Get the current Profile
        AuthResponse authResponse = authService.getProfile(principal);

        // Get the available templates based on subscription
        List<String> availableTemplates;

        Boolean isPremium = PREMIUM.equalsIgnoreCase(authResponse.getSubscriptionPlan());

        if (isPremium) {
            availableTemplates = List.of("01", "02", "03");
        } else {
//            availableTemplates = allTemplates.subList(0, 2);
            availableTemplates = List.of("01");
        }

        // Add the data into map
        Map<String, Object> restrictions = new HashMap<>();
        restrictions.put("availableTemplates", availableTemplates);
        restrictions.put("allTemplates", List.of("01", "02", "03"));
        restrictions.put("subscriptionPlan", authResponse.getSubscriptionPlan());
        restrictions.put("isPremium", isPremium);

        // return the result
        return restrictions;
    }
}
