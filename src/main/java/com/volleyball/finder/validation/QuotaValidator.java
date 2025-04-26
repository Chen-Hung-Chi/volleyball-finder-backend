package com.volleyball.finder.validation;


import com.volleyball.finder.dto.ActivityUpdateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class QuotaValidator implements ConstraintValidator<QuotaConstraint, ActivityUpdateDto> {

    @Override
    public boolean isValid(ActivityUpdateDto dto, ConstraintValidatorContext context) {
        if (dto.getMaleQuota() == null || dto.getFemaleQuota() == null || dto.getMaxParticipants() == null)
            return true;

        int maleQuota = dto.getMaleQuota();
        int femaleQuota = dto.getFemaleQuota();
        int maxParticipants = dto.getMaxParticipants();

        // rule 1: -1 (禁止報名) 不能同時出現在男女 quota
        if (maleQuota == -1 && femaleQuota == -1) return false;

        // rule 2: 禁止男生報名時女生 quota 必須是 0
        if (maleQuota == -1 && femaleQuota != 0) return false;

        // rule 3: 禁止女生報名時男生 quota 必須是 0
        if (femaleQuota == -1 && maleQuota != 0) return false;

        // rule 4: 若任一 quota 為 0，另一方不得等於 maxParticipants
        if (maleQuota == 0 && femaleQuota == maxParticipants) return false;
        if (femaleQuota == 0 && maleQuota == maxParticipants) return false;

        // rule 5: quota 不得小於 -1
        if (maleQuota < -1 || femaleQuota < -1) return false;

        // rule 6: quota 不得超過 maxParticipants
        if (maleQuota > maxParticipants || femaleQuota > maxParticipants) return false;

        // rule 7: 若兩者皆 > 0，總和必須等於 maxParticipants
        if (maleQuota > 0 && femaleQuota > 0 && maleQuota + femaleQuota != maxParticipants) return false;

        return true;
    }
}
