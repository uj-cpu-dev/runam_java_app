package rum_am_app.run_am.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rum_am_app.run_am.dtorequest.UserSettingsUpdateRequest;
import rum_am_app.run_am.model.UserSettings;
import rum_am_app.run_am.repository.UserSettingsRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserSettingsRepository settingsRepository;
    public UserSettings getSettings(String userId) {
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
    }

    public UserSettings updateSettings(String userId, UserSettingsUpdateRequest updates) {
        UserSettings settings = getExistingOrCreateDefault(userId);

        // Notification settings
        if (updates.getEmailNotifications() != null)
            settings.setEmailNotifications(updates.getEmailNotifications());
        if (updates.getPushNotifications() != null)
            settings.setPushNotifications(updates.getPushNotifications());
        if (updates.getSmsNotifications() != null)
            settings.setSmsNotifications(updates.getSmsNotifications());
        if (updates.getMarketingNotifications() != null)
            settings.setMarketingNotifications(updates.getMarketingNotifications());
        if (updates.getNewMessageNotifications() != null)
            settings.setNewMessageNotifications(updates.getNewMessageNotifications());
        if (updates.getItemUpdateNotifications() != null)
            settings.setItemUpdateNotifications(updates.getItemUpdateNotifications());
        if (updates.getPriceDropNotifications() != null)
            settings.setPriceDropNotifications(updates.getPriceDropNotifications());

        // Privacy settings
        if (updates.getShowPhoneNumber() != null)
            settings.setShowPhoneNumber(updates.getShowPhoneNumber());
        if (updates.getShowEmail() != null)
            settings.setShowEmail(updates.getShowEmail());
        if (updates.getShowOnlineStatus() != null)
            settings.setShowOnlineStatus(updates.getShowOnlineStatus());
        if (updates.getPublicProfile() != null)
            settings.setPublicProfile(updates.getPublicProfile());

        return settingsRepository.save(settings);
    }


    private UserSettings getExistingOrCreateDefault(String userId) {
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
    }

    private UserSettings createDefaultSettings(String userId) {
        return UserSettings.builder()
                .userId(userId)
                .emailNotifications(true)
                .pushNotifications(true)
                .newMessageNotifications(true)
                .itemUpdateNotifications(true)
                .showOnlineStatus(true)
                .publicProfile(true)
                .build();
    }
}
