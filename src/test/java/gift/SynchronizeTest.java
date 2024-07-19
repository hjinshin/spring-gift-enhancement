package gift;

import gift.model.Category;
import gift.model.Option;
import gift.model.Product;
import gift.repository.CategoryRepository;
import gift.repository.OptionRepository;
import gift.repository.ProductRepository;
import gift.service.OptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class SynchronizeTest {
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OptionService optionService;

    @Test
    @DisplayName("동시에 삭제 요청")
    void subtractRequestAtTheSameTime() throws InterruptedException {
        // given
        int quantity = 100;
        int subtractAmount = 1;
        Category category = categoryRepository.save(new Category("cname", "ccolor", "cImage", ""));
        List<Option> options = List.of(new Option("oName", quantity));
        Product product = productRepository.save(new Product("pName", 0, "purl", category, options));
        Long id = product.getOptions().get(0).getId();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        int threadCount = 100; // 스레드 개수
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 스레드 풀 크기
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    while (true) {
                        try {
                            optionService.subOptionQuantity(id, subtractAmount);
                            successCount.incrementAndGet();
                            break;
                        } catch (Exception e) {
                            Thread.sleep(50);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // when
        Option actual = optionRepository.findById(id).get();

        // then
        int expactedQuantity = quantity - subtractAmount * threadCount;
        assertThat(actual.getQuantity()).isEqualTo(expactedQuantity);
    }
}
