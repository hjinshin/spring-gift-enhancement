package gift.service;

import gift.common.exception.EntityNotFoundException;
import gift.controller.dto.request.OptionRequest;
import gift.controller.dto.response.OptionResponse;
import gift.model.Option;
import gift.model.Product;
import gift.repository.OptionRepository;
import gift.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OptionService {
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;

    public OptionService(ProductRepository productRepository, OptionRepository optionRepository) {
        this.productRepository = productRepository;
        this.optionRepository = optionRepository;
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getAllOptions(Long productId) {
        Product product = productRepository.findProductAndOptionByIdFetchJoin(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + productId + " not found"));
        return product.getOptions().stream()
                .map(OptionResponse::from)
                .toList();
    }

    @Transactional
    public void addOption(OptionRequest.Create request) {
        checkProductExist(request.productId());
        Product product = productRepository.getReferenceById(request.productId());
        Option option = new Option(request.name(), request.quantity(), product);
        product.addOption(option);
    }

    @Transactional
    public void updateOption(OptionRequest.Update request) {
        Product product = productRepository.findProductAndOptionByIdFetchJoin(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + request.productId() + " not found"));
        Option option = product.findOptionByOptionId(request.id());
        product.checkDuplicateOptionName(request.id(), request.name());
        option.updateOption(request.name(), request.quantity());
    }

    @Transactional(readOnly = true)
    public OptionResponse findOptionByProductId(Long productId, Long optionId) {
        Product product = productRepository.findProductAndOptionByIdFetchJoin(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + productId + " not found"));
        Option option = product.findOptionByOptionId(optionId);
        return OptionResponse.from(option);
    }

    @Transactional(timeout = 5)
    public int subOptionQuantity(Long optionId, int amount) {
        Option option = optionRepository.findByIdPessimisticReadLock(optionId)
                .orElseThrow(() -> new EntityNotFoundException("Option with id " + optionId + " not found"));
        return option.subtractQuantity(amount);
    }

    private void checkProductExist(Long productId) {
        if(!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product with id " + productId + " not found");
        }
    }
}
