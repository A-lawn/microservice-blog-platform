package com.blog.platform.common.domain;

import com.blog.platform.common.infrastructure.SpringDomainEventPublisher;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * **Feature: microservice-blog-platform, Property 14: 领域事件发布处理**
 * **验证需求: Requirements 6.5**
 * 
 * 属性测试：验证领域事件发布和处理的正确性
 */
class DomainEventPublishingProperties {
    
    private ApplicationContext mockApplicationContext;
    private SpringDomainEventPublisher eventPublisher;
    private TestEventHandler testEventHandler;
    
    @BeforeProperty
    void setUp() {
        mockApplicationContext = mock(ApplicationContext.class);
        testEventHandler = new TestEventHandler();
        
        // 配置模拟的ApplicationContext返回我们的测试处理器
        when(mockApplicationContext.getBeansOfType(DomainEventHandler.class))
            .thenReturn(java.util.Map.of("testHandler", testEventHandler));
        
        eventPublisher = new SpringDomainEventPublisher(mockApplicationContext);
    }
    
    @Property(tries = 100)
    @Label("对于任何领域事件，系统应当正确发布事件并由相应的事件处理器处理")
    void domainEventPublishingAndHandling(@ForAll("testEvents") TestDomainEvent event) {
        // Given: 一个领域事件和事件处理器
        testEventHandler.reset();
        
        // When: 发布事件
        eventPublisher.publish(event);
        
        // Then: 事件应该被正确处理
        assertThat(testEventHandler.getHandledEvents())
            .hasSize(1)
            .contains(event);
        
        assertThat(testEventHandler.getHandleCount()).isEqualTo(1);
    }
    
    @Property(tries = 100)
    @Label("对于任何事件列表，批量发布应当处理所有事件")
    void batchEventPublishing(@ForAll("eventLists") List<TestDomainEvent> events) {
        // Given: 一个事件列表
        testEventHandler.reset();
        
        // When: 批量发布事件
        List<DomainEvent> domainEvents = new ArrayList<>(events);
        eventPublisher.publishAll(domainEvents);
        
        // Then: 所有事件都应该被处理
        assertThat(testEventHandler.getHandledEvents())
            .hasSize(events.size())
            .containsExactlyElementsOf(events);
        
        assertThat(testEventHandler.getHandleCount()).isEqualTo(events.size());
    }
    
    @Property(tries = 100)
    @Label("对于任何聚合根，发布事件后应当清除聚合根中的事件")
    void aggregateRootEventPublishing(@ForAll("testAggregates") TestAggregateRoot aggregate) {
        // Given: 一个包含事件的聚合根
        testEventHandler.reset();
        
        // 确保聚合根有事件
        if (!aggregate.hasDomainEvents()) {
            aggregate.addTestEvent(new TestDomainEvent(aggregate.getId().toString()));
        }
        
        int initialEventCount = aggregate.getDomainEvents().size();
        
        // When: 发布聚合根的事件
        eventPublisher.publishEvents(aggregate);
        
        // Then: 聚合根的事件应该被清除，处理器应该处理所有事件
        assertThat(aggregate.hasDomainEvents()).isFalse();
        assertThat(aggregate.getDomainEvents()).isEmpty();
        assertThat(testEventHandler.getHandleCount()).isEqualTo(initialEventCount);
    }
    
    // 生成器方法
    @Provide
    Arbitrary<TestDomainEvent> testEvents() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(50)
            .map(TestDomainEvent::new);
    }
    
    @Provide
    Arbitrary<List<TestDomainEvent>> eventLists() {
        return testEvents().list().ofMinSize(0).ofMaxSize(10);
    }
    
    @Provide
    Arbitrary<TestAggregateRoot> testAggregates() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(20)
            .map(TestAggregateRoot::new)
            .map(aggregate -> {
                // 随机添加一些事件
                int eventCount = Arbitraries.integers().between(0, 5).sample();
                for (int i = 0; i < eventCount; i++) {
                    aggregate.addTestEvent(new TestDomainEvent(aggregate.getId() + "-event-" + i));
                }
                return aggregate;
            });
    }
    
    // 测试用的领域事件
    static class TestDomainEvent extends DomainEvent {
        private final String testData;
        
        public TestDomainEvent(String aggregateId) {
            super(aggregateId);
            this.testData = "test-data-" + aggregateId;
        }
        
        public String getTestData() {
            return testData;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            if (!super.equals(obj)) return false;
            
            TestDomainEvent that = (TestDomainEvent) obj;
            return testData.equals(that.testData);
        }
        
        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + testData.hashCode();
            return result;
        }
    }
    
    // 测试用的聚合根
    static class TestAggregateRoot extends AggregateRoot<String> {
        private final String id;
        
        public TestAggregateRoot(String id) {
            this.id = id;
        }
        
        @Override
        public String getId() {
            return id;
        }
        
        public void addTestEvent(TestDomainEvent event) {
            addDomainEvent(event);
        }
    }
    
    // 测试用的事件处理器
    static class TestEventHandler implements DomainEventHandler<TestDomainEvent> {
        private final List<TestDomainEvent> handledEvents = new ArrayList<>();
        private final AtomicInteger handleCount = new AtomicInteger(0);
        
        @Override
        public void handle(TestDomainEvent event) {
            handledEvents.add(event);
            handleCount.incrementAndGet();
        }
        
        @Override
        public Class<TestDomainEvent> getEventType() {
            return TestDomainEvent.class;
        }
        
        public List<TestDomainEvent> getHandledEvents() {
            return new ArrayList<>(handledEvents);
        }
        
        public int getHandleCount() {
            return handleCount.get();
        }
        
        public void reset() {
            handledEvents.clear();
            handleCount.set(0);
        }
    }
}