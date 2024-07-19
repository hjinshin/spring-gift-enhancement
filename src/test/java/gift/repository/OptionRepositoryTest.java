package gift.repository;

import gift.config.JpaConfig;
import gift.model.Category;
import gift.model.Option;
import gift.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OptionRepositoryTest {
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("id로 Product와 Fetch Join하여 조회 테스트[성공]")
    void findByIdFetchJoin() {
        // given
        String pName = "pName";
        int pPrice = 10;
        String pImageUrl = "pImageUrl";
        Category category = categoryRepository.save(new Category("cname", "ccolor", "cImage", ""));
        String oName = "oName";
        int oQuantity = 123;
        List<Option> options = List.of(new Option(oName, oQuantity));
        Product product = productRepository.save(
                new Product(pName, pPrice, pImageUrl, category, options));
        options.get(0).setProduct(product);

        // when
        Option actual = optionRepository.findByIdFetchJoin(options.get(0).getId()).orElse(null);
//
        // then
        assert actual != null;
        assertThat(actual.getName()).isEqualTo(oName);
        assertThat(actual.getQuantity()).isEqualTo(oQuantity);
        assertThat(actual.getProduct()).isEqualTo(product);
    }

    @Test
    @DisplayName(" productId로 Product와 Fetch Join하여 조회 테스트[성공]")
    void findAllByProductIdFetchJoin() {
        // given
        String pName = "pName";
        int pPrice = 10;
        String pImageUrl = "pImageUrl";
        Category category = categoryRepository.save(new Category("cname", "ccolor", "cImage", ""));
        String oName = "oName";
        int oQuantity = 123;
        List<Option> options = List.of(new Option(oName, oQuantity));
        Product product = productRepository.save(new Product(pName, pPrice, pImageUrl, category, options));
        options.get(0).setProduct(product);

        // when
        List<Option> actual = optionRepository.findAllByProductIdFetchJoin(product.getId());

        // then
        assertThat(actual).hasSize(1);
        assertThat(actual.get(0)).isEqualTo(options.get(0));
    }

    @Test
    @DisplayName("옵선 수량 빼기 작업 테스트[성공]")
    void subtractOptionQuantity() {
        // given
        int quantity = 123;
        int subtractAmount = 10;
        int expectedQuantity = quantity - subtractAmount;
        Category category = categoryRepository.save(new Category("cname", "ccolor", "cImage", ""));
        List<Option> options = List.of(new Option("oName", quantity));
        Product product = productRepository.save(new Product("pName", 0, "purl", category, options));
        Long id = product.getOptions().get(0).getId();
        Option option = optionRepository.findById(id).get();
        option.subtractQuantity(subtractAmount);
        entityManager.flush();
        entityManager.clear();

        // when
        Option actual = optionRepository.findById(id).get();

        // then
        assertThat(actual.getQuantity()).isEqualTo(expectedQuantity);
    }
}