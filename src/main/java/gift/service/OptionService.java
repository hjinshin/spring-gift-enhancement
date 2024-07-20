package gift.service;

import gift.common.exception.EntityNotFoundException;
import gift.model.Option;
import gift.repository.OptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptionService {
    private final OptionRepository optionRepository;

    public OptionService(OptionRepository optionRepository) {
        this.optionRepository = optionRepository;
    }

    @Transactional(timeout = 5)
    public int subOptionQuantity(Long optionId, int amount) {
        Option option = optionRepository.findByIdPessimisticReadLock(optionId)
                .orElseThrow(() -> new EntityNotFoundException("Option with id " + optionId + " not found"));
        return option.subtractQuantity(amount);
    }
}
