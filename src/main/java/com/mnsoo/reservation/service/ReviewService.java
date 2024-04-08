package com.mnsoo.reservation.service;

import com.mnsoo.reservation.domain.ReviewRequest;
import com.mnsoo.reservation.domain.persist.Reservation;
import com.mnsoo.reservation.domain.persist.ReserverEntity;
import com.mnsoo.reservation.domain.persist.Review;
import com.mnsoo.reservation.domain.persist.Store;
import com.mnsoo.reservation.repository.ReservationRepository;
import com.mnsoo.reservation.repository.ReserverRepository;
import com.mnsoo.reservation.repository.ReviewRepository;
import com.mnsoo.reservation.repository.StoreRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReserverRepository reserverRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    public Review createReview(ReviewRequest createRequest){
        validateRequiredField(createRequest, true);

        ReserverEntity reserver = this.reserverRepository.findByUserId(createRequest.getReviewerId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 입니다."));

        Store visitedStore = this.storeRepository.findByName(createRequest.getStoreName())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상점명 입니다."));

        Reservation reservation = this.reservationRepository.findByStore_NameAndReservationDateAndReservationTime(
                createRequest.getStoreName(),
                createRequest.getVisitedDate(),
                createRequest.getVisitedTime()
        ).orElseThrow(() -> new RuntimeException("존재하지 않는 예약 내역입니다."));

        if(!reserver.getPhoneNumber().equals(reservation.getReserverPhoneNumber())){
            throw new RuntimeException("사용자의 정보와 예약자의 정보가 일치하지 않습니다.");
        }

        List<Review> storeReviews = this.reviewRepository.findByStoreName(visitedStore.getName());
        double totalRate = storeReviews.stream().mapToDouble(Review::getRate).sum();
        double averageRate = (totalRate + createRequest.getRate()) / (storeReviews.size() + 1);
        visitedStore.setRating(averageRate);
        this.storeRepository.save(visitedStore);

        Review review = Review.builder()
                .reviewerId(reserver.getUserId())
                .store(visitedStore)
                .visitedDate(reservation.getReservationDate())
                .visitedTime(reservation.getReservationTime())
                .rate(createRequest.getRate())
                .comment(createRequest.getComment())
                .build();

        Review savedReview = this.reviewRepository.save(review);

        visitedStore.getReviews().add(savedReview);
        this.storeRepository.save(visitedStore);

        return savedReview;
    }

    public Review updateReview(ReviewRequest updateRequest) {
        validateRequiredField(updateRequest, false);

        Review review = this.reviewRepository.findByReviewerIdAndStoreNameAndVisitedDateAndVisitedTime(
                updateRequest.getReviewerId(),
                updateRequest.getStoreName(),
                updateRequest.getVisitedDate(),
                updateRequest.getVisitedTime()
        ).orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다."));

        if(updateRequest.getRate() != null) {
            review.setRate(updateRequest.getRate());
            // 상점의 평점 업데이트
            List<Review> storeReviews = this.reviewRepository.findByStoreName(review.getStore().getName());
            double totalRate = storeReviews.stream().mapToDouble(Review::getRate).sum();
            double averageRate = totalRate / storeReviews.size();
            Store store = this.storeRepository.findByName(review.getStore().getName())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 상점명 입니다."));
            store.setRating(averageRate);
            this.storeRepository.save(store);
        }

        if(updateRequest.getComment() != null) {
            review.setComment(updateRequest.getComment());
        }

        return this.reviewRepository.save(review);
    }

    public String deleteReview(ReviewRequest deleteRequest) {
        validateRequiredField(deleteRequest, false);

        Review review = this.reviewRepository.findByReviewerIdAndStoreNameAndVisitedDateAndVisitedTime(
                deleteRequest.getReviewerId(),
                deleteRequest.getStoreName(),
                deleteRequest.getVisitedDate(),
                deleteRequest.getVisitedTime()
        ).orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다."));

        // 상점의 평점 업데이트
        List<Review> storeReviews = this.reviewRepository.findByStoreName(review.getStore().getName());
        double totalRate = storeReviews.stream().filter(r -> !r.getId().equals(review.getId())).mapToDouble(Review::getRate).sum();
        double averageRate = storeReviews.size() > 1 ? totalRate / (storeReviews.size() - 1) : 0;
        Store store = this.storeRepository.findByName(review.getStore().getName())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상점명 입니다."));
        store.setRating(averageRate);
        this.storeRepository.save(store);

        this.reviewRepository.delete(review);

        store.getReviews().remove(review);
        this.storeRepository.save(store);

        return "Delete successful";
    }

    public String deleteReviewByAdmin(String storeName, String userId, String partnerId, LocalDate date, LocalTime time) {
        Store store = this.storeRepository.findByName(storeName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상점명 입니다."));

        if(!partnerId.equals(store.getPartner().getUserId())){
            throw new RuntimeException("상점주만 해당 리뷰를 삭제할 수 있습니다.");
        }

        Review review = this.reviewRepository.findByReviewerIdAndStoreNameAndVisitedDateAndVisitedTime(
                userId,
                storeName,
                date,
                time
        ).orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다."));

        // 상점의 평점 업데이트
        List<Review> storeReviews = this.reviewRepository.findByStore(store);
        double totalRate = storeReviews.stream().filter(r -> !r.getId().equals(review.getId())).mapToDouble(Review::getRate).sum();
        double averageRate = storeReviews.size() > 1 ? totalRate / (storeReviews.size() - 1) : 0;
        store.setRating(averageRate);
        this.storeRepository.save(store);

        this.reviewRepository.delete(review);

        store.getReviews().remove(review);
        this.storeRepository.save(store);

        return "Delete successful";
    }

    private void validateRequiredField(ReviewRequest request, boolean isCreate){
        Optional.ofNullable(request.getReviewerId()).orElseThrow(() -> new RuntimeException("사용자 ID는 필수값입니다."));
        Optional.ofNullable(request.getStoreName()).orElseThrow(() -> new RuntimeException("상점 이름은 필수값입니다."));
        Optional.ofNullable(request.getVisitedDate()).orElseThrow(() -> new RuntimeException("방문한 날짜는 필수값입니다."));
        Optional.ofNullable(request.getVisitedTime()).orElseThrow(() -> new RuntimeException("방문 시간은 필수값입니다."));
        if(isCreate){
            Optional.ofNullable(request.getRate()).orElseThrow(() -> new RuntimeException("평점은 필수값입니다."));
            Optional.ofNullable(request.getComment()).orElseThrow(() -> new RuntimeException("리뷰 내용은 필수값입니다."));
        }
    }
}
