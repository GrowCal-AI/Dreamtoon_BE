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

    @Column(name = "generation_count", nullable = false)
    private Integer generationCount = 0;

    @Column(name = "library_count", nullable = false)
    private Integer libraryCount = 0;

    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount = 0;

    @Column(name = "quota_reset_date")
    private LocalDate quotaResetDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Subscription(User user, SubscriptionTier tier) {
        this.user = user;
        this.tier = tier != null ? tier : SubscriptionTier.FREE;
        this.isActive = true;
        this.generationCount = 0;
        this.libraryCount = 0;
        this.favoriteCount = 0;
        this.quotaResetDate = LocalDate.now().withDayOfMonth(1).plusMonths(1); // 다음 달 1일
    }

    // === 비즈니스 메서드 ===

    /** 생성 횟수 증가 */
    public void incrementGenerationCount() {
        this.generationCount++;
    }

    /** 라이브러리 저장 개수 증가 */
    public void incrementLibraryCount() {
        this.libraryCount++;
    }

    /** 라이브러리 저장 개수 감소 */
    public void decrementLibraryCount() {
        if (this.libraryCount > 0) {
            this.libraryCount--;
        }
    }

    /** 즐겨찾기 개수 증가 */
    public void incrementFavoriteCount() {
        this.favoriteCount++;
    }

    /** 즐겨찾기 개수 감소 */
    public void decrementFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }

    /** 월별 생성 횟수 초기화 (매월 1일 실행) */
    public void resetMonthlyGenerationCount() {
        this.generationCount = 0;
        this.quotaResetDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);
    }

    /** 프리미엄으로 업그레이드 */
    public void upgradeToPremium() {
        this.tier = SubscriptionTier.PREMIUM;
    }

    /** 무료로 다운그레이드 */
    public void downgradeToFree() {
        this.tier = SubscriptionTier.FREE;
    }

    /** 구독 비활성화 */
    public void deactivate() {
        this.isActive = false;
    }

    /** 구독 재활성화 */
    public void activate() {
        this.isActive = true;
    }

    /** 생성 가능 여부 확인 */
    public boolean canGenerate() {
        if (!isActive) {
            return false;
        }
        if (tier.isUnlimitedGenerations()) {
            return true;
        }
        return generationCount < tier.getMaxGenerations();
    }

    /** 라이브러리 추가 가능 여부 확인 */
    public boolean canAddToLibrary() {
        if (!isActive) {
            return false;
        }
        if (tier.isUnlimitedLibrary()) {
            return true;
        }
        return libraryCount < tier.getMaxLibraryItems();
    }

    /** 즐겨찾기 추가 가능 여부 확인 */
    public boolean canFavorite() {
        if (!isActive) {
            return false;
        }
        if (tier.isUnlimitedFavorites()) {
            return true;
        }
        return favoriteCount < tier.getMaxFavorites();
    }

    /** 프리미엄 기능 사용 가능 여부 */
    public boolean canUsePremiumFeatures() {
        return isActive && tier.isPremiumFeaturesAllowed();
    }
}
