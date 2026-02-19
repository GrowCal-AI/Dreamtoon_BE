package com.dreamtoon.domain.subscription.entity;

import com.dreamtoon.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** 사용자 구독 정보 */
@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionTier tier = SubscriptionTier.FREE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ── 스탠다드 이미지 쿼터 ──
    @Column(name = "standard_generation_count", nullable = false)
    private Integer standardGenerationCount = 0;

    // ── 프리미엄 이미지 쿼터 ──
    @Column(name = "premium_generation_count", nullable = false)
    private Integer premiumGenerationCount = 0;

    /** 회원가입 최초 1회 무료 프리미엄 사용 여부 (영구, 리셋 안 됨) */
    @Column(name = "premium_trial_used", nullable = false)
    private Boolean premiumTrialUsed = false;

    // ── 기타 쿼터 ──
    @Column(name = "library_count", nullable = false)
    private Integer libraryCount = 0;

    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount = 0;

    @Column(name = "quota_reset_date")
    private LocalDate quotaResetDate;

    // ── Polar.sh 결제 정보 ──
    @Column(name = "polar_subscription_id")
    private String polarSubscriptionId;

    @Column(name = "polar_customer_id")
    private String polarCustomerId;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;

    @Column(name = "cancel_at_period_end", nullable = false)
    private Boolean cancelAtPeriodEnd = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Subscription(User user, SubscriptionTier tier) {
        this.user = user;
        this.tier = tier != null ? tier : SubscriptionTier.FREE;
        this.isActive = true;
        this.standardGenerationCount = 0;
        this.premiumGenerationCount = 0;
        this.premiumTrialUsed = false;
        this.libraryCount = 0;
        this.favoriteCount = 0;
        this.cancelAtPeriodEnd = false;
        this.quotaResetDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    }

    // === 스탠다드 이미지 쿼터 ===

    public boolean canGenerateStandard() {
        if (!isActive) return false;
        if (tier.isUnlimitedStandard()) return true;
        return standardGenerationCount < tier.getMaxStandardGenerations();
    }

    public void incrementStandardGenerationCount() {
        this.standardGenerationCount++;
    }

    // === 프리미엄 이미지 쿼터 ===

    /** 프리미엄 필터 사용 가능 여부 (trial 포함) */
    public boolean canGeneratePremium() {
        if (!isActive) return false;
        // 무료 회원: trial 1회만
        if (tier == SubscriptionTier.FREE) {
            return !premiumTrialUsed;
        }
        if (tier.isUnlimitedPremium()) return true;
        return premiumGenerationCount < tier.getMaxPremiumGenerations();
    }

    public void incrementPremiumGenerationCount() {
        if (tier == SubscriptionTier.FREE) {
            this.premiumTrialUsed = true;
        } else {
            this.premiumGenerationCount++;
        }
    }

    // === 라이브러리 / 즐겨찾기 ===

    public boolean canAddToLibrary(long currentLibraryCount) {
        if (!isActive) return false;
        if (tier.isUnlimitedLibrary()) return true;
        return currentLibraryCount < tier.getMaxLibraryItems();
    }

    public boolean canFavorite(long currentFavoriteCount) {
        if (!isActive) return false;
        if (tier.isUnlimitedFavorites()) return true;
        return currentFavoriteCount < tier.getMaxFavorites();
    }

    // === 월별 쿼터 리셋 ===

    public void resetMonthlyQuota() {
        this.standardGenerationCount = 0;
        this.premiumGenerationCount = 0;
        // premiumTrialUsed는 영구 — 리셋하지 않음
        this.quotaResetDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    }

    // === Polar.sh 구독 동기화 ===

    public void activateSubscription(
            String polarSubscriptionId,
            String polarCustomerId,
            SubscriptionTier newTier,
            LocalDate endDate) {
        this.polarSubscriptionId = polarSubscriptionId;
        this.polarCustomerId = polarCustomerId;
        this.tier = newTier;
        this.isActive = true;
        this.subscriptionEndDate = endDate;
        this.cancelAtPeriodEnd = false;
    }

    public void markCancelAtPeriodEnd(boolean cancel) {
        this.cancelAtPeriodEnd = cancel;
    }

    public void revokeSubscription() {
        this.tier = SubscriptionTier.FREE;
        this.isActive = true; // FREE 상태로 계속 활성
        this.polarSubscriptionId = null;
        this.subscriptionEndDate = null;
        this.cancelAtPeriodEnd = false;
    }

    /** 관리자/테스트용 티어 강제 변경 */
    public void forceSetTier(SubscriptionTier tier) {
        this.tier = tier;
        this.isActive = true;
        this.standardGenerationCount = 0;
        this.premiumGenerationCount = 0;
        this.quotaResetDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
