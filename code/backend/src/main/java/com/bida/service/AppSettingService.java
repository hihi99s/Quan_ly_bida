package com.bida.service;

import com.bida.entity.AppSetting;
import com.bida.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AppSettingService {

    private static final String HOLIDAY_MODE_KEY = "HOLIDAY_MODE";

    private final AppSettingRepository appSettingRepository;

    /**
     * Kiểm tra trạng thái Holiday Mode.
     * Trả về true nếu đang BẬT, false nếu không có cài đặt hoặc đang TẮT.
     */
    @Transactional(readOnly = true)
    public boolean isHolidayMode() {
        return appSettingRepository.findBySettingKey(HOLIDAY_MODE_KEY)
                .map(s -> "true".equalsIgnoreCase(s.getSettingValue()))
                .orElse(false);
    }

    /**
     * Bật/Tắt Holiday Mode (đảo trạng thái hiện tại).
     * Nếu chưa có setting thì tạo mới với giá trị false rồi đảo.
     */
    public void toggleHolidayMode() {
        AppSetting setting = getOrCreateHolidaySetting();
        boolean current = "true".equalsIgnoreCase(setting.getSettingValue());
        setting.setSettingValue(String.valueOf(!current));
        appSettingRepository.save(setting);
    }

    /**
     * Đặt trực tiếp trạng thái Holiday Mode.
     *
     * @param enabled true = BẬT, false = TẮT
     */
    public void setHolidayMode(boolean enabled) {
        AppSetting setting = getOrCreateHolidaySetting();
        setting.setSettingValue(String.valueOf(enabled));
        appSettingRepository.save(setting);
    }

    /**
     * Lấy giá trị của một setting key bất kỳ.
     * Trả về null nếu không tồn tại.
     */
    @Transactional(readOnly = true)
    public String getSetting(String key) {
        return appSettingRepository.findBySettingKey(key)
                .map(AppSetting::getSettingValue)
                .orElse(null);
    }

    /**
     * Đặt giá trị cho một setting key bất kỳ (upsert).
     */
    public void setSetting(String key, String value) {
        AppSetting setting = appSettingRepository.findBySettingKey(key)
                .orElseGet(() -> {
                    AppSetting s = new AppSetting();
                    s.setSettingKey(key);
                    return s;
                });
        setting.setSettingValue(value);
        appSettingRepository.save(setting);
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private AppSetting getOrCreateHolidaySetting() {
        return appSettingRepository.findBySettingKey(HOLIDAY_MODE_KEY)
                .orElseGet(() -> {
                    AppSetting s = new AppSetting();
                    s.setSettingKey(HOLIDAY_MODE_KEY);
                    s.setSettingValue("false");
                    return s;
                });
    }
}
