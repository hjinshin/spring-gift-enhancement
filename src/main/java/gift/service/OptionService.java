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
    private final OptionRepository optionRepository;
    private final ProductRepository productRepository;

    public OptionService(OptionRepository optionRepository, ProductRepository productRepository) {
        this.optionRepository = optionRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getAllOptions(Long id) {
        return optionRepository.findAllByProductIdFetchJoin(id).stream()
                .map(OptionResponse::from)
                .toList();
    }

    @Transactional
    public void save(OptionRequest.Create request) {
        checkProductExist(request.productId());
        Product product = productRepository.getReferenceById(request.productId());
        Option option = new Option(request.name(), request.quantity(), product);
        optionRepository.save(option);
    }

    @Transactional
    public void updateById(OptionRequest.Update request) {
        checkOptionExist(request.id());
        Option option = optionRepository.getReferenceById(request.id());
        Product product = option.getProduct();
        product.checkDuplicateName(request.name());
        option.updateOption(request.name(), request.quantity());
    }

    @Transactional(readOnly = true)
    public OptionResponse findByIdFetchJoin(Long id) {
        Option option = optionRepository.findByIdFetchJoin(id)
                .orElseThrow(() -> new EntityNotFoundException("Option with id " + id + " not found"));
        return OptionResponse.from(option);
    }

    @Transactional
    public void deleteByIdAndProductId(Long id, Long productId) {
        List<Option> options = optionRepository.findAllByProductIdFetchJoin(productId);
        if (options.size() <= 1) {
            throw new IllegalArgumentException("At least 1 option is required");
        }
        optionRepository.deleteById(id);
    }

    @Transactional
    public int subOptionQuantity(Long id, int amount) {
        Option option = optionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Option with id " + id + " not found"));
        return option.subtractQuantity(amount);
    }

    private void checkProductExist(Long productId) {
        if(!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product with id " + productId + " not found");
        }
    }

    private void checkOptionExist(Long id) {
        if(!optionRepository.existsById(id)) {
            throw new EntityNotFoundException("Option with id " + id + " not found");
        }
    }
}
