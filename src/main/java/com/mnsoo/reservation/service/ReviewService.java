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

// 리뷰 관련 로직을 처리하는 서비스 코드
@Service
@AllArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReserverRepository reserverRepository;
    private final StoreRepository storeRepository;
    private final ReservationRepository reservationRepository;

    // 리뷰 생성 메소드
    public Review createReview(ReviewRequest createRequest){
        // 넘어온 인자가 유효한지 체크
        validateRequiredField(createRequest, true);

        // 사용자가 존재하는지 체크
        ReserverEntity reserver = this.reserverRepository.findByUserId(createRequest.getReviewerId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자 입니다."));

        // 상점이 존재하는지 체크
        Store visitedStore = this.storeRepository.findByName(createRequest.getStoreName())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상점명 입니다."));

        // 존재하는 예약 내역인지 확인
        Reservation reservation = this.reservationRepository.findByStore_NameAndReservationDateAndReservationTime(
                createRequest.getStoreName(),
                createRequest.getVisitedDate(),
                createRequest.getVisitedTime()
        ).orElseThrow(() -> new RuntimeException("존재하지 않는 예약 내역입니다."));

        // 리뷰를 작성하고자 하는 사용자와 예약자의 정보가 일치하는지 체크
        if(!reserver.getPhoneNumber().equals(reservation.getReserverPhoneNumber())){
            throw new RuntimeException("사용자의 정보와 예약자의 정보가 일치하지 않습니다.");
        }

        // 상점의 별점 업데이트
        List<Review> storeReviews = this.reviewRepository.findByStoreName(visitedStore.getName());
        double totalRate = storeReviews.stream().mapToDouble(Review::getRate).sum();
        double averageRate = (totalRate + createRequest.getRate()) / (storeReviews.size() + 1);
        visitedStore.setRating(averageRate);
        this.storeRepository.save(visitedStore);

        // 리뷰 저장
        Review review = Review.builder()
                .reviewerId(reserver.getUserId())
                .store(visitedStore)
                .visitedDate(reservation.getReservationDate())
                .visitedTime(reservation.getReservationTime())
                .rate(createRequest.getRate())
                .comment(createRequest.getComment())
                .build();

        Review savedReview = this.reviewRepository.save(review);

        // 상점에 리뷰 추가, 별점 업데이트
        visitedStore.getReviews().add(savedReview);
        this.storeRepository.save(visitedStore);

        return savedReview;
    }

    // 리뷰 업데이트 메소드
    public Review updateReview(ReviewRequest updateRequest) {
        // 리뷰 조회에 필요한 인자가 다 넘어왔는지 체크
        validateRequiredField(updateRequest, false);

        // 존재하는 리뷰인지 체크
        Review review = this.reviewRepository.findByReviewerIdAndStoreNameAndVisitedDateAndVisitedTime(
                updateRequest.getReviewerId(),
                updateRequest.getStoreName(),
                updateRequest.getVisitedDate(),
                updateRequest.getVisitedTime()
        ).orElseThrow(() -> new RuntimeException("존재하지 않는 리뷰입니다."));

        // 별점이 인자로 넘어왔다면 별점 업데이트
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

        // 상점에 대한 상세 리뷰 업데이트
        if(updateRequest.getComment() != null) {
            review.setComment(updateRequest.getComment());
        }

        // 변경된 내용 저장
        return this.reviewRepository.save(review);
    }

    // 리뷰 삭제 메소드
    public String deleteReview(ReviewRequest deleteRequest) {
        // 리뷰 조회에 필요한 인자가 다 넘어왔는지 체크
        validateRequiredField(deleteRequest, false);

        // 존재하는 리뷰인지 체크
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

        // 리뷰 삭제
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

    // 각 메소드에 필요한 인자가 다 넘어왔는지 체크하는 메소드(NULL값이 들어가 있는지 체크)
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
