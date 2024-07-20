package gift.repository;

import gift.model.Option;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OptionRepository extends JpaRepository<Option, Integer> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT o FROM Option o WHERE o.id = :id")
    Optional<Option> findByIdPessimisticReadLock(Long id);
}
