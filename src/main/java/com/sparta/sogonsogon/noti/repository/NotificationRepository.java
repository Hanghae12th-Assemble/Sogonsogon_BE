package com.sparta.sogonsogon.noti.repository;

import com.sparta.sogonsogon.noti.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findByReceiverIdNot(Long memberId);

    List<Notification> findByReceiverId(Long memberId);

    List<Notification> findByReceiver_IdOrderByCreatedAtDesc(Long receiverId);

//    List<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long memberId);
    Page<Notification> findAllByReceiverIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

//    @Query(value = "SELECT * FROM notification n " +
//            "WHERE n.created_at < date_add(now(), INTERVAL -1 DAY) " +
//            "AND n.is_read = 'true' AND NOW()", nativeQuery = true)
//    List<Notification> findOldNotification();

//    @Query("SELECT n FROM notification n WHERE n.createdAt < :cutoffTime")
//    List<Notification> findExpiredNotification(@Param("cutoffTime") LocalDateTime cutoffTime);

}
