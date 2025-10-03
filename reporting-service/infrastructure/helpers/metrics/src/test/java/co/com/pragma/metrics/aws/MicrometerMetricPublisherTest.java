package co.com.pragma.metrics.aws;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricRecord;
import software.amazon.awssdk.metrics.SdkMetric;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MicrometerMetricPublisherTest {

    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private Timer timer;
    @Mock
    private Counter counter;

    private MicrometerMetricPublisher metricPublisher;

    @BeforeEach
    void setUp() {
        metricPublisher = new MicrometerMetricPublisher(meterRegistry);
    }

    @Test
    void shouldPublishMetricsCorrectly() throws InterruptedException {
        when(meterRegistry.timer(anyString(), any(Iterable.class))).thenReturn(timer);
        when(meterRegistry.counter(anyString(), any(Iterable.class))).thenReturn(counter);

        SdkMetric<Duration> durationMetric = mock(SdkMetric.class);
        when(durationMetric.name()).thenReturn("ApiCallDuration");

        SdkMetric<Integer> countMetric = mock(SdkMetric.class);
        when(countMetric.name()).thenReturn("RetryCount");

        SdkMetric<String> tagMetric = mock(SdkMetric.class);
        when(tagMetric.name()).thenReturn("OperationName");

        SdkMetric<Boolean> boolMetric = mock(SdkMetric.class);
        when(boolMetric.name()).thenReturn("IsSuccessful");

        MetricCollection testMetrics = new TestMetricCollection(Arrays.asList(
                new TestMetricRecord<>(durationMetric, Duration.ofMillis(150)),
                new TestMetricRecord<>(countMetric, 3),
                new TestMetricRecord<>(tagMetric, "GetItem"),
                new TestMetricRecord<>(boolMetric, true)
        ));

        metricPublisher.publish(testMetrics);

        Thread.sleep(100);

        List<Tag> expectedTags = Arrays.asList(
                Tag.of("OperationName", "GetItem"),
                Tag.of("IsSuccessful", "true")
        );

        verify(meterRegistry).timer("ApiCallDuration", expectedTags);
        verify(timer).record(Duration.ofMillis(150));

        verify(meterRegistry).counter("RetryCount", expectedTags);
        verify(counter).increment(3.0);
    }

    @Test
    void closeDoesNothing() {
        metricPublisher.close();
    }

    private static class TestMetricCollection implements MetricCollection {
        private final List<MetricRecord<?>> records;
        private final Instant creationTime = Instant.now();

        public TestMetricCollection(List<MetricRecord<?>> records) {
            this.records = records;
        }

        @Override
        public String name() {
            return "TestCollection";
        }

        @Override
        public Stream<MetricRecord<?>> stream() {
            return records.stream();
        }

        @Override
        public List<MetricCollection> children() {
            return Collections.emptyList();
        }

        @Override
        public <T> List<T> metricValues(SdkMetric<T> metric) {
            return records.stream()
                    .filter(r -> r.metric().equals(metric))
                    .map(r -> (T) r.value())
                    .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public Instant creationTime() {
            return creationTime;
        }

        @Override
        public java.util.Iterator<MetricRecord<?>> iterator() {
            return records.iterator();
        }
    }

    private static class TestMetricRecord<T> implements MetricRecord<T> {
        private final SdkMetric<T> metric;
        private final T value;

        public TestMetricRecord(SdkMetric<T> metric, T value) {
            this.metric = metric;
            this.value = value;
        }

        @Override
        public SdkMetric<T> metric() {
            return metric;
        }

        @Override
        public T value() {
            return value;
        }
    }
}